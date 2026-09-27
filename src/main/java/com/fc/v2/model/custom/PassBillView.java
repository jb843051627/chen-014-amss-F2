package com.fc.v2.model.custom;

import com.fc.v2.model.auto.TAmssPassBill;
import com.fc.v2.model.auto.TAmssPassCert;
import com.fc.v2.model.auto.TAmssPassSign;

import java.util.ArrayList;
import java.util.List;

/**
 * 核准单流转视图：压在哪一道、还差几双手、各道落过的名目，
 * 全由业务服务层从单据与签字流水同一回计算得出，页面不许先摆关口再凑数。
 *
 * @author fuce
 * @date 2026-09-27
 */
public class PassBillView {

    /** 关口名（受理/核验/复核/用章） */
    public static final String[] NODE_NAMES = {"受理", "核验", "复核", "用章"};

    /** 关口内位次名（0 为单人关；复核关 1 名册合同位、2 牌证证文合龙位） */
    public static final String[] SEAT_NAMES = {"", "名册合同核验", "牌证证文合龙"};

    /** 单据本体 */
    private TAmssPassBill bill;

    /** 本轮在账的签字名目（展示数与库存数同出此列） */
    private List<TAmssPassSign> currentSigns = new ArrayList<>();

    /** 历轮被勾销的名目（勾掉的与后补的不摆在一处，仅供回溯） */
    private List<TAmssPassSign> voidedSigns = new ArrayList<>();

    /** 已转出的对外证文（未出证为 null） */
    private TAmssPassCert cert;

    /** 当前关口名 */
    private String nodeName;

    /** 当前关还缺几双手 */
    private int missingCount;

    /** 该件是否还能往下签（在核且未封） */
    private boolean active;

    /** 终局情形说明（已核讫/不予出证/已封章） */
    private String terminalText;

    public TAmssPassBill getBill() {
        return bill;
    }

    public void setBill(TAmssPassBill bill) {
        this.bill = bill;
    }

    public List<TAmssPassSign> getCurrentSigns() {
        return currentSigns;
    }

    public void setCurrentSigns(List<TAmssPassSign> currentSigns) {
        this.currentSigns = currentSigns;
    }

    public List<TAmssPassSign> getVoidedSigns() {
        return voidedSigns;
    }

    public void setVoidedSigns(List<TAmssPassSign> voidedSigns) {
        this.voidedSigns = voidedSigns;
    }

    public TAmssPassCert getCert() {
        return cert;
    }

    public void setCert(TAmssPassCert cert) {
        this.cert = cert;
    }

    public String getNodeName() {
        return nodeName;
    }

    public void setNodeName(String nodeName) {
        this.nodeName = nodeName;
    }

    public int getMissingCount() {
        return missingCount;
    }

    public void setMissingCount(int missingCount) {
        this.missingCount = missingCount;
    }

    public boolean isActive() {
        return active;
    }

    public void setActive(boolean active) {
        this.active = active;
    }

    public String getTerminalText() {
        return terminalText;
    }

    public void setTerminalText(String terminalText) {
        this.terminalText = terminalText;
    }
}
