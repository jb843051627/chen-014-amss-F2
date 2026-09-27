package com.fc.v2.service.impl;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Calendar;
import java.util.Date;
import java.util.List;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.core.conditions.update.UpdateWrapper;
import com.fc.v2.common.exception.AmssFlowException;
import com.fc.v2.mapper.auto.TAmssPassBillMapper;
import com.fc.v2.mapper.auto.TAmssPassCertMapper;
import com.fc.v2.mapper.auto.TAmssPassSignMapper;
import com.fc.v2.model.auto.TAmssPassBill;
import com.fc.v2.model.auto.TAmssPassCert;
import com.fc.v2.model.auto.TAmssPassSign;
import com.fc.v2.model.custom.PassApproveForm;
import com.fc.v2.service.ITAmssPassBillService;
import com.fc.v2.shiro.util.ShiroUtils;
import com.fc.v2.util.StringUtils;

/**
 * 跨区作业证逐级核准单 Service业务层处理（approval-chain 形状：多阶段签批）
 *
 * 四道关口：0受理 1核验 2两名分管并排签 3本站用章报市局存案。
 * 件只认节点：走到哪一节点，只有该节点该出的手落得下笔；上一道没走完，
 * 下一道在待办里连件都看不见。第三道两笔各记各的，屏幕数与库存数同出流水一回计算。
 *
 * @author fuce
 * @date 2026-09-14
 */
@Service
public class TAmssPassBillServiceImpl implements ITAmssPassBillService {

    private static final int MAX_NODE = 2;
    private static final int MODE_OR = 0;
    private static final int MODE_AND = 1;
    private static final int STATUS_RUNNING = 0;
    private static final int STATUS_PASS = 1;
    private static final int STATUS_VETO = 2;
    private static final int STATUS_REJECT = 3;

    /** 节点 0受理 1核验 2双签 3用章报市局存案 */
    private static final int NODE_ACCEPT = 0;
    private static final int NODE_VERIFY = 1;
    private static final int NODE_DUAL = 2;
    private static final int NODE_SEAL = 3;

    /** 流水名目 */
    private static final String SLOT_ACCEPT = "ACCEPT";
    private static final String SLOT_VERIFY = "VERIFY";
    private static final String SLOT_TEAM_A = "TEAM_A";
    private static final String SLOT_CARD_B = "CARD_B";
    private static final String SLOT_SEAL = "SEAL";
    private static final String SLOT_CITY_FILE = "CITY_FILE";
    private static final String SLOT_CITY_ACK = "CITY_ACK";
    private static final String SLOT_REJECT = "REJECT";
    private static final String SLOT_BACK = "BACK";

    private static final int VERDICT_PASS = 1;
    private static final int VERDICT_NO = 0;

    @javax.annotation.Resource
    private TAmssPassBillMapper amssPassBillMapper;

    @javax.annotation.Resource
    private TAmssPassSignMapper amssPassSignMapper;

    @javax.annotation.Resource
    private TAmssPassCertMapper amssPassCertMapper;

    @Override
    public TAmssPassBill selectTAmssPassBillById(Long id) {
        TAmssPassBill bill = this.amssPassBillMapper.selectById(id);
        if (bill == null) {
            return null;
        }
        return fillView(bill);
    }

    /**
     * 旧签法（照旧留得住）：签批一票，节点向前挪一格。
     * 新流程请走 approve(Long, PassApproveForm) 重载。
     */
    @Override
    public TAmssPassBill approve(Long id, String approver, String comment) {
        TAmssPassBill r = this.amssPassBillMapper.selectById(id);
        if (r == null || approver == null || approver.trim().isEmpty()) {
            return null;
        }
        r.setNodeNo(Integer.valueOf((r.getNodeNo() == null ? 0 : r.getNodeNo()) + 1));
        r.setStatus(Integer.valueOf(r.getNodeNo() >= MAX_NODE ? STATUS_PASS : STATUS_RUNNING));
        this.amssPassBillMapper.updateById(r);
        return r;
    }

    @Override
    public TAmssPassBill reject(Long id, String approver, String comment) {
        TAmssPassBill r = this.amssPassBillMapper.selectById(id);
        if (r == null) {
            return null;
        }
        this.amssPassBillMapper.updateById(r);
        return r;
    }

    @Override
    public TAmssPassBill rollback(Long id, String comment) {
        TAmssPassBill r = this.amssPassBillMapper.selectById(id);
        if (r == null) {
            return null;
        }
        int node = r.getNodeNo() == null ? 0 : r.getNodeNo();
        r.setNodeNo(Integer.valueOf(Math.max(0, node - 1)));
        this.amssPassBillMapper.updateById(r);
        return r;
    }

    /**
     * 第一道·受理：四样少一样当场拦在门外；受理笔落账，件随即压到核验关。
     */
    @Override
    @Transactional(rollbackFor = Exception.class)
    public TAmssPassBill receive(TAmssPassBill form) {
        if (form == null) {
            throw new AmssFlowException("申领件不存在，不予受理");
        }
        if (StringUtils.isEmpty(form.getOwnerName()) || StringUtils.isEmpty(form.getDriveCode())
                || StringUtils.isEmpty(form.getRegNo()) || StringUtils.isEmpty(form.getCertText())) {
            throw new AmssFlowException("机主、准驾代号、在册牌证字号、拟出证文写法四样少一样，当场不予受理");
        }
        TAmssPassBill bill = new TAmssPassBill();
        bill.setOwnerName(form.getOwnerName().trim());
        bill.setDriveCode(form.getDriveCode().trim());
        bill.setRegNo(form.getRegNo().trim());
        bill.setCertText(form.getCertText().trim());
        bill.setNodeNo(NODE_ACCEPT);
        bill.setSignMode(MODE_OR);
        bill.setNeedCount(1);
        bill.setSignCount(0);
        bill.setStatus(STATUS_RUNNING);
        bill.setRoundNo(1);
        bill.setSealed(0);
        bill.setCertIssued(0);
        bill.setDelFlag(0);
        this.amssPassBillMapper.insert(bill);
        bill.setBillNo("HZ" + year() + bill.getId());
        writeSign(bill, SLOT_ACCEPT, resolveOfficer(form.getCreateBy()), VERDICT_PASS, "受理：四要件齐备");
        // 头一道受理落账即走完，件压到第二道核验关
        advanceTo(bill, NODE_VERIFY);
        return fillView(bill);
    }

    /**
     * 新签法（同名重载）：当前节点该出的手落一笔；REJECT 为任一在办关写下不予出证。
     */
    @Override
    @Transactional(rollbackFor = Exception.class)
    public TAmssPassBill approve(Long id, PassApproveForm form) {
        if (form == null || StringUtils.isEmpty(form.getSlot())) {
            throw new AmssFlowException("签批表单缺名目，落不了笔");
        }
        TAmssPassBill bill = loadLiving(id);
        String slot = form.getSlot().trim();
        String officer = resolveOfficer(form.getOfficer());

        if (SLOT_REJECT.equals(slot)) {
            return writeReject(bill, officer, form.getOpinion());
        }
        int node = bill.getNodeNo();
        if (SLOT_VERIFY.equals(slot)) {
            if (node != NODE_VERIFY) {
                throw new AmssFlowException("核验手只在核验关落得下笔，件此刻压在第" + node + "道");
            }
            doVerify(bill, form, officer);
        } else if (SLOT_TEAM_A.equals(slot) || SLOT_CARD_B.equals(slot)) {
            if (node != NODE_DUAL) {
                throw new AmssFlowException("两位分管的手只在第三道落得下笔，想抢步也不行");
            }
            doDualSign(bill, slot, form, officer);
        } else if (SLOT_SEAL.equals(slot) || SLOT_CITY_FILE.equals(slot)) {
            if (node != NODE_SEAL) {
                throw new AmssFlowException("用章报送的手只在第四道落得下笔");
            }
            doSealFile(bill, slot, form, officer);
        } else {
            throw new AmssFlowException("名目[" + slot + "]不在当前节点该出的几双手里");
        }
        return fillView(bill);
    }

    /** 第二道·核验：只对字号不对人；年度检验没做或没过，件压死在这一道 */
    private void doVerify(TAmssPassBill bill, PassApproveForm form, String officer) {
        ensureSlotAbsent(bill, SLOT_VERIFY);
        if (StringUtils.isEmpty(form.getRegNo()) || !form.getRegNo().trim().equals(bill.getRegNo())) {
            throw new AmssFlowException("在册牌证字号对不上：这一关只对字号，机主姓名一模一样也不顶用");
        }
        if (form.getInspPassed() == null || form.getInspPassed() != 1) {
            throw new AmssFlowException("本机年度安全技术检验未做或未过，件压死在核验关，往后再齐也走不动");
        }
        bill.setInspPassed(1);
        writeSign(bill, SLOT_VERIFY, officer, VERDICT_PASS,
                StringUtils.isNotEmpty(form.getOpinion()) ? form.getOpinion() : "字号对得上，年度检验已过");
        advanceTo(bill, NODE_DUAL);
    }

    /** 第三道·两名分管并排签：名册合同一位、牌证证文一位，各判各的，互不接手 */
    private void doDualSign(TAmssPassBill bill, String slot, PassApproveForm form, String officer) {
        ensureSlotAbsent(bill, slot);
        // 不论哪位先落，后落的都不能是同一双手：替签的一概当没签
        String otherSlot = SLOT_TEAM_A.equals(slot) ? SLOT_CARD_B : SLOT_TEAM_A;
        TAmssPassSign other = findSign(bill, otherSlot);
        if (other != null && officer.equals(other.getOfficer())) {
            throw new AmssFlowException("两名分管不能是同一双手，替签的一概当没签");
        }
        if (SLOT_TEAM_A.equals(slot)) {
            if (form.getRostered() == null || form.getRostered() != 1) {
                throw new AmssFlowException("机主今年不在跨区作业队名册上，名册分管这一笔落不下去");
            }
            if (StringUtils.isEmpty(form.getDestProvince()) || form.getValidFrom() == null
                    || form.getValidTo() == null || form.getValidFrom().after(form.getValidTo())) {
                throw new AmssFlowException("作业合同指向省与起讫日子没核齐，名册分管这一笔落不下去");
            }
            bill.setDestProvince(form.getDestProvince().trim());
            bill.setValidFrom(form.getValidFrom());
            bill.setValidTo(form.getValidTo());
            writeSign(bill, SLOT_TEAM_A, officer, VERDICT_PASS,
                    StringUtils.isNotEmpty(form.getOpinion()) ? form.getOpinion()
                            : "名册在列，合同指向" + bill.getDestProvince());
        } else {
            if (bill.getInspPassed() == null || bill.getInspPassed() != 1) {
                throw new AmssFlowException("牌证与检验在册情况尚未核得拢，这一笔落不下去");
            }
            if (StringUtils.isEmpty(bill.getCertText())) {
                throw new AmssFlowException("拟出证文写法空缺，合不拢");
            }
            writeSign(bill, SLOT_CARD_B, officer, VERDICT_PASS,
                    StringUtils.isNotEmpty(form.getOpinion()) ? form.getOpinion() : "牌证、检验在册情况与拟出证文合得拢");
        }
        // 两笔各记各的；凑齐没有不由人手填，由件自己在流水里数出来
        if (countRoundSigns(bill, NODE_DUAL) >= nodeSlots(NODE_DUAL).size()) {
            advanceTo(bill, NODE_SEAL);
        } else {
            this.amssPassBillMapper.updateById(bill);
        }
    }

    /** 第四道·本站用章、报市局存一份：两只手一前一后，都过了才轮得到市局下结论 */
    private void doSealFile(TAmssPassBill bill, String slot, PassApproveForm form, String officer) {
        ensureSlotAbsent(bill, slot);
        if (SLOT_SEAL.equals(slot)) {
            writeSign(bill, SLOT_SEAL, officer, VERDICT_PASS,
                    StringUtils.isNotEmpty(form.getOpinion()) ? form.getOpinion() : "本站用章");
        } else {
            if (findSign(bill, SLOT_SEAL) == null) {
                throw new AmssFlowException("章还没落，报市局这一步抢不得");
            }
            writeSign(bill, SLOT_CITY_FILE, officer, VERDICT_PASS,
                    StringUtils.isNotEmpty(form.getOpinion()) ? form.getOpinion() : "报市局存一份");
        }
        this.amssPassBillMapper.updateById(bill);
    }

    /** 第四道·市局存案：章一落到底，各栏随即封死 */
    @Override
    @Transactional(rollbackFor = Exception.class)
    public TAmssPassBill cityAck(Long id, String officer) {
        TAmssPassBill bill = loadLiving(id);
        if (bill.getNodeNo() != NODE_SEAL) {
            throw new AmssFlowException("件还没走到第四道，市局无从存案");
        }
        if (findSign(bill, SLOT_CITY_FILE) == null) {
            throw new AmssFlowException("本站还没报上来，市局无件可存");
        }
        ensureSlotAbsent(bill, SLOT_CITY_ACK);
        writeSign(bill, SLOT_CITY_ACK, resolveOfficer(officer), VERDICT_PASS, "市局存案");
        bill.setStatus(STATUS_PASS);
        bill.setSealed(1);
        this.amssPassBillMapper.updateById(bill);
        return fillView(bill);
    }

    /** 第四道·市局打回：先前各道名目尽数勾销，轮次+1，从第二道重新往下攒 */
    @Override
    @Transactional(rollbackFor = Exception.class)
    public TAmssPassBill cityBack(Long id, String officer, String opinion) {
        TAmssPassBill bill = loadLiving(id);
        if (bill.getNodeNo() != NODE_SEAL || findSign(bill, SLOT_CITY_FILE) == null) {
            throw new AmssFlowException("件没报到市局，谈不上打回");
        }
        // 勾掉的与后补的不摆在一处：上一轮各道准签先尽数勾销（打回笔本身留痕，不勾）
        this.amssPassSignMapper.update(null, new UpdateWrapper<TAmssPassSign>()
                .eq("bill_id", bill.getId())
                .eq("round_no", bill.getRoundNo())
                .eq("voided", 0)
                .ne("slot", SLOT_BACK)
                .set("voided", 1));
        // 打回笔落在第四道发生地；随后轮次+1，从第二道（核验）重新往下攒
        bill.setRoundNo(bill.getRoundNo() + 1);
        writeSign(bill, SLOT_BACK, resolveOfficer(officer), VERDICT_NO,
                StringUtils.isNotEmpty(opinion) ? opinion : "市局以表述不合规矩打回");
        // 核验、合同上核得的东西一并清掉重核
        bill.setInspPassed(null);
        bill.setDestProvince(null);
        bill.setValidFrom(null);
        bill.setValidTo(null);
        bill.setStatus(STATUS_RUNNING);
        advanceTo(bill, NODE_VERIFY);
        return fillView(bill);
    }

    /** 任一在办关写下不予出证：件就此止步，过后谁也补不进一枚去 */
    private TAmssPassBill writeReject(TAmssPassBill bill, String officer, String opinion) {
        writeSign(bill, SLOT_REJECT, officer, VERDICT_NO,
                StringUtils.isNotEmpty(opinion) ? opinion : "不予出证");
        bill.setStatus(STATUS_REJECT);
        bill.setSealed(1);
        this.amssPassBillMapper.updateById(bill);
        return fillView(bill);
    }

    /**
     * 一键转对外证文：字段同出核准单一件；同机主当年第二条当场拦下。
     */
    @Override
    @Transactional(rollbackFor = Exception.class)
    public TAmssPassCert issueCert(Long id, String officer) {
        TAmssPassBill bill = this.amssPassBillMapper.selectById(id);
        if (bill == null || (bill.getDelFlag() != null && bill.getDelFlag() == 1)) {
            throw new AmssFlowException("核准单不存在");
        }
        if (bill.getStatus() == null || bill.getStatus() != STATUS_PASS || bill.getSealed() == null
                || bill.getSealed() != 1) {
            throw new AmssFlowException("四道没走满、章没落定，证文转不出去");
        }
        if (bill.getCertIssued() != null && bill.getCertIssued() == 1) {
            throw new AmssFlowException("这件的证文已经转过，不得再抄一条");
        }
        if (StringUtils.isEmpty(bill.getOwnerName()) || StringUtils.isEmpty(bill.getRegNo())
                || StringUtils.isEmpty(bill.getDestProvince()) || bill.getValidFrom() == null
                || bill.getValidTo() == null) {
            throw new AmssFlowException("机主、牌证字号、目的地省、起讫日子有缺，证文转不出去");
        }
        int issueYear = yearOf(bill.getValidFrom());
        Integer dup = this.amssPassCertMapper.selectCount(new QueryWrapper<TAmssPassCert>()
                .eq("owner_name", bill.getOwnerName())
                .eq("issue_year", issueYear)
                .eq("del_flag", 0));
        if (dup != null && dup > 0) {
            throw new AmssFlowException("同一位机主" + issueYear + "年已有证文，第二条重复抄发当场拦下");
        }
        TAmssPassCert cert = TAmssPassCert.fromBill(bill, "KQ" + issueYear + bill.getId());
        this.amssPassCertMapper.insert(cert);
        bill.setCertIssued(1);
        this.amssPassBillMapper.updateById(bill);
        return cert;
    }

    @Override
    public List<TAmssPassBill> todoAtNode(Integer nodeNo) {
        if (nodeNo == null || nodeNo < NODE_VERIFY || nodeNo > NODE_SEAL) {
            return new ArrayList<>();
        }
        List<TAmssPassBill> list = this.amssPassBillMapper.selectList(new QueryWrapper<TAmssPassBill>()
                .eq("node_no", nodeNo)
                .eq("status", STATUS_RUNNING)
                .eq("sealed", 0)
                .eq("del_flag", 0)
                .orderByAsc("create_time"));
        for (TAmssPassBill bill : list) {
            fillView(bill);
        }
        return list;
    }

    // ---------------------------------------------------------------------
    // 内里规矩
    // ---------------------------------------------------------------------

    /** 取出一件还在办的件；封了档（章落/不予）的，谁也补不进一枚去 */
    private TAmssPassBill loadLiving(Long id) {
        TAmssPassBill bill = this.amssPassBillMapper.selectById(id);
        if (bill == null || (bill.getDelFlag() != null && bill.getDelFlag() == 1)) {
            throw new AmssFlowException("核准单不存在");
        }
        if (bill.getSealed() != null && bill.getSealed() == 1) {
            throw new AmssFlowException("这件各栏已经封死（章落或不予出证），要动只能重新起件");
        }
        return bill;
    }

    private void writeSign(TAmssPassBill bill, String slot, String officer, int verdict, String opinion) {
        TAmssPassSign sign = new TAmssPassSign();
        sign.setBillId(bill.getId());
        sign.setRoundNo(bill.getRoundNo());
        sign.setNodeNo(bill.getNodeNo());
        sign.setSlot(slot);
        sign.setOfficer(officer);
        sign.setVerdict(verdict);
        sign.setVoided(0);
        sign.setOpinion(opinion);
        sign.setSignTime(new Date());
        sign.setDelFlag(0);
        this.amssPassSignMapper.insert(sign);
        bill.setLastSlot(slot);
        // 库里存的已签数也由流水回算，与摆给人看的数同出一回计算；打回笔不算新节点的签
        if (!SLOT_BACK.equals(slot)) {
            bill.setSignCount(countRoundSigns(bill, bill.getNodeNo()));
        }
    }

    private void ensureSlotAbsent(TAmssPassBill bill, String slot) {
        if (findSign(bill, slot) != null) {
            throw new AmssFlowException("名目[" + slot + "]本轮已经落过一笔，同一只手不能再落第二回");
        }
    }

    private TAmssPassSign findSign(TAmssPassBill bill, String slot) {
        return this.amssPassSignMapper.selectOne(new QueryWrapper<TAmssPassSign>()
                .eq("bill_id", bill.getId())
                .eq("round_no", bill.getRoundNo())
                .eq("node_no", bill.getNodeNo())
                .eq("slot", slot)
                .eq("voided", 0)
                .last("limit 1"));
    }

    private int countRoundSigns(TAmssPassBill bill, int node) {
        Integer n = this.amssPassSignMapper.selectCount(new QueryWrapper<TAmssPassSign>()
                .eq("bill_id", bill.getId())
                .eq("round_no", bill.getRoundNo())
                .eq("node_no", node)
                .eq("voided", 0));
        return n == null ? 0 : n;
    }

    /** 节点向前挪，并按新节点重置同层核准办法与应签数 */
    private void advanceTo(TAmssPassBill bill, int node) {
        bill.setNodeNo(node);
        bill.setSignMode(node == NODE_DUAL ? MODE_AND : MODE_OR);
        bill.setNeedCount(nodeSlots(node).size());
        bill.setSignCount(0);
        this.amssPassBillMapper.updateById(bill);
    }

    /**
     * 屏幕数与库存数同出一回计算：已签数、还差哪几双手、流水回捋都从这一遍流水查询里摆出来。
     */
    private TAmssPassBill fillView(TAmssPassBill bill) {
        int node = bill.getNodeNo() == null ? NODE_ACCEPT : bill.getNodeNo();
        List<TAmssPassSign> roundSigns = this.amssPassSignMapper.selectList(new QueryWrapper<TAmssPassSign>()
                .eq("bill_id", bill.getId())
                .eq("round_no", bill.getRoundNo())
                .eq("node_no", node)
                .eq("voided", 0));
        List<String> signed = new ArrayList<>();
        for (TAmssPassSign s : roundSigns) {
            signed.add(s.getSlot());
        }
        List<String> pending = new ArrayList<>();
        for (String slot : nodeSlots(node)) {
            if (!signed.contains(slot)) {
                pending.add(slot);
            }
        }
        bill.setSignCount(roundSigns.size());
        bill.setNeedCount(nodeSlots(node).size());
        bill.setPendingSlots(pending);
        // 各道落过的名目一道一道往回捋：勾销的也留着，标 voided，与当日经办一屏对得上号
        bill.setTrace(this.amssPassSignMapper.selectList(new QueryWrapper<TAmssPassSign>()
                .eq("bill_id", bill.getId())
                .orderByAsc("round_no")
                .orderByAsc("sign_time")
                .orderByAsc("id")));
        return bill;
    }

    /** 每个节点该出哪几双手；屏幕上没有第二个格容人代填 */
    private List<String> nodeSlots(int node) {
        switch (node) {
            case NODE_ACCEPT:
                return Arrays.asList(SLOT_ACCEPT);
            case NODE_VERIFY:
                return Arrays.asList(SLOT_VERIFY);
            case NODE_DUAL:
                return Arrays.asList(SLOT_TEAM_A, SLOT_CARD_B);
            case NODE_SEAL:
                return Arrays.asList(SLOT_SEAL, SLOT_CITY_FILE, SLOT_CITY_ACK);
            default:
                return new ArrayList<>();
        }
    }

    private String resolveOfficer(String officer) {
        if (StringUtils.isNotEmpty(officer)) {
            return officer.trim();
        }
        return ShiroUtils.getLoginName();
    }

    private int year() {
        return yearOf(new Date());
    }

    private int yearOf(Date date) {
        Calendar cal = Calendar.getInstance();
        cal.setTime(date);
        return cal.get(Calendar.YEAR);
    }
}
