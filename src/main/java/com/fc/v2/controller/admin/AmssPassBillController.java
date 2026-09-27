package com.fc.v2.controller.admin;

import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.fc.v2.common.base.BaseController;
import com.fc.v2.common.domain.AjaxResult;
import com.fc.v2.common.domain.ResultTable;
import com.fc.v2.common.log.Log;
import com.fc.v2.model.auto.TAmssPassBill;
import com.fc.v2.model.auto.TAmssPassCert;
import com.fc.v2.model.custom.PassBillView;
import com.fc.v2.service.ITAmssPassBillService;
import com.fc.v2.shiro.util.ShiroUtils;
import com.github.pagehelper.PageInfo;
import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import org.apache.shiro.authz.annotation.RequiresPermissions;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.ModelMap;
import org.springframework.web.bind.annotation.*;

/**
 * 跨区作业证逐级核准单 Controller（approval-chain 形状）。
 * 只做业务服务层的薄转发：能不能往下走全凭服务层签法给出的结论，
 * 本控制器不开收件、用章、撤件的旁门口；经人手取当前登录名，不由页面代填。
 *
 * @author fuce
 * @date 2026-09-14
 */
@Api(value = "跨区作业证逐级核准单")
@Controller
@RequestMapping("/amssPassBill")
public class AmssPassBillController extends BaseController {

    private final String prefix = "admin/amssPassBill";

    @Autowired
    private ITAmssPassBillService amssPassBillService;

    @ApiOperation(value = "核准台账跳转", notes = "核准台账跳转")
    @GetMapping("/view")
    @RequiresPermissions("amssPassBill:view")
    public String view() {
        return prefix + "/list";
    }

    @Log(title = "跨区作业证核准台账", action = "list")
    @ApiOperation(value = "台账分页", notes = "台账分页")
    @GetMapping("/list")
    @RequiresPermissions("amssPassBill:list")
    @ResponseBody
    public ResultTable list(TAmssPassBill query) {
        QueryWrapper<TAmssPassBill> qw = new QueryWrapper<>();
        qw.eq(query.getBillNo() != null && !query.getBillNo().trim().isEmpty(), "bill_no", query.getBillNo());
        qw.eq(query.getPlateNo() != null && !query.getPlateNo().trim().isEmpty(), "plate_no", query.getPlateNo());
        qw.eq(query.getStatus() != null, "status", query.getStatus());
        qw.orderByDesc("create_time");
        startPage();
        PageInfo<TAmssPassBill> page = new PageInfo<>(amssPassBillService.selectPassBillList(qw));
        return pageTable(page.getList(), page.getTotal());
    }

    @ApiOperation(value = "跨区作业证申领表", notes = "仍用现行那张申领表，不另做一张")
    @GetMapping("/receivePage")
    @RequiresPermissions("amssPassBill:receive")
    public String receivePage() {
        return prefix + "/receive";
    }

    @Log(title = "跨区作业证申领受理", action = "receive")
    @ApiOperation(value = "收件受理（四道头一关）", notes = "四样少一样当场拦在门外")
    @PostMapping("/receive")
    @RequiresPermissions("amssPassBill:receive")
    @ResponseBody
    public AjaxResult receive(TAmssPassBill form) {
        form.setCreateBy(ShiroUtils.getLoginName());
        PassBillView v = amssPassBillService.receive(form);
        return v == null ? error("受理拦下：机主、准驾代号、在册牌证字号、拟出证文四样少一样")
                : AjaxResult.success(200, "受理完成", v.getBill());
    }

    @ApiOperation(value = "经办一屏跳转", notes = "翻开一件：关口、缺口、名目全凭服务层返回")
    @GetMapping("/detail/{id}")
    @RequiresPermissions("amssPassBill:view")
    public String detail(@PathVariable("id") Long id, ModelMap model) {
        model.put("billId", id);
        return prefix + "/detail";
    }

    @Log(title = "跨区作业证核准单查询", action = "view")
    @ApiOperation(value = "经办视图", notes = "当前关口/还差几手/各道名目同一回计算")
    @GetMapping("/detail/data/{id}")
    @RequiresPermissions("amssPassBill:view")
    @ResponseBody
    public AjaxResult detailData(@PathVariable("id") Long id) {
        PassBillView v = amssPassBillService.view(id);
        return v == null ? error("件不存在") : retobject(200, v);
    }

    @Log(title = "跨区作业证核准签字", action = "approve")
    @ApiOperation(value = "签字核准（单人关旧签法）", notes = "受理/核验/用章关")
    @PostMapping("/approve")
    @RequiresPermissions("amssPassBill:approve")
    @ResponseBody
    public AjaxResult approve(Long id, String comment) {
        TAmssPassBill r = amssPassBillService.approve(id, ShiroUtils.getLoginName(), comment);
        return r == null ? error("本关签不进去：件不在这道，或字号/年检/证文合不拢") : success();
    }

    @Log(title = "跨区作业证复核并签", action = "approve")
    @ApiOperation(value = "复核关按位次签（新增重载签法）", notes = "seatNo 1名册合同位 2牌证证文合龙位，替签当没签")
    @PostMapping("/approveSeat")
    @RequiresPermissions("amssPassBill:approve")
    @ResponseBody
    public AjaxResult approveSeat(Long id, Integer seatNo, String comment) {
        TAmssPassBill r = amssPassBillService.approve(id, ShiroUtils.getLoginName(), seatNo, comment);
        return r == null ? error("这笔签不上：位次已有人判过、同手替签，或本关判语不过") : success();
    }

    @Log(title = "跨区作业证不予出证", action = "reject")
    @ApiOperation(value = "写下不予出证", notes = "件就此止步，过后谁也补不进一枚")
    @PostMapping("/reject")
    @RequiresPermissions("amssPassBill:reject")
    @ResponseBody
    public AjaxResult reject(Long id, String comment) {
        TAmssPassBill r = amssPassBillService.reject(id, ShiroUtils.getLoginName(), comment);
        return r == null ? error("件已终局或已封章，写不进去") : success();
    }

    @Log(title = "市局打回重核", action = "rollback")
    @ApiOperation(value = "市局打回", notes = "各道名目尽数勾销，从第二道重新往下攒")
    @PostMapping("/rollback")
    @RequiresPermissions("amssPassBill:rollback")
    @ResponseBody
    public AjaxResult rollback(Long id, String comment) {
        TAmssPassBill r = amssPassBillService.rollback(id, comment);
        return r == null ? error("只有在用章关、章未落的件才打得回") : success();
    }

    @Log(title = "核准结论一键转对外证文", action = "issue")
    @ApiOperation(value = "一键出证", notes = "机主/牌证/目的省/起讫日同件带出，当年重复当场拦")
    @PostMapping("/issue")
    @RequiresPermissions("amssPassBill:issue")
    @ResponseBody
    public AjaxResult issue(Long id, String certNo) {
        TAmssPassCert cert = amssPassBillService.issueCert(id, certNo);
        return cert == null ? error("出不了证：四道未走满、未盖章，或同机主本年度已发过一张")
                : AjaxResult.success("已转对外证文").put("data", cert);
    }

    @ApiOperation(value = "对外证文一页跳转", notes = "字段全部自核准结论带出")
    @GetMapping("/cert/{id}")
    @RequiresPermissions("amssPassBill:view")
    public String certPage(@PathVariable("id") Long id, ModelMap model) {
        model.put("billId", id);
        return prefix + "/cert";
    }
}
