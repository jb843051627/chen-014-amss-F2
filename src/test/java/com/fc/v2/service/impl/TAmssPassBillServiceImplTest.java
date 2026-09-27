package com.fc.v2.service.impl;

import com.baomidou.mybatisplus.annotation.TableField;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.core.conditions.Wrapper;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.fc.v2.model.auto.TAmssMachReg;
import com.fc.v2.model.auto.TAmssPassBill;
import com.fc.v2.model.auto.TAmssPassCert;
import com.fc.v2.model.auto.TAmssPassSign;
import com.fc.v2.model.auto.TAmssTeamCard;
import com.fc.v2.model.custom.PassBillView;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.test.util.ReflectionTestUtils;

import java.io.Serializable;
import java.lang.reflect.Field;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Date;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.atomic.AtomicLong;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import static org.junit.jupiter.api.Assertions.*;

/**
 * 四道关口规则单测：用内存假 Mapper 兜住 MyBatis-Plus，不起 Spring、不连库。
 */
class TAmssPassBillServiceImplTest {

    private TAmssPassBillServiceImpl svc;
    private FakeBillMapper bills;
    private FakeSignMapper signs;
    private FakeCertMapper certs;
    private FakeRegMapper regs;
    private FakeTeamMapper teams;

    private final SimpleDateFormat ymd = new SimpleDateFormat("yyyy-MM-dd");

    @BeforeEach
    void setUp() {
        svc = new TAmssPassBillServiceImpl();
        bills = new FakeBillMapper();
        signs = new FakeSignMapper();
        certs = new FakeCertMapper();
        regs = new FakeRegMapper();
        teams = new FakeTeamMapper();
        ReflectionTestUtils.setField(svc, "amssPassBillMapper", bills);
        ReflectionTestUtils.setField(svc, "amssPassSignMapper", signs);
        ReflectionTestUtils.setField(svc, "amssPassCertMapper", certs);
        ReflectionTestUtils.setField(svc, "amssMachRegMapper", regs);
        ReflectionTestUtils.setField(svc, "amssTeamCardMapper", teams);

        regs.insert(reg(100L, "赣F·09联收", "周麦收", "R", 2026, 1));
        // 同名机主挂在别的字号上，且年检不合格：用来证明核验只对字号不对人
        regs.insert(reg(102L, "赣F·11联收", "周麦收", "R", 2025, 0));
        regs.insert(reg(101L, "赣F·10联收", "吴耕田", "R", 2025, 0));
        teams.insert(team("JD2026001", 1, "临川县丰收跨区作业队2026年三夏在队名录·目的地省：河南省"));
        teams.insert(team("JD2026002", 2, "名录核验未过"));
    }

    // ------------------------------------------------------------------

    @Test
    @DisplayName("受理：四样少一样当场拦在门外，库里一件不留")
    void receive_missingElement_rejected() {
        TAmssPassBill form = baseForm("周麦收", "赣F·09联收");
        form.setCertText(null);
        assertNull(svc.receive(form));
        assertEquals(0, bills.rows.size());
        assertEquals(0, signs.rows.size());

        form = baseForm("周麦收", "赣F·09联收");
        form.setOwnerName("   ");
        assertNull(svc.receive(form));
    }

    @Test
    @DisplayName("满路四道：受理→字号核验→两名点齐→用章，节点逐级挪、缺口数逐笔回算")
    void happyPath_fourGates() throws Exception {
        PassBillView v = svc.receive(baseForm("周麦收", "赣F·09联收"));
        assertNotNull(v);
        Long id = v.getBill().getId();
        assertEquals(1, v.getBill().getNodeNo(), "受理一笔落完，件挪到核验关");
        assertEquals(1, v.getMissingCount(), "核验关尚无人签，差一双手");

        assertNotNull(svc.approve(id, "核验员甲", "字号与年检对得上"));
        v = svc.view(id);
        assertEquals(2, v.getBill().getNodeNo());
        assertEquals(2, v.getBill().getNeedCount());
        assertEquals(2, v.getMissingCount(), "复核关两名点齐，差两双手");

        assertNull(svc.approve(id, "核验员甲", "旧签法在复核关无处落"),
                "第三道不接受旧的单人签法");

        assertNotNull(svc.approve(id, "名册官乙", 1, "在队且合同指向河南"));
        v = svc.view(id);
        assertEquals(2, v.getBill().getNodeNo(), "只到一位，件还停在复核关");
        assertEquals(1, v.getMissingCount());

        assertNotNull(svc.approve(id, "合龙官丙", 2, "在册情况同证文合拢"));
        v = svc.view(id);
        assertEquals(3, v.getBill().getNodeNo(), "两笔点齐才够得着用章关");
        assertEquals(1, v.getMissingCount());

        TAmssPassBill sealed = svc.approve(id, "站长丁", "本站用章报市局");
        assertNotNull(sealed);
        assertEquals(1, sealed.getSealed().intValue());
        assertEquals(1, sealed.getStatus().intValue());
        v = svc.view(id);
        assertFalse(v.isActive());
        assertTrue(v.getTerminalText().contains("封死"));
    }

    @Test
    @DisplayName("核验只对字号不对人：在册姓名同申领人不同，只要字号与年检合格一样过")
    void verify_matchesPlateOnlyNotName() {
        TAmssPassBill form = baseForm("李插秧", "赣F·09联收");
        form.setCertText("持赣F·09联收往河南收麦，机主李插秧。");
        Long id = svc.receive(form).getBill().getId();
        assertNotNull(svc.approve(id, "核验员甲", null));
        assertNotNull(svc.approve(id, "名册官乙", 1, null));
        assertNotNull(svc.approve(id, "合龙官丙", 2, null));
        assertEquals(3, svc.selectTAmssPassBillById(id).getNodeNo().intValue());
    }

    @Test
    @DisplayName("年检未做/未过：件一次压死在核验关，过后再齐也走不动")
    void inspectFailed_blocksForever() {
        TAmssPassBill form = baseForm("吴耕田", "赣F·10联收");
        Long id = svc.receive(form).getBill().getId();
        assertNull(svc.approve(id, "核验员甲", null));
        assertEquals(1, svc.selectTAmssPassBillById(id).getInspectBlock().intValue());
        assertNull(svc.approve(id, "核验员甲", "材料补齐了再来"),
                "压死后再签也进不去");
        PassBillView v = svc.view(id);
        assertEquals(1, v.getBill().getNodeNo());
        assertTrue(v.isActive());
        assertEquals(1, v.getMissingCount());
    }

    @Test
    @DisplayName("字号对不上（哪怕同名同姓）：核验不放行")
    void plateUnknown_rejected() {
        // 赣F·11联收 在册机主也叫周麦收，但该字号年检未过且并非本机字号
        TAmssPassBill form = baseForm("周麦收", "赣F·11联收");
        Long id = svc.receive(form).getBill().getId();
        assertNull(svc.approve(id, "核验员甲", null));
        assertEquals(1, svc.selectTAmssPassBillById(id).getNodeNo().intValue());
    }

    @Test
    @DisplayName("复核关：同一只手顶两个位次按替签论，当没签；同一分钟两笔仍是两笔")
    void review_twoDistinctHandsRequired() throws Exception {
        Long id = receivedToReview();
        assertNotNull(svc.approve(id, "一人顶俩戊", 1, null));
        assertNull(svc.approve(id, "一人顶俩戊", 2, null), "替签一概当没签");
        assertEquals(1, svc.view(id).getCurrentSigns().stream()
                .filter(s -> s.getNodeNo() == 2).count());

        // 同一位次两人抢，第二位不算
        assertNull(svc.approve(id, "合龙官丙", 1, null), "名册位已有人判过");

        // 两个不同的人，掐着同一分钟落完，也是两笔，不并成一笔
        assertNotNull(svc.approve(id, "合龙官丙", 2, null));
        List<TAmssPassSign> review = svc.view(id).getCurrentSigns().stream()
                .filter(s -> s.getNodeNo() == 2 && s.getApprover() != null).collect(java.util.stream.Collectors.toList());
        assertEquals(2, review.size(), "两笔还是两笔");
        assertNotEquals(review.get(0).getApprover(), review.get(1).getApprover());
        assertEquals(3, svc.selectTAmssPassBillById(id).getNodeNo().intValue());
    }

    @Test
    @DisplayName("复核名册位：名录压死或合同指省对不上，判不过")
    void rosterSeat_checks() {
        TAmssPassBill form = baseForm("周麦收", "赣F·09联收");
        form.setTeamCardNo("JD2026002");
        Long id = svc.receive(form).getBill().getId();
        svc.approve(id, "核验员甲", null);
        assertNull(svc.approve(id, "名册官乙", 1, null), "名录已压死，不算在队");

        TAmssPassBill form2 = baseForm("周麦收", "赣F·09联收");
        form2.setDestProvince("河北省");
        Long id2 = svc.receive(form2).getBill().getId();
        svc.approve(id2, "核验员甲", null);
        assertNull(svc.approve(id2, "名册官乙", 1, null), "合同指省与名录记要对不上");
    }

    @Test
    @DisplayName("复核合龙位：准驾代号不符、证文拢不进机主字号，判不过")
    void matchSeat_checks() {
        TAmssPassBill form = baseForm("周麦收", "赣F·09联收");
        form.setDriveCode("S");
        Long id = svc.receive(form).getBill().getId();
        svc.approve(id, "核验员甲", null);
        svc.approve(id, "名册官乙", 1, null);
        assertNull(svc.approve(id, "合龙官丙", 2, null), "准驾代号同在册合不拢");

        TAmssPassBill form2 = baseForm("周麦收", "赣F·09联收");
        form2.setCertText("证文里既没写机主也没写字号");
        Long id2 = svc.receive(form2).getBill().getId();
        svc.approve(id2, "核验员甲", null);
        assertNull(svc.approve(id2, "合龙官丙", 2, null), "拟出证文与牌证机主合不了龙");
    }

    @Test
    @DisplayName("市局打回：旧名目尽数勾销（不删行），轮次加一，从第二道重攒；勾销与后补不同列")
    void rollback_voidsAndRestartsFromVerify() {
        Long id = receivedToSeal();
        TAmssPassBill back = svc.rollback(id, "表述不合规矩");
        assertNotNull(back);
        assertEquals(2, back.getRoundNo().intValue());
        assertEquals(1, back.getNodeNo().intValue(), "从第二道（核验）重新往下攒");

        PassBillView v = svc.view(id);
        assertEquals(0, v.getCurrentSigns().size(), "新一轮一笔未攒");
        long voided = signs.rows.values().stream().filter(s -> id.equals(s.getBillId())
                && s.getVoidFlag() == 1).count();
        assertEquals(4, voided, "受理/核验/两位复核四笔尽数勾销，行保留");
        assertEquals(4, v.getVoidedSigns().size(), "勾掉的与后补的不摆在一处");

        // 重新攒一遍：新轮次的签字与旧勾销行分轮分列
        svc.approve(id, "新核验员", null);
        svc.approve(id, "新名册官", 1, null);
        svc.approve(id, "新合龙官", 2, null);
        v = svc.view(id);
        assertEquals(3, v.getBill().getNodeNo().intValue());
        assertEquals(3, v.getCurrentSigns().size());
        assertTrue(v.getCurrentSigns().stream().allMatch(s -> s.getRoundNo() == 2));
        assertEquals(4, v.getVoidedSigns().size());
    }

    @Test
    @DisplayName("没走到用章关打不得；已落章也打不得")
    void rollback_onlyAtSealGate() {
        Long id = receivedToReview();
        assertNull(svc.rollback(id, null), "件还在复核关");
        Long sealedId = receivedAndSealed();
        assertNull(svc.rollback(sealedId, null), "章已落，打不回");
    }

    @Test
    @DisplayName("不予出证：任一道写下即终局，过后谁也补不进一枚")
    void denyIsTerminal() {
        Long id = receivedToReview();
        TAmssPassBill denied = svc.reject(id, "名册官乙", "名册查无此队");
        assertEquals(3, denied.getStatus().intValue());
        assertNull(svc.approve(id, "合龙官丙", 2, null));
        assertNull(svc.approve(id, "核验员甲", null));
        assertNull(svc.reject(id, "别人", null));
        assertNull(svc.rollback(id, null));
        PassBillView v = svc.view(id);
        assertFalse(v.isActive());
        assertTrue(v.getTerminalText().contains("不予出证"));
        assertTrue(signs.rows.values().stream().anyMatch(s -> id.equals(s.getBillId())
                && s.getSignKind() == 1 && "不予出证".equals(s.getSeatName())));
    }

    @Test
    @DisplayName("章一落各栏封死：只有一键转证文可做，改件只能重新起件")
    void sealedLocksBill() {
        Long id = receivedAndSealed();
        assertNull(svc.approve(id, "谁", null));
        assertNull(svc.reject(id, "谁", null));
    }

    @Test
    @DisplayName("一键出证：机主/牌证/目的省/起讫日/证文同件带出，不经第二个人外抄")
    void issueCert_carriesFieldsFromBill() throws Exception {
        Long id = receivedAndSealed();
        TAmssPassCert cert = svc.issueCert(id, "JX2026000001");
        assertNotNull(cert);
        TAmssPassBill b = svc.selectTAmssPassBillById(id);
        assertEquals("周麦收", cert.getOwnerName());
        assertEquals("赣F·09联收", cert.getPlateNo());
        assertEquals("河南省", cert.getDestProvince());
        assertEquals(ymd.parse("2026-05-20"), cert.getValidFrom());
        assertEquals(ymd.parse("2026-07-10"), cert.getValidTo());
        assertEquals(b.getCertText(), cert.getCertText());
        assertEquals("JX2026000001", b.getCertNo());
        assertNotNull(cert.getIssueTime());
        assertNull(svc.issueCert(id, "JX2026000002"), "一件只能转一张");
    }

    @Test
    @DisplayName("同一机主同年度第二张重复抄发，当场拦下")
    void issueCert_duplicateOwnerYearBlocked() {
        Long first = receivedAndSealed();
        assertNotNull(svc.issueCert(first, "JX2026A0001"));
        Long second = receivedAndSealed();
        assertNull(svc.issueCert(second, "JX2026A0002"), "同机主当年重复抄发拦下");
    }

    @Test
    @DisplayName("视图回算纠偏：库存已签数被拨乱后，翻开即按流水重数")
    void view_recountHealsStoredCounter() {
        Long id = receivedToReview();
        TAmssPassBill tampered = new TAmssPassBill();
        tampered.setId(id);
        tampered.setSignCount(99);
        bills.updateById(tampered);
        PassBillView v = svc.view(id);
        assertEquals(0, v.getBill().getSignCount().intValue(), "复核关本关尚无签");
        assertEquals(2, v.getMissingCount(), "屏幕数与库存数同出流水这一回计算");
    }

    @Test
    @DisplayName("回溯对账：出证后各道名目一道一道往回捋，与经办屏同出一源对得上号")
    void traceBack_signsMatchOperatingView() {
        Long id = receivedAndSealed();
        svc.issueCert(id, null);
        PassBillView v = svc.view(id);
        assertNotNull(v.getCert());
        // 四道各一笔（复核关两笔，共五笔），轮次、关口、经手齐全
        assertEquals(5, v.getCurrentSigns().size());
        assertEquals(1, v.getCurrentSigns().stream().filter(s -> s.getNodeNo() == 0).count());
        assertEquals(1, v.getCurrentSigns().stream().filter(s -> s.getNodeNo() == 1).count());
        assertEquals(2, v.getCurrentSigns().stream().filter(s -> s.getNodeNo() == 2).count());
        assertEquals(1, v.getCurrentSigns().stream().filter(s -> s.getNodeNo() == 3).count());
        assertTrue(v.getCurrentSigns().stream().allMatch(s -> s.getApprover() != null && s.getVoidFlag() == 0));
        assertEquals(0, v.getVoidedSigns().size());
    }

    // ------------------------------------------------------------------

    private TAmssPassBill baseForm(String owner, String plate) {
        TAmssPassBill f = new TAmssPassBill();
        f.setOwnerName(owner);
        f.setDriveCode("R");
        f.setPlateNo(plate);
        f.setCertText("持" + plate + "联合收割机往河南参加三夏跨区机收，机主" + owner + "。");
        f.setDestProvince("河南省");
        f.setTeamCardNo("JD2026001");
        f.setIssueYear(2026);
        try {
            f.setValidFrom(ymd.parse("2026-05-20"));
            f.setValidTo(ymd.parse("2026-07-10"));
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
        return f;
    }

    private Long receivedToReview() {
        Long id = svc.receive(baseForm("周麦收", "赣F·09联收")).getBill().getId();
        svc.approve(id, "核验员甲", null);
        return id;
    }

    private Long receivedToSeal() {
        Long id = receivedToReview();
        svc.approve(id, "名册官乙", 1, null);
        svc.approve(id, "合龙官丙", 2, null);
        return id;
    }

    private Long receivedAndSealed() {
        Long id = receivedToSeal();
        svc.approve(id, "站长丁", null);
        return id;
    }

    private TAmssMachReg reg(Long id, String plate, String owner, String drive, Integer year, Integer result) {
        TAmssMachReg r = new TAmssMachReg();
        r.setId(id);
        r.setPlateNo(plate);
        r.setRegOwner(owner);
        r.setDriveCode(drive);
        r.setInspectYear(year);
        r.setInspectResult(result);
        r.setStatus(0);
        r.setDelFlag(0);
        return r;
    }

    private TAmssTeamCard team(String no, int status, String content) {
        TAmssTeamCard c = new TAmssTeamCard();
        c.setBillNo(no);
        c.setStatus(status);
        c.setContent(content);
        c.setDelFlag(0);
        return c;
    }

    // ------------------------------------------------------------------

    /**
     * MyBatis-Plus BaseMapper 的内存假实现：按 @TableId 存行，
     * 支持服务里实际用到的 eq 条件解析与 updateById 非空字段合并，其余方法不用即抛。
     */
    static class FakeMapper<T> implements com.baomidou.mybatisplus.core.mapper.BaseMapper<T> {
        final Map<Object, T> rows = new LinkedHashMap<>();
        private final AtomicLong seq = new AtomicLong(1000);
        private static final Pattern EQ =
                Pattern.compile("([a-z_]+)\\s*=\\s*#\\{ew\\.paramNameValuePairs\\.(MPGENVAL\\d+)}");

        @Override
        public int insert(T entity) {
            try {
                Field idField = idField(entity.getClass());
                Object id = idField.get(entity);
                if (id == null) {
                    id = seq.incrementAndGet();
                    idField.set(entity, id);
                }
                rows.put(id, entity);
                return 1;
            } catch (Exception e) {
                throw new RuntimeException(e);
            }
        }

        @Override
        public int updateById(T entity) {
            try {
                Object id = idField(entity.getClass()).get(entity);
                T stored = rows.get(id);
                if (stored == null) {
                    return 0;
                }
                for (Field f : fields(entity.getClass())) {
                    f.setAccessible(true);
                    Object v = f.get(entity);
                    if (v != null) {
                        f.set(stored, v);
                    }
                }
                return 1;
            } catch (Exception e) {
                throw new RuntimeException(e);
            }
        }

        @Override
        @SuppressWarnings("unchecked")
        public T selectById(Serializable id) {
            return rows.get(id);
        }

        @Override
        public T selectOne(Wrapper<T> queryWrapper) {
            List<T> list = selectList(queryWrapper);
            return list.isEmpty() ? null : list.get(0);
        }

        @Override
        public Integer selectCount(Wrapper<T> queryWrapper) {
            return selectList(queryWrapper).size();
        }

        @Override
        public List<T> selectList(Wrapper<T> queryWrapper) {
            List<T> out = new ArrayList<>();
            for (T row : rows.values()) {
                if (queryWrapper == null || matches(row, queryWrapper)) {
                    out.add(row);
                }
            }
            return out;
        }

        private boolean matches(T row, Wrapper<T> w) {
            try {
                String seg = w.getSqlSegment() == null ? "" : w.getSqlSegment();
                com.baomidou.mybatisplus.core.conditions.AbstractWrapper<?, ?, ?> aw =
                        (com.baomidou.mybatisplus.core.conditions.AbstractWrapper<?, ?, ?>) w;
                Map<String, Object> params = aw.getParamNameValuePairs();
                Matcher m = EQ.matcher(seg);
                while (m.find()) {
                    String column = m.group(1);
                    Object expected = params.get(m.group(2));
                    Object actual = readColumn(row, column);
                    if (expected == null ? actual != null : !expected.equals(actual)) {
                        return false;
                    }
                }
                return true;
            } catch (Exception e) {
                throw new RuntimeException(e);
            }
        }

        private Object readColumn(T row, String column) throws Exception {
            for (Field f : fields(row.getClass())) {
                TableField tf = f.getAnnotation(TableField.class);
                String col = tf != null ? tf.value() : null;
                if (column.equals(col) || (col == null && f.getName().equals(column))) {
                    f.setAccessible(true);
                    return f.get(row);
                }
            }
            return null;
        }

        private static Field idField(Class<?> c) {
            for (Field f : fields(c)) {
                if (f.isAnnotationPresent(TableId.class)) {
                    f.setAccessible(true);
                    return f;
                }
            }
            throw new IllegalStateException("no @TableId on " + c);
        }

        private static List<Field> fields(Class<?> c) {
            List<Field> all = new ArrayList<>();
            Class<?> p = c;
            while (p != null && p != Object.class) {
                for (Field f : p.getDeclaredFields()) {
                    if (!java.lang.reflect.Modifier.isStatic(f.getModifiers())) {
                        all.add(f);
                    }
                }
                p = p.getSuperclass();
            }
            return all;
        }

        @Override public int deleteById(Serializable id) { return rows.remove(id) == null ? 0 : 1; }
        @Override public int deleteByMap(Map<String, Object> columnMap) { throw uoe(); }
        @Override public int delete(Wrapper<T> queryWrapper) { throw uoe(); }
        @Override public int deleteBatchIds(Collection<? extends Serializable> idList) { throw uoe(); }
        @Override public int update(T entity, Wrapper<T> updateWrapper) { throw uoe(); }
        @Override public List<T> selectBatchIds(Collection<? extends Serializable> idList) { throw uoe(); }
        @Override public List<T> selectByMap(Map<String, Object> columnMap) { throw uoe(); }
        @Override public List<Map<String, Object>> selectMaps(Wrapper<T> queryWrapper) { throw uoe(); }
        @Override public List<Object> selectObjs(Wrapper<T> queryWrapper) { throw uoe(); }
        @Override public <E extends IPage<T>> E selectPage(E page, Wrapper<T> queryWrapper) { throw uoe(); }
        @Override public <E extends IPage<Map<String, Object>>> E selectMapsPage(E page, Wrapper<T> queryWrapper) { throw uoe(); }

        private static UnsupportedOperationException uoe() {
            return new UnsupportedOperationException("fake mapper 不支该法");
        }
    }

    /** 各具体 Mapper 的内存假实现：继承通用假实现并挂上项目 Mapper 接口，方法按同签名满足。 */
    static class FakeBillMapper extends FakeMapper<TAmssPassBill> implements com.fc.v2.mapper.auto.TAmssPassBillMapper { }
    static class FakeSignMapper extends FakeMapper<TAmssPassSign> implements com.fc.v2.mapper.auto.TAmssPassSignMapper { }
    static class FakeCertMapper extends FakeMapper<TAmssPassCert> implements com.fc.v2.mapper.auto.TAmssPassCertMapper { }
    static class FakeRegMapper extends FakeMapper<TAmssMachReg> implements com.fc.v2.mapper.auto.TAmssMachRegMapper { }
    static class FakeTeamMapper extends FakeMapper<TAmssTeamCard> implements com.fc.v2.mapper.auto.TAmssTeamCardMapper { }
}
