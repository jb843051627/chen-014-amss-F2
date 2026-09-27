package com.fc.v2.service.impl;

import java.util.ArrayList;
import java.util.Calendar;
import java.util.List;

import org.springframework.stereotype.Service;

import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.fc.v2.mapper.auto.TAmssMachRegMapper;
import com.fc.v2.mapper.auto.TAmssPassBillMapper;
import com.fc.v2.mapper.auto.TAmssPassCertMapper;
import com.fc.v2.mapper.auto.TAmssPassSignMapper;
import com.fc.v2.mapper.auto.TAmssTeamCardMapper;
import com.fc.v2.model.auto.TAmssMachReg;
import com.fc.v2.model.auto.TAmssPassBill;
import com.fc.v2.model.auto.TAmssPassCert;
import com.fc.v2.model.auto.TAmssPassSign;
import com.fc.v2.model.auto.TAmssTeamCard;
import com.fc.v2.model.custom.PassBillView;
import com.fc.v2.service.ITAmssPassBillService;

/**
 * 跨区作业证逐级核准单 Service业务层处理。
 *
 * 四道关口：0受理 1核验 2复核（两名点齐，各记各的）3用章。
 * 流转只认节点：上一道没走完下一道看不到件；本关已签人数一律从签字流水
 * {@link #recount(TAmssPassBill)} 回算，屏幕数与库存数同出此回计算。
 *
 * @author fuce
 * @date 2026-09-14
 */
@Service
public class TAmssPassBillServiceImpl implements ITAmssPassBillService {

    /** 关口：受理 */
    private static final int NODE_RECEIVE = 0;
    /** 关口：核验 */
    private static final int NODE_VERIFY = 1;
    /** 关口：复核（两名并排签） */
    private static final int NODE_REVIEW = 2;
    /** 关口：用章报市局 */
    private static final int NODE_SEAL = 3;

    /** 同层核准办法：任一人 */
    private static final int MODE_OR = 0;
    /** 同层核准办法：两名点齐 */
    private static final int MODE_AND = 1;

    /** 报批情形：在核 */
    private static final int STATUS_RUNNING = 0;
    /** 报批情形：已核讫（章落到底） */
    private static final int STATUS_PASS = 1;
    /** 报批情形：不予出证（终局） */
    private static final int STATUS_DENY = 3;

    /** 名目类别：核准签字 */
    private static final int KIND_APPROVE = 0;
    /** 名目类别：不予出证 */
    private static final int KIND_DENY = 1;

    /** 单人关位次 */
    private static final int SEAT_SOLE = 0;
    /** 复核关：名册合同位 */
    private static final int SEAT_ROSTER = 1;
    /** 复核关：牌证证文合龙位 */
    private static final int SEAT_MATCH = 2;

    private static final String[] SEAT_NAMES_AT_NODE = {
            "受理", "在册牌证与年度检验核验", "", "本站用章报市局存查"
    };

    @javax.annotation.Resource
    private TAmssPassBillMapper amssPassBillMapper;
    @javax.annotation.Resource
    private TAmssPassSignMapper amssPassSignMapper;
    @javax.annotation.Resource
    private TAmssPassCertMapper amssPassCertMapper;
    @javax.annotation.Resource
    private TAmssMachRegMapper amssMachRegMapper;
    @javax.annotation.Resource
    private TAmssTeamCardMapper amssTeamCardMapper;

    @Override
    public TAmssPassBill selectTAmssPassBillById(Long id) {
        return id == null ? null : this.amssPassBillMapper.selectById(id);
    }

    @Override
    public List<TAmssPassBill> selectPassBillList(QueryWrapper<TAmssPassBill> queryWrapper) {
        return this.amssPassBillMapper.selectList(queryWrapper);
    }

    @Override
    public PassBillView receive(TAmssPassBill form) {
        if (form == null
                || !hasText(form.getOwnerName())
                || !hasText(form.getDriveCode())
                || !hasText(form.getPlateNo())
                || !hasText(form.getCertText())) {
            // 四样少一样，当场拦在门外
            return null;
        }
        int year = form.getIssueYear() == null
                ? Calendar.getInstance().get(Calendar.YEAR) : form.getIssueYear();

        TAmssPassBill bill = new TAmssPassBill();
        bill.setBillNo(form.getBillNo());
        bill.setOwnerName(form.getOwnerName().trim());
        bill.setDriveCode(form.getDriveCode().trim());
        bill.setPlateNo(form.getPlateNo().trim());
        bill.setCertText(form.getCertText().trim());
        bill.setDestProvince(form.getDestProvince());
        bill.setValidFrom(form.getValidFrom());
        bill.setValidTo(form.getValidTo());
        bill.setTeamCardNo(form.getTeamCardNo());
        bill.setIssueYear(year);
        bill.setRoundNo(1);
        bill.setInspectBlock(0);
        bill.setSealed(0);
        bill.setStatus(STATUS_RUNNING);
        bill.setNodeNo(NODE_RECEIVE);
        bill.setSignMode(MODE_OR);
        bill.setNeedCount(1);
        bill.setSignCount(0);
        bill.setDelFlag(0);
        this.amssPassBillMapper.insert(bill);
        if (!hasText(bill.getBillNo())) {
            bill.setBillNo("HZ" + year + String.format("%06d", bill.getId() % 1000000L));
            this.amssPassBillMapper.updateById(bill);
        }

        // 受理窗这一双手落一笔，件随即挪到核验关
        writeSign(bill, SEAT_SOLE, SEAT_NAMES_AT_NODE[NODE_RECEIVE],
                KIND_APPROVE, hasText(form.getCreateBy()) ? form.getCreateBy() : "受理窗", "受理要件齐备");
        settleNode(bill);
        this.amssPassBillMapper.updateById(bill);
        return view(bill.getId());
    }

    @Override
    public TAmssPassBill approve(Long id, String approver, String comment) {
        TAmssPassBill bill = lockBill(id);
        if (bill == null || !hasText(approver)) {
            return null;
        }
        int node = bill.getNodeNo();
        if (node == NODE_REVIEW) {
            // 两名点齐关不受理旧签法，须指明位次走重载，免得一只手顶两个名
            return null;
        }
        if (node == NODE_VERIFY) {
            if (!verifyPlateAndInspection(bill)) {
                return null;
            }
        } else if (node != NODE_SEAL) {
            return null;
        }
        boolean sealedNow = node == NODE_SEAL;
        writeSign(bill, SEAT_SOLE, SEAT_NAMES_AT_NODE[node], KIND_APPROVE, approver.trim(), comment);
        if (sealedNow) {
            // 章一落到底，各栏随即封死
            bill.setSealed(1);
            bill.setStatus(STATUS_PASS);
            bill.setSignCount(recount(bill));
        } else {
            settleNode(bill);
        }
        this.amssPassBillMapper.updateById(bill);
        return bill;
    }

    @Override
    public TAmssPassBill approve(Long id, String approver, Integer seatNo, String comment) {
        TAmssPassBill bill = lockBill(id);
        if (bill == null || !hasText(approver) || seatNo == null) {
            return null;
        }
        if (bill.getNodeNo() != NODE_REVIEW || bill.getStatus() != STATUS_RUNNING) {
            // 件不在复核关，这个位次的签无处落
            return null;
        }
        int seat = seatNo.intValue();
        if (seat != SEAT_ROSTER && seat != SEAT_MATCH) {
            return null;
        }
        List<TAmssPassSign> hands = activeSigns(bill, NODE_REVIEW);
        for (TAmssPassSign s : hands) {
            if (s.getSeatNo() != null && s.getSeatNo().intValue() == seat) {
                // 这位已经有人判过，不能再压一笔
                return null;
            }
            if (approver.trim().equals(s.getApprover())) {
                // 同一只手在本关再落一笔，无论哪个位次都按替签论，当没签
                return null;
            }
        }
        if (seat == SEAT_ROSTER) {
            if (!verifyRosterAndContract(bill)) {
                return null;
            }
        } else {
            if (!verifyDocMatch(bill)) {
                return null;
            }
        }
        writeSign(bill, seat, PassBillView.SEAT_NAMES[seat], KIND_APPROVE, approver.trim(), comment);
        settleNode(bill);
        this.amssPassBillMapper.updateById(bill);
        return bill;
    }

    @Override
    public TAmssPassBill reject(Long id, String approver, String comment) {
        TAmssPassBill bill = lockBill(id);
        if (bill == null || !hasText(approver)) {
            return null;
        }
        // 哪一道写下不予出证，件就此止步；过后谁也补不进一枚
        writeSign(bill, SEAT_SOLE, "不予出证", KIND_DENY, approver.trim(), comment);
        bill.setStatus(STATUS_DENY);
        this.amssPassBillMapper.updateById(bill);
        return bill;
    }

    @Override
    public TAmssPassBill rollback(Long id, String comment) {
        TAmssPassBill bill = lockBill(id);
        if (bill == null) {
            return null;
        }
        // 只准在用章关、章尚未落时由市局打回；已盖封或压在别道都打不得
        if (bill.getNodeNo() != NODE_SEAL || bill.getStatus() != STATUS_RUNNING
                || bill.getSealed() == null || bill.getSealed().intValue() == 1) {
            return null;
        }
        // 先前各道记下的名目尽数勾销：行保留、只翻勾销标记，勾掉的与后补的不摆在一处
        List<TAmssPassSign> all = activeSignsOfRound(bill);
        for (TAmssPassSign s : all) {
            s.setVoidFlag(1);
            this.amssPassSignMapper.updateById(s);
        }
        bill.setRoundNo(bill.getRoundNo() == null ? 2 : bill.getRoundNo() + 1);
        bill.setNodeNo(NODE_VERIFY);
        bill.setSignMode(MODE_OR);
        bill.setNeedCount(1);
        bill.setSignCount(0);
        bill.setStatus(STATUS_RUNNING);
        bill.setRemark(comment);
        this.amssPassBillMapper.updateById(bill);
        return bill;
    }

    @Override
    public TAmssPassCert issueCert(Long id, String certNo) {
        if (id == null) {
            return null;
        }
        // 出证只认落章核讫件，不经 lockBill（那道口子只放在核件）
        TAmssPassBill bill = this.amssPassBillMapper.selectById(id);
        if (bill == null || (bill.getDelFlag() != null && bill.getDelFlag() == 1)
                || bill.getSealed() == null || bill.getSealed() != 1
                || bill.getStatus() == null || bill.getStatus() != STATUS_PASS
                || hasText(bill.getCertNo())) {
            // 四道未走满、未盖章，或已经转过证文，都不得再转
            return null;
        }
        // 同一位机主当年第二条重复抄发的，当场拦下
        QueryWrapper<TAmssPassCert> dup = new QueryWrapper<>();
        dup.eq("owner_name", bill.getOwnerName())
           .eq("issue_year", bill.getIssueYear())
           .eq("del_flag", 0);
        if (this.amssPassCertMapper.selectCount(dup) > 0) {
            return null;
        }
        TAmssPassCert cert = new TAmssPassCert();
        cert.setBillId(bill.getId());
        cert.setCertNo(hasText(certNo) ? certNo.trim()
                : "JX" + bill.getIssueYear() + String.format("%06d", bill.getId() % 1000000L));
        cert.setOwnerName(bill.getOwnerName());
        cert.setPlateNo(bill.getPlateNo());
        cert.setDestProvince(bill.getDestProvince());
        cert.setValidFrom(bill.getValidFrom());
        cert.setValidTo(bill.getValidTo());
        cert.setCertText(bill.getCertText());
        cert.setIssueYear(bill.getIssueYear());
        cert.setIssueTime(Calendar.getInstance().getTime());
        cert.setDelFlag(0);
        this.amssPassCertMapper.insert(cert);

        // 证文号是系统一键转出时回连的存根，不属于任何一道可由人填的栏目
        TAmssPassBill back = new TAmssPassBill();
        back.setId(bill.getId());
        back.setCertNo(cert.getCertNo());
        this.amssPassBillMapper.updateById(back);
        bill.setCertNo(cert.getCertNo());
        return cert;
    }

    @Override
    public PassBillView view(Long id) {
        TAmssPassBill bill = id == null ? null : this.amssPassBillMapper.selectById(id);
        if (bill == null) {
            return null;
        }
        PassBillView v = new PassBillView();
        v.setBill(bill);

        QueryWrapper<TAmssPassSign> q = new QueryWrapper<>();
        q.eq("bill_id", bill.getId()).orderByAsc("round_no", "node_no", "seat_no", "create_time", "id");
        List<TAmssPassSign> all = this.amssPassSignMapper.selectList(q);
        int round = bill.getRoundNo() == null ? 1 : bill.getRoundNo();
        List<TAmssPassSign> current = new ArrayList<>();
        List<TAmssPassSign> voided = new ArrayList<>();
        for (TAmssPassSign s : all) {
            if (s.getVoidFlag() != null && s.getVoidFlag() == 1) {
                voided.add(s);
            } else if (s.getRoundNo() != null && s.getRoundNo() == round) {
                current.add(s);
            }
        }
        v.setCurrentSigns(current);
        v.setVoidedSigns(voided);

        if (hasText(bill.getCertNo())) {
            QueryWrapper<TAmssPassCert> cq = new QueryWrapper<>();
            cq.eq("bill_id", bill.getId());
            v.setCert(this.amssPassCertMapper.selectOne(cq));
        }

        int node = bill.getNodeNo() == null ? 0 : bill.getNodeNo();
        v.setNodeName(PassBillView.NODE_NAMES[node]);
        int need = needOf(node);
        int signed = recount(bill);
        // 摆给人看的数同库里存的数出自同一回计算：回算与应到不一致就纠正库存
        if (bill.getSignCount() == null || bill.getSignCount() != signed) {
            TAmssPassBill fix = new TAmssPassBill();
            fix.setId(bill.getId());
            fix.setSignCount(signed);
            fix.setNeedCount(need);
            fix.setSignMode(node == NODE_REVIEW ? MODE_AND : MODE_OR);
            this.amssPassBillMapper.updateById(fix);
            bill.setSignCount(signed);
            bill.setNeedCount(need);
            bill.setSignMode(fix.getSignMode());
        }
        v.setMissingCount(Math.max(0, need - signed));
        boolean active = bill.getStatus() != null && bill.getStatus() == STATUS_RUNNING
                && (bill.getSealed() == null || bill.getSealed() == 0);
        v.setActive(active);
        if (!active) {
            if (bill.getStatus() != null && bill.getStatus() == STATUS_DENY) {
                v.setTerminalText("已写下不予出证，件就此止步");
            } else if (bill.getSealed() != null && bill.getSealed() == 1) {
                v.setTerminalText(hasText(bill.getCertNo()) ? "已用章并转出对外证文，各栏封死" : "章已落到底，各栏封死，待一键转证文");
            } else {
                v.setTerminalText("已打回");
            }
        }
        return v;
    }

    // ------------------------------------------------------------------
    // 关口判语
    // ------------------------------------------------------------------

    /**
     * 第二道：在册牌证同申领材料对着看，只对字号不对人；
     * 年度安全技术检验没做或没过，件压死在本道，此后再齐也走不动。
     */
    private boolean verifyPlateAndInspection(TAmssPassBill bill) {
        if (bill.getInspectBlock() != null && bill.getInspectBlock() == 1) {
            return false;
        }
        TAmssMachReg reg = findReg(bill.getPlateNo());
        if (reg == null) {
            // 字号对不上，哪怕机主姓名一模一样也不顶用
            return false;
        }
        boolean inspectOk = reg.getInspectYear() != null && bill.getIssueYear() != null
                && reg.getInspectYear().equals(bill.getIssueYear())
                && reg.getInspectResult() != null && reg.getInspectResult() == 1;
        if (!inspectOk) {
            TAmssPassBill dead = new TAmssPassBill();
            dead.setId(bill.getId());
            dead.setInspectBlock(1);
            this.amssPassBillMapper.updateById(dead);
            bill.setInspectBlock(1);
            return false;
        }
        return true;
    }

    /** 复核关第一位：这一年是否真在跨区作业队名册上、作业合同指向哪个省。 */
    private boolean verifyRosterAndContract(TAmssPassBill bill) {
        if (!hasText(bill.getTeamCardNo())) {
            return false;
        }
        QueryWrapper<TAmssTeamCard> q = new QueryWrapper<>();
        q.eq("bill_no", bill.getTeamCardNo().trim()).eq("del_flag", 0);
        TAmssTeamCard card = this.amssTeamCardMapper.selectOne(q);
        // 1 已核对才算在队；2 已压死、0 待核对都不算
        if (card == null || card.getStatus() == null || card.getStatus() != 1) {
            return false;
        }
        if (hasText(bill.getDestProvince())
                && (card.getContent() == null || !card.getContent().contains(bill.getDestProvince().trim()))) {
            // 作业合同指向的省与在队名录记要对不上
            return false;
        }
        return true;
    }

    /** 复核关第二位：牌证与检验在册情况同拟出证文合不合得拢。 */
    private boolean verifyDocMatch(TAmssPassBill bill) {
        TAmssMachReg reg = findReg(bill.getPlateNo());
        if (reg == null) {
            return false;
        }
        if (reg.getInspectYear() == null || !reg.getInspectYear().equals(bill.getIssueYear())
                || reg.getInspectResult() == null || reg.getInspectResult() != 1) {
            return false;
        }
        if (hasText(bill.getDriveCode()) && hasText(reg.getDriveCode())
                && !bill.getDriveCode().trim().equals(reg.getDriveCode().trim())) {
            return false;
        }
        if (hasText(bill.getCertText())
                && (!bill.getCertText().contains(bill.getPlateNo().trim())
                    || !bill.getCertText().contains(bill.getOwnerName().trim()))) {
            // 拟出证文里连本机字号、机主都拢不进去，合不了龙
            return false;
        }
        return true;
    }

    private TAmssMachReg findReg(String plateNo) {
        if (!hasText(plateNo)) {
            return null;
        }
        QueryWrapper<TAmssMachReg> q = new QueryWrapper<>();
        q.eq("plate_no", plateNo.trim()).eq("status", 0).eq("del_flag", 0);
        return this.amssMachRegMapper.selectOne(q);
    }

    // ------------------------------------------------------------------
    // 流水与计数
    // ------------------------------------------------------------------

    /** 件在当前关本手笔数够了就自己往前挪，顺手给新关摆上应到人数。 */
    private void settleNode(TAmssPassBill bill) {
        int node = bill.getNodeNo();
        int signed = recount(bill);
        bill.setSignCount(signed);
        if (signed < needOf(node)) {
            bill.setNeedCount(needOf(node));
            bill.setSignMode(node == NODE_REVIEW ? MODE_AND : MODE_OR);
            return;
        }
        if (node < NODE_SEAL) {
            int next = node + 1;
            bill.setNodeNo(next);
            bill.setNeedCount(needOf(next));
            bill.setSignMode(next == NODE_REVIEW ? MODE_AND : MODE_OR);
            bill.setSignCount(recount(bill));
            bill.setStatus(STATUS_RUNNING);
        }
    }

    /** 本关应到几双手，只由关位定，页面上没有格容人代填。 */
    private int needOf(int node) {
        return node == NODE_REVIEW ? 2 : 1;
    }

    /**
     * 本关已签数：件自己在流水里一笔一笔数出来——
     * 当前轮次、未勾销、核准名目，缺一笔就算这道没满。
     */
    private int recount(TAmssPassBill bill) {
        return activeSigns(bill, bill.getNodeNo()).size();
    }

    private List<TAmssPassSign> activeSigns(TAmssPassBill bill, int node) {
        QueryWrapper<TAmssPassSign> q = new QueryWrapper<>();
        q.eq("bill_id", bill.getId())
         .eq("round_no", bill.getRoundNo() == null ? 1 : bill.getRoundNo())
         .eq("node_no", node)
         .eq("void_flag", 0)
         .eq("sign_kind", KIND_APPROVE)
         .eq("del_flag", 0);
        return this.amssPassSignMapper.selectList(q);
    }

    private List<TAmssPassSign> activeSignsOfRound(TAmssPassBill bill) {
        QueryWrapper<TAmssPassSign> q = new QueryWrapper<>();
        q.eq("bill_id", bill.getId())
         .eq("round_no", bill.getRoundNo() == null ? 1 : bill.getRoundNo())
         .eq("void_flag", 0)
         .eq("del_flag", 0);
        return this.amssPassSignMapper.selectList(q);
    }

    private void writeSign(TAmssPassBill bill, int seat, String seatName, int kind,
                           String approver, String opinion) {
        TAmssPassSign s = new TAmssPassSign();
        s.setBillId(bill.getId());
        s.setRoundNo(bill.getRoundNo() == null ? 1 : bill.getRoundNo());
        s.setNodeNo(bill.getNodeNo());
        s.setSeatNo(seat);
        s.setSeatName(seatName);
        s.setSignKind(kind);
        s.setApprover(approver);
        s.setOpinion(opinion);
        s.setVoidFlag(0);
        s.setDelFlag(0);
        this.amssPassSignMapper.insert(s);
    }

    /** 取件并挡掉一切终局/封死后的动作：不予出证过后补不进一枚，章落封死动只能重起件。 */
    private TAmssPassBill lockBill(Long id) {
        if (id == null) {
            return null;
        }
        TAmssPassBill bill = this.amssPassBillMapper.selectById(id);
        if (bill == null || (bill.getDelFlag() != null && bill.getDelFlag() == 1)) {
            return null;
        }
        if (bill.getStatus() != null && bill.getStatus() == STATUS_DENY) {
            return null;
        }
        if (bill.getSealed() != null && bill.getSealed() == 1) {
            return null;
        }
        if (bill.getStatus() == null || bill.getStatus() != STATUS_RUNNING) {
            return null;
        }
        return bill;
    }

    private boolean hasText(String s) {
        return s != null && !s.trim().isEmpty();
    }
}
