import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertSame;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.atomic.AtomicLong;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.invocation.InvocationOnMock;

import com.fc.v2.common.exception.AmssFlowException;
import com.fc.v2.mapper.auto.TAmssPassBillMapper;
import com.fc.v2.mapper.auto.TAmssPassCertMapper;
import com.fc.v2.mapper.auto.TAmssPassSignMapper;
import com.fc.v2.model.auto.TAmssPassBill;
import com.fc.v2.model.auto.TAmssPassCert;
import com.fc.v2.model.auto.TAmssPassSign;
import com.fc.v2.model.custom.PassApproveForm;
import com.fc.v2.service.impl.TAmssPassBillServiceImpl;

/**
 * 跨区作业证四级核准流水单测：不起 Spring，用内存假 Mapper 验证服务层签法。
 */
public class TAmssPassBillFlowTest {

    private TAmssPassBillMapper billMapper;
    private TAmssPassSignMapper signMapper;
    private TAmssPassCertMapper certMapper;
    private TAmssPassBillServiceImpl service;

    private final Map<Long, TAmssPassBill> bills = new HashMap<>();
    private final Map<Long, TAmssPassSign> signs = new HashMap<>();
    private final Map<Long, TAmssPassCert> certs = new HashMap<>();
    private final AtomicLong ids = new AtomicLong(1000);

    @BeforeEach
    @SuppressWarnings("unchecked")
    public void setUp() {
        bills.clear();
        signs.clear();
        certs.clear();
        billMapper = mock(TAmssPassBillMapper.class);
        signMapper = mock(TAmssPassSignMapper.class);
        certMapper = mock(TAmssPassCertMapper.class);

        when(billMapper.insert(any())).thenAnswer(inv -> {
            TAmssPassBill b = inv.getArgument(0);
            if (b.getId() == null) {
                b.setId(ids.incrementAndGet());
            }
            bills.put(b.getId(), b);
            return 1;
        });
        when(billMapper.updateById(any())).thenAnswer(inv -> {
            TAmssPassBill b = inv.getArgument(0);
            bills.put(b.getId(), b);
            return 1;
        });
        when(billMapper.selectById(any())).thenAnswer(inv -> bills.get(((Number) inv.getArgument(0)).longValue()));
        when(billMapper.selectList(any())).thenAnswer(this::queryBills);

        when(signMapper.insert(any())).thenAnswer(inv -> {
            TAmssPassSign s = inv.getArgument(0);
            if (s.getId() == null) {
                s.setId(ids.incrementAndGet());
            }
            signs.put(s.getId(), s);
            return 1;
        });
        when(signMapper.selectOne(any())).thenAnswer(this::oneSign);
        when(signMapper.selectCount(any())).thenAnswer(this::countSigns);
        when(signMapper.selectList(any())).thenAnswer(this::listSigns);
        when(signMapper.update(any(), any())).thenAnswer(this::voidSigns);

        when(certMapper.insert(any())).thenAnswer(inv -> {
            TAmssPassCert c = inv.getArgument(0);
            if (c.getId() == null) {
                c.setId(ids.incrementAndGet());
            }
            certs.put(c.getId(), c);
            return 1;
        });
        when(certMapper.selectCount(any())).thenAnswer(inv -> {
            List<Object> p = params(inv);
            String owner = (String) p.get(0);
            int year = ((Number) p.get(1)).intValue();
            long n = certs.values().stream()
                    .filter(c -> owner.equals(c.getOwnerName()) && year == c.getIssueYear()
                            && (c.getDelFlag() == null || c.getDelFlag() == 0))
                    .count();
            return (int) n;
        });

        service = new TAmssPassBillServiceImpl();
        inject("amssPassBillMapper", billMapper);
        inject("amssPassSignMapper", signMapper);
        inject("amssPassCertMapper", certMapper);
    }

    private void inject(String field, Object value) {
        try {
            java.lang.reflect.Field f = TAmssPassBillServiceImpl.class.getDeclaredField(field);
            f.setAccessible(true);
            f.set(service, value);
        } catch (ReflectiveOperationException e) {
            throw new IllegalStateException(e);
        }
    }

    // ------------------------------------------------------------------
    // 第一道·受理
    // ------------------------------------------------------------------

    @Test
    public void receiveMissingOneOfFour_isStoppedAtDoor() {
        TAmssPassBill form = baseBillForm();
        form.setCertText(null);
        AmssFlowException ex = assertThrows(AmssFlowException.class, receiveEx(form));
        assertTrue(ex.getMessage().contains("四样少一样"));
        assertTrue(bills.isEmpty());
    }

    @Test
    public void receiveComplete_movesToVerifyNode() {
        TAmssPassBill bill = service.receive(baseBillForm());
        assertEquals(1, bill.getNodeNo(), "受理落账即压到核验关");
        assertEquals(1, bill.getNeedCount());
        assertEquals(0, bill.getSignCount());
        assertEquals(1, bill.getPendingSlots().size());
        assertEquals("VERIFY", bill.getPendingSlots().get(0));
        assertEquals(1, bill.getRoundNo());
    }

    // ------------------------------------------------------------------
    // 第二道·核验
    // ------------------------------------------------------------------

    @Test
    public void verifyRegNoMismatch_isStopped_evenOwnerNameSame() {
        TAmssPassBill bill = service.receive(baseBillForm());
        PassApproveForm f = form(bill.getId(), "VERIFY", "核验员甲");
        f.setRegNo("别的字号");
        f.setInspPassed(1);
        assertThrows(AmssFlowException.class, approveEx(bill.getId(), f));
    }

    @Test
    public void verifyInspectionNotPassed_pieceIsCrushedHere() {
        TAmssPassBill bill = service.receive(baseBillForm());
        PassApproveForm f = form(bill.getId(), "VERIFY", "核验员甲");
        f.setRegNo(bill.getRegNo());
        f.setInspPassed(0);
        assertThrows(AmssFlowException.class, approveEx(bill.getId(), f));
        // 件仍压在核验关，下一道看不见
        assertTrue(service.todoAtNode(2).isEmpty());
    }

    @Test
    public void verifyOk_entersDualNodeWithTwoHandsDue() {
        TAmssPassBill bill = passVerify(service.receive(baseBillForm()), "核验员甲");
        assertEquals(2, bill.getNodeNo());
        assertEquals(1, bill.getSignMode(), "第三道是两名点齐");
        assertEquals(2, bill.getNeedCount());
        assertEquals(0, bill.getSignCount(), "已签数由流水回算，初到第三道为0");
        assertTrue(bill.getPendingSlots().contains("TEAM_A"));
        assertTrue(bill.getPendingSlots().contains("CARD_B"));
    }

    // ------------------------------------------------------------------
    // 第三道·两名分管并排签
    // ------------------------------------------------------------------

    @Test
    public void dualOnlyOneSigned_pieceStaysAtNodeThree() {
        TAmssPassBill bill = passVerify(service.receive(baseBillForm()), "核验员甲");
        bill = signTeamA(bill, "名册分管A", "江原省");
        assertEquals(2, bill.getNodeNo(), "只到一位，件还停在这一道");
        assertEquals(1, bill.getSignCount());
        assertEquals(1, bill.getPendingSlots().size());
        assertEquals("CARD_B", bill.getPendingSlots().get(0));
        final Long dualBillId = bill.getId();
        assertNull(service.todoAtNode(3).stream().filter(b -> b.getId().equals(dualBillId)).findFirst().orElse(null),
                "够不着第四道");
    }

    @Test
    public void dualSameOfficerTwice_countsAsUnsigned() {
        TAmssPassBill bill = passVerify(service.receive(baseBillForm()), "核验员甲");
        bill = signTeamA(bill, "同一双手", "江原省");
        PassApproveForm card = form(bill.getId(), "CARD_B", "同一双手");
        assertThrows(AmssFlowException.class, approveEx(bill.getId(), card));
    }

    @Test
    public void dualSignOfficerOrderReversed_stillRejectsSameHand() {
        TAmssPassBill bill = passVerify(service.receive(baseBillForm()), "核验员甲");
        PassApproveForm card = form(bill.getId(), "CARD_B", "同一双手");
        bill = service.approve(bill.getId(), card);
        PassApproveForm team = form(bill.getId(), "TEAM_A", "同一双手");
        team.setRostered(1);
        team.setDestProvince("江原省");
        team.setValidFrom(d(2026, 5, 1));
        team.setValidTo(d(2026, 6, 1));
        assertThrows(AmssFlowException.class, approveEx(bill.getId(), team));
    }

    @Test
    public void dualTwoHandsAreTwoRows_evenSameInstant_neverMerged() {
        TAmssPassBill bill = passVerify(service.receive(baseBillForm()), "核验员甲");
        bill = signTeamA(bill, "名册分管A", "江原省");
        bill = signCardB(bill, "牌证分管B");
        assertEquals(3, bill.getNodeNo());
        final Long dualRowsBillId = bill.getId();
        long rows = signs.values().stream()
                .filter(s -> s.getBillId().equals(dualRowsBillId) && s.getRoundNo() == 1 && s.getNodeNo() == 2
                        && s.getVoided() == 0)
                .count();
        assertEquals(2, rows, "掐着同一分钟落完也是两笔，不并成一笔");
    }

    @Test
    public void cannotJumpAheadToDualBeforeVerify() {
        TAmssPassBill bill = service.receive(baseBillForm());
        assertThrows(AmssFlowException.class, approveEx(bill.getId(), form(bill.getId(), "TEAM_A", "名册分管A")));
    }

    @Test
    public void sameSlotTwice_isRejected() {
        TAmssPassBill bill = passVerify(service.receive(baseBillForm()), "核验员甲");
        bill = signTeamA(bill, "名册分管A", "江原省");
        assertThrows(AmssFlowException.class,
                approveEx(bill.getId(), form(bill.getId(), "TEAM_A", "名册分管A")));
    }

    // ------------------------------------------------------------------
    // 第四道·用章报送与市局
    // ------------------------------------------------------------------

    @Test
    public void cityFileBeforeSeal_isRejected() {
        TAmssPassBill bill = passDual(service.receive(baseBillForm()));
        assertThrows(AmssFlowException.class,
                approveEx(bill.getId(), form(bill.getId(), "CITY_FILE", "站员")));
    }

    @Test
    public void cityAckBeforeFiled_isRejected() {
        TAmssPassBill bill = passDual(service.receive(baseBillForm()));
        bill = service.approve(bill.getId(), form(bill.getId(), "SEAL", "站长"));
        assertThrows(AmssFlowException.class, ackEx(bill.getId(), "市局员"));
    }

    @Test
    public void fourNodesCompleted_sealsAllColumns() {
        TAmssPassBill bill = fullToCityAck(service.receive(baseBillForm()));
        assertEquals(3, bill.getNodeNo());
        assertEquals(1, bill.getStatus(), "已核讫");
        assertEquals(1, bill.getSealed());
        assertTrue(bill.getPendingSlots().isEmpty());
        // 章落到底，补不进一枚去
        assertThrows(AmssFlowException.class,
                approveEx(bill.getId(), form(bill.getId(), "SEAL", "再来一只手")));
    }

    // ------------------------------------------------------------------
    // 不予出证：任一在办关写下即终局
    // ------------------------------------------------------------------

    @Test
    public void rejectAtAnyNode_isTerminalAndSealed() {
        TAmssPassBill bill = signTeamA(passVerify(service.receive(baseBillForm()), "核验员甲"), "名册分管A", "江原省");
        PassApproveForm reject = form(bill.getId(), "REJECT", "牌证分管B");
        bill = service.approve(bill.getId(), reject);
        assertEquals(3, bill.getStatus());
        assertEquals(1, bill.getSealed());
        assertThrows(AmssFlowException.class,
                approveEx(bill.getId(), form(bill.getId(), "CARD_B", "牌证分管B")));
        assertThrows(AmssFlowException.class, ackEx(bill.getId(), "市局员"));
    }

    // ------------------------------------------------------------------
    // 市局打回：名目尽数勾销，轮次+1，从第二道重走
    // ------------------------------------------------------------------

    @Test
    public void cityBack_voidsAllNamesAndRestartsFromVerify() {
        TAmssPassBill bill = fullToFiled(service.receive(baseBillForm()));
        int signsBefore = signs.size();
        bill = service.cityBack(bill.getId(), "市局员", "表述不合规矩");

        assertEquals(2, bill.getRoundNo());
        assertEquals(1, bill.getNodeNo(), "从第二道重新往下攒");
        assertEquals(0, bill.getSignCount(), "新一轮计数从流水重新累");
        assertNull(bill.getDestProvince());
        assertNull(bill.getValidFrom());

        final Long backBillId = bill.getId();
        long voided = signs.values().stream().filter(s -> s.getBillId().equals(backBillId) && s.getVoided() == 1).count();
        long backRows = signs.values().stream()
                .filter(s -> s.getBillId().equals(backBillId) && "BACK".equals(s.getSlot()) && s.getVoided() == 0).count();
        assertEquals(signsBefore, voided, "先前各道名目尽数勾销");
        assertEquals(1, backRows, "勾掉的与后补的不摆在一处：打回笔另立一笔");

        // 新一轮可再走一遍，且第二道必须重新核验
        bill = passVerify(bill, "核验员乙");
        bill = signTeamA(bill, "名册分管A2", "河西省");
        bill = signCardB(bill, "牌证分管B2");
        assertEquals(3, bill.getNodeNo());
        TAmssPassBill viewed = service.selectTAmssPassBillById(bill.getId());
        assertEquals(2, viewed.getRoundNo());
        long liveRoundTwo = viewed.getTrace().stream()
                .filter(s -> s.getRoundNo() == 2 && s.getVoided() == 0 && !"BACK".equals(s.getSlot())).count();
        assertEquals(3, liveRoundTwo, "回捋与当日经办一屏：VERIFY/TEAM_A/CARD_B 三笔新账");
        long backInTrace = viewed.getTrace().stream()
                .filter(s -> "BACK".equals(s.getSlot()) && s.getRoundNo() == 2 && s.getVoided() == 0).count();
        assertEquals(1, backInTrace, "打回笔另立在新一轮留痕，与勾掉的账不摆在一处");
    }

    @Test
    public void cityBackBeforeFiled_isRejected() {
        TAmssPassBill bill = passDual(service.receive(baseBillForm()));
        assertThrows(AmssFlowException.class, backEx(bill.getId(), "市局员", null));
    }

    // ------------------------------------------------------------------
    // 一键转证文
    // ------------------------------------------------------------------

    @Test
    public void issueCertBeforeSeal_isRejected() {
        TAmssPassBill bill = passDual(service.receive(baseBillForm()));
        assertThrows(AmssFlowException.class, issueEx(bill.getId(), "发证员"));
    }

    @Test
    public void issueCert_carriesEveryFieldFromTheSameBill() {
        TAmssPassBill bill = fullToCityAck(service.receive(baseBillForm()));
        TAmssPassCert cert = service.issueCert(bill.getId(), "发证员");
        assertEquals(bill.getId(), cert.getBillId());
        assertEquals(bill.getOwnerName(), cert.getOwnerName());
        assertEquals(bill.getRegNo(), cert.getRegNo());
        assertEquals(bill.getDestProvince(), cert.getDestProvince());
        assertSame(bill.getValidFrom(), cert.getValidFrom());
        assertSame(bill.getValidTo(), cert.getValidTo());
        assertEquals(2026, cert.getIssueYear());
        assertNotNull(cert.getIssueTime());
        assertEquals(1, service.selectTAmssPassBillById(bill.getId()).getCertIssued());
    }

    @Test
    public void issueCertTwice_isBlocked() {
        TAmssPassBill bill = fullToCityAck(service.receive(baseBillForm()));
        service.issueCert(bill.getId(), "发证员");
        assertThrows(AmssFlowException.class, issueEx(bill.getId(), "发证员"));
    }

    @Test
    public void duplicateSecondCertSameOwnerSameYear_isBlocked() {
        // 第一件已发过证
        TAmssPassBill first = fullToCityAck(service.receive(baseBillForm()));
        service.issueCert(first.getId(), "发证员");
        // 同年另一件同机主，四道走满照拦
        TAmssPassBill secondForm = baseBillForm();
        secondForm.setRegNo("另一台机字号");
        TAmssPassBill second = fullToCityAck(service.receive(secondForm));
        AmssFlowException ex = assertThrows(AmssFlowException.class,
                issueEx(second.getId(), "发证员"));
        assertTrue(ex.getMessage().contains("重复抄发"));
    }

    // ------------------------------------------------------------------
    // 旧签法照旧留得住
    // ------------------------------------------------------------------

    @Test
    public void legacyApproveStillMovesNode() {
        TAmssPassBill bill = service.receive(baseBillForm());
        TAmssPassBill moved = service.approve(bill.getId(), "旧官人", "旧签法");
        assertEquals(bill.getNodeNo(), moved.getNodeNo());
    }

    // ------------------------------------------------------------------ helpers

    private org.junit.jupiter.api.function.Executable approveEx(Long id, PassApproveForm f) {
        return () -> service.approve(id, f);
    }

    private org.junit.jupiter.api.function.Executable ackEx(Long id, String officer) {
        return () -> service.cityAck(id, officer);
    }

    private org.junit.jupiter.api.function.Executable backEx(Long id, String officer, String opinion) {
        return () -> service.cityBack(id, officer, opinion);
    }

    private org.junit.jupiter.api.function.Executable issueEx(Long id, String officer) {
        return () -> service.issueCert(id, officer);
    }

    private org.junit.jupiter.api.function.Executable receiveEx(TAmssPassBill form) {
        return () -> service.receive(form);
    }

    private TAmssPassBill baseBillForm() {
        TAmssPassBill form = new TAmssPassBill();
        form.setOwnerName("麦农张三");
        form.setDriveCode("R1");
        form.setRegNo("赣11·收2026-007");
        form.setCertText("拟发跨区作业证一张，机驾联合收割机赴外省作业");
        form.setCreateBy("受理员");
        return form;
    }

    private PassApproveForm form(Long id, String slot, String officer) {
        PassApproveForm f = new PassApproveForm();
        f.setId(id);
        f.setSlot(slot);
        f.setOfficer(officer);
        return f;
    }

    private Date d(int year, int month, int day) {
        java.util.Calendar c = java.util.Calendar.getInstance();
        c.set(year, month - 1, day, 0, 0, 0);
        c.set(java.util.Calendar.MILLISECOND, 0);
        return c.getTime();
    }

    private TAmssPassBill passVerify(TAmssPassBill bill, String officer) {
        PassApproveForm f = form(bill.getId(), "VERIFY", officer);
        f.setRegNo(bill.getRegNo());
        f.setInspPassed(1);
        return service.approve(bill.getId(), f);
    }

    private TAmssPassBill signTeamA(TAmssPassBill bill, String officer, String province) {
        PassApproveForm f = form(bill.getId(), "TEAM_A", officer);
        f.setRostered(1);
        f.setDestProvince(province);
        f.setValidFrom(d(2026, 5, 20));
        f.setValidTo(d(2026, 7, 10));
        return service.approve(bill.getId(), f);
    }

    private TAmssPassBill signCardB(TAmssPassBill bill, String officer) {
        return service.approve(bill.getId(), form(bill.getId(), "CARD_B", officer));
    }

    private TAmssPassBill passDual(TAmssPassBill bill) {
        bill = passVerify(bill, "核验员甲");
        bill = signTeamA(bill, "名册分管A", "江原省");
        return signCardB(bill, "牌证分管B");
    }

    private TAmssPassBill fullToFiled(TAmssPassBill bill) {
        bill = passDual(bill);
        bill = service.approve(bill.getId(), form(bill.getId(), "SEAL", "站长"));
        return service.approve(bill.getId(), form(bill.getId(), "CITY_FILE", "站员"));
    }

    private TAmssPassBill fullToCityAck(TAmssPassBill bill) {
        bill = fullToFiled(bill);
        return service.cityAck(bill.getId(), "市局员");
    }

    private List<Object> params(InvocationOnMock inv) {
        return paramsOf(inv.getArgument(0));
    }

    private List<Object> paramsOf(Object w) {
        com.baomidou.mybatisplus.core.conditions.AbstractWrapper<?, ?, ?> aw = wrapper(w);
        aw.getSqlSegment(); // MP 3.4.2：渲染片段时才把参数物化为 MPGENVAL 键值
        Map<String, Object> pairs = aw.getParamNameValuePairs();
        List<String> keys = new ArrayList<>(pairs.keySet());
        keys.sort(java.util.Comparator.comparingInt(k -> Integer.parseInt(k.replaceAll("\\D", ""))));
        List<Object> ordered = new ArrayList<>();
        for (String k : keys) {
            ordered.add(pairs.get(k));
        }
        return ordered;
    }

    @SuppressWarnings("unchecked")
    private com.baomidou.mybatisplus.core.conditions.AbstractWrapper<?, ?, ?> wrapper(Object w) {
        return (com.baomidou.mybatisplus.core.conditions.AbstractWrapper<?, ?, ?>) w;
    }

    private TAmssPassSign oneSign(InvocationOnMock inv) {
        List<Object> p = params(inv);
        Long billId = ((Number) p.get(0)).longValue();
        int round = ((Number) p.get(1)).intValue();
        int node = ((Number) p.get(2)).intValue();
        String slot = (String) p.get(3);
        return signs.values().stream()
                .filter(s -> s.getBillId().equals(billId) && s.getRoundNo() == round && s.getNodeNo() == node
                        && slot.equals(s.getSlot()) && s.getVoided() == 0)
                .findFirst().orElse(null);
    }

    private Integer countSigns(InvocationOnMock inv) {
        List<Object> p = params(inv);
        Long billId = ((Number) p.get(0)).longValue();
        int round = ((Number) p.get(1)).intValue();
        int node = ((Number) p.get(2)).intValue();
        return (int) signs.values().stream()
                .filter(s -> s.getBillId().equals(billId) && s.getRoundNo() == round && s.getNodeNo() == node
                        && s.getVoided() == 0)
                .count();
    }

    private List<TAmssPassSign> listSigns(InvocationOnMock inv) {
        List<Object> p = params(inv);
        Long billId = ((Number) p.get(0)).longValue();
        List<TAmssPassSign> all = new ArrayList<>();
        for (TAmssPassSign s : signs.values()) {
            if (!s.getBillId().equals(billId)) {
                continue;
            }
            if (p.size() == 4 && s.getRoundNo() == ((Number) p.get(1)).intValue()
                    && s.getNodeNo() == ((Number) p.get(2)).intValue()
                    && s.getVoided() == ((Number) p.get(3)).intValue()) {
                all.add(s);
            } else if (p.size() == 1) {
                all.add(s);
            }
        }
        return all;
    }

    private Integer voidSigns(InvocationOnMock inv) {
        com.baomidou.mybatisplus.core.conditions.AbstractWrapper<?, ?, ?> uw = wrapper(inv.getArgument(1));
        uw.getSqlSet();   // SET 值先物化（MPGENVAL1）
        List<Object> list = paramsOf(inv.getArgument(1));
        Long billId = ((Number) list.get(1)).longValue();
        int round = ((Number) list.get(2)).intValue();
        String excludeSlot = (String) list.get(4);
        for (TAmssPassSign s : signs.values()) {
            if (s.getBillId().equals(billId) && s.getRoundNo() == round && s.getVoided() == 0
                    && !s.getSlot().equals(excludeSlot)) {
                s.setVoided(1);
            }
        }
        return 1;
    }

    private List<TAmssPassBill> queryBills(InvocationOnMock inv) {
        List<Object> p = params(inv);
        int node = ((Number) p.get(0)).intValue();
        List<TAmssPassBill> r = new ArrayList<>();
        for (TAmssPassBill b : bills.values()) {
            if (b.getNodeNo() == node && b.getStatus() == 0 && b.getSealed() == 0
                    && (b.getDelFlag() == null || b.getDelFlag() == 0)) {
                r.add(b);
            }
        }
        return r;
    }
}
