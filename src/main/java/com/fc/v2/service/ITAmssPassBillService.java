package com.fc.v2.service;

import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.fc.v2.model.auto.TAmssPassBill;
import com.fc.v2.model.auto.TAmssPassCert;
import com.fc.v2.model.custom.PassBillView;

import java.util.List;

/**
 * 跨区作业证逐级核准单 Service接口（approval-chain 形状：四道关口逐级签批）
 *
 * 关口：0受理 1核验 2复核(两名点齐) 3用章；流转只认节点，
 * 页面所需的“压在哪一道、还差几双手”一律由 {@link #view(Long)} 的返回给出。
 *
 * @author fuce
 * @date 2026-09-14
 */
public interface ITAmssPassBillService {

    /** 按主键回查单据 */
    TAmssPassBill selectTAmssPassBillById(Long id);

    /** 台账查询 */
    List<TAmssPassBill> selectPassBillList(QueryWrapper<TAmssPassBill> queryWrapper);

    /**
     * 头道受理：机主、准驾代号、在册牌证字号、拟出证文四样少一样当场拦在门外。
     * 受理名目随件落一笔，件随即挪到核验关；拦下返回 null。
     */
    PassBillView receive(TAmssPassBill form);

    /**
     * 签批一票（单人关：受理/核验/用章沿用的旧签法）：返回更新后的单据；被拒返回 null。
     * 第三道两名点齐关不接受本签法，须走带位次的重载。
     */
    TAmssPassBill approve(Long id, String approver, String comment);

    /**
     * 同一名下新增的签法（重载）：第三道两位分管各判各的，
     * seatNo=1 名册合同位，seatNo=2 牌证证文合龙位；替签当没签。
     */
    TAmssPassBill approve(Long id, String approver, Integer seatNo, String comment);

    /** 否决（在本关写下不予出证，件就此终局）：返回更新后的单据；被拒返回 null */
    TAmssPassBill reject(Long id, String approver, String comment);

    /**
     * 市局把件打回：先前各道名目尽数勾销（不删行），轮次加一，
     * 从第二道（核验）重新往下攒；未报到用章关或已出证的件打不得。
     */
    TAmssPassBill rollback(Long id, String comment);

    /**
     * 四道走满后一键转对外证文：机主、牌证字号、目的地省、起讫日子自核准单同件带出；
     * 同机主同年度第二张当场拦，返回 null。
     */
    TAmssPassCert issueCert(Long id, String certNo);

    /**
     * 翻开一件：当前关口、本关应到/已到/还差几手、各道落过的名目（含历轮勾销）、
     * 已转证文，全部由单据与签字流水同一回计算摆出来，供当日经办屏与事后回溯同屏对号。
     */
    PassBillView view(Long id);
}
