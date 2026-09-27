package com.fc.v2.amss;

import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.fc.v2.mapper.auto.TAmssPassBillMapper;
import com.fc.v2.model.auto.TAmssPassBill;
import com.fc.v2.service.ITAmssPassBillService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.boot.test.context.SpringBootTest;

import static org.junit.jupiter.api.Assertions.*;

/**
 * amss F2 跨区作业证逐级核准与核准结论转换成对外证文 · 功能点验收测试（approval-chain 形状：直调签批服务，用例自播种）
 * create 侧产物，仅质检使用，不交付执行模型。
 */
@SpringBootTest
public class AmssF2AcceptanceTest {

    /** 本测试类自己插入的单据编号前缀（@BeforeEach 只清这一批，不动其它数据） */
    private static final String PFX = "AC-";

    @javax.annotation.Resource
    private ITAmssPassBillService amssPassBillService;

    @javax.annotation.Resource
    private TAmssPassBillMapper amssPassBillMapper;

    @BeforeEach
    public void setUp() {
        amssPassBillMapper.delete(new QueryWrapper<TAmssPassBill>().likeRight("bill_no", PFX));
    }

    private TAmssPassBill mk(String no, int nodeNo, int signMode, int needCount, int signCount, int status) {
        TAmssPassBill r = new TAmssPassBill();
        r.setBillNo(no);
        r.setNodeNo(Integer.valueOf(nodeNo));
        r.setSignMode(Integer.valueOf(signMode));
        r.setNeedCount(Integer.valueOf(needCount));
        r.setSignCount(Integer.valueOf(signCount));
        r.setStatus(Integer.valueOf(status));
        return r;
    }

    /** 坑2 签署票数 */
    @Test
    public void trap2_sign_count_should_accumulate() {
        TAmssPassBill r = mk(PFX + "C2", 0, 1, 3, 0, 0);
        amssPassBillMapper.insert(r);
        TAmssPassBill after = amssPassBillService.approve(r.getId(), "张三", "同意");
        assertNotNull(after, "签批被拒");
        assertEquals(1, after.getSignCount().intValue(), "签署票数没有累加");
    }

    /** 坑5 退回清票 */
    @Test
    public void trap5_rollback_should_clear_votes() {
        TAmssPassBill r = mk(PFX + "R5", 1, 1, 3, 2, 0);
        amssPassBillMapper.insert(r);
        TAmssPassBill after = amssPassBillService.rollback(r.getId(), "退回补充材料");
        assertNotNull(after, "退回被拒");
        assertEquals(0, after.getNodeNo().intValue(), "退回环节不符合预期");
        assertEquals(0, after.getSignCount().intValue(), "退回后本轮签署没有清空");
    }

    /** 坑1 会签满员 */
    @Test
    public void trap1_and_sign_should_wait_for_all() {
        TAmssPassBill r = mk(PFX + "A1", 0, 1, 2, 0, 0);
        amssPassBillMapper.insert(r);
        TAmssPassBill after = amssPassBillService.approve(r.getId(), "张三", "同意");
        assertNotNull(after, "签批被拒");
        assertEquals(0, after.getNodeNo().intValue(), "会签未满不应推进环节");
    }

    /** 坑6 末环节处置 */
    @Test
    public void trap6_pass_only_after_last_node() {
        TAmssPassBill mid = mk(PFX + "B6A", 1, 0, 1, 0, 0);
        amssPassBillMapper.insert(mid);
        TAmssPassBill afterMid = amssPassBillService.approve(mid.getId(), "赵六", "同意");
        assertEquals(2, afterMid.getNodeNo().intValue(), "中间环节推进不符合预期");
        assertEquals(0, afterMid.getStatus().intValue(), "中间环节不应置为已通过");

        TAmssPassBill last = mk(PFX + "B6B", 2, 0, 1, 0, 0);
        amssPassBillMapper.insert(last);
        TAmssPassBill afterLast = amssPassBillService.approve(last.getId(), "赵六", "同意");
        assertEquals(2, afterLast.getNodeNo().intValue(), "末环节不应再往后越界");
        assertEquals(1, afterLast.getStatus().intValue(), "末环节之后应置为已通过");
    }

    /** 坑4 结案守卫 */
    @Test
    public void trap4_closed_bill_should_not_be_signed() {
        TAmssPassBill r = mk(PFX + "T4", 2, 0, 1, 0, 2);
        amssPassBillMapper.insert(r);
        assertNull(amssPassBillService.approve(r.getId(), "王五", "同意"), "已结案单据仍能签批");
    }

    /** 坑3 否决终止 */
    @Test
    public void trap3_reject_should_terminate() {
        TAmssPassBill r = mk(PFX + "V3", 1, 0, 1, 0, 0);
        amssPassBillMapper.insert(r);
        TAmssPassBill after = amssPassBillService.reject(r.getId(), "李四", "不同意");
        assertNotNull(after, "否决被拒");
        assertEquals(2, after.getStatus().intValue(), "否决之后单据没有终止");
    }
}
