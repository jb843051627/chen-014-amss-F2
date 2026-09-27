package com.fc.v2.service;

import java.util.List;

import com.fc.v2.model.auto.TAmssPassBill;
import com.fc.v2.model.auto.TAmssPassCert;
import com.fc.v2.model.custom.PassApproveForm;

/**
 * 跨区作业证逐级核准单 Service接口（approval-chain 形状：多阶段签批，无增删改查入口）
 *
 * 四道关口：0受理 1核验 2两名分管并排签 3本站用章报市局存案。
 * 件能不能往下走只认本接口给出的结论；此刻压在哪一道、还差几双手，
 * 全凭返回单据上的 nodeNo/signCount/pendingSlots 摆上屏幕。
 *
 * @author fuce
 * @date 2026-09-14
 */
public interface ITAmssPassBillService {

    /** 按主键回查单据（附本层待办名目与流水回捋，均由流水一次回算） */
    TAmssPassBill selectTAmssPassBillById(Long id);

    /**
     * 旧签法：签批一票：返回更新后的单据；被拒返回 null。
     * 照旧留得住，新签法走 {@link #approve(Long, PassApproveForm)} 重载。
     */
    TAmssPassBill approve(Long id, String approver, String comment);

    /** 旧签法：否决：返回更新后的单据；被拒返回 null */
    TAmssPassBill reject(Long id, String approver, String comment);

    /** 旧签法：退回上一环节：返回更新后的单据；被拒返回 null */
    TAmssPassBill rollback(Long id, String comment);

    /**
     * 第一道·受理：机主、准驾代号、在册牌证字号、拟出证文写法四样少一样当场拦下。
     * 受理落账后件停在核验关之前。
     */
    TAmssPassBill receive(TAmssPassBill form);

    /**
     * 新签法（同名重载）：当前节点该出的手落一笔。
     * slot：VERIFY 核验 / TEAM_A 名册分管 / CARD_B 牌证分管 / SEAL 本站用章报送。
     * 第三道两名分管各判各的，两笔不并笔；只到一位件仍停在本道，替签按没签。
     * 抢步、越岗、对不上字号、检验未过、终局件补签一律抛 AmssFlowException。
     */
    TAmssPassBill approve(Long id, PassApproveForm form);

    /** 第四道·市局存案：章落到底，各栏封死，此后可一键转证文 */
    TAmssPassBill cityAck(Long id, String officer);

    /**
     * 第四道·市局打回：先前各道名目尽数勾销（勾掉的与后补的不摆在一处），
     * 轮次+1，从第二道（核验）重新往下攒。
     */
    TAmssPassBill cityBack(Long id, String officer, String opinion);

    /**
     * 一键转对外证文：机主/牌证字号/目的地省/起讫日子同出核准单一件，不要第二个人外抄。
     * 同一位机主当年第二条重复抄发的当场拦下。
     */
    TAmssPassCert issueCert(Long id, String officer);

    /** 节点待办：只有压在该节点的件才摆到该节点经手感官屏幕上 */
    List<TAmssPassBill> todoAtNode(Integer nodeNo);
}
