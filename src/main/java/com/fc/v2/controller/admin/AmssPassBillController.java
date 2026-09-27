package com.fc.v2.controller.admin;

import java.util.List;

import com.fc.v2.common.base.BaseController;
import com.fc.v2.common.domain.AjaxResult;
import com.fc.v2.common.log.Log;
import com.fc.v2.model.auto.TAmssPassBill;
import com.fc.v2.model.auto.TAmssPassCert;
import com.fc.v2.model.custom.PassApproveForm;
import com.fc.v2.service.ITAmssPassBillService;
import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import org.apache.shiro.authz.annotation.RequiresPermissions;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.ModelMap;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.ResponseBody;

/**
 * 跨区作业证逐级核准单 Controller
 *
 * 模块自己不开收件、用章、撤件的口子；每件能不能往下走只认服务层签法的结论，
 * 当前压在哪一道、还差哪几双手，全凭返回单据上的字段摆上屏幕。
 *
 * @author fuce
 * @date 2026-09-14
 */
@Api(value = "跨区作业证逐级核准单")
@Controller
@RequestMapping("/AmssPassBillController")
public class AmssPassBillController extends BaseController {

    private final String prefix = "admin/amssPassBill";

    @Autowired
    private ITAmssPassBillService amssPassBillService;

    @ApiOperation(value = "分页跳转", notes = "分页跳转")
    @GetMapping("/view")
    @RequiresPermissions("amss:amssPassBill:view")
    public String view(ModelMap model) {
        return prefix + "/list";
    }

    /** 翻开一件：节点、已签/应签、还差哪几双手、各道名目回捋，全在返回里 */
    @ApiOperation(value = "单据回查", notes = "节点、签数、待办名目、流水回捋")
    @GetMapping("/detail/{id}")
    @RequiresPermissions("amss:amssPassBill:view")
    @ResponseBody
    public AjaxResult detail(@PathVariable("id") Long id) {
        TAmssPassBill bill = amssPassBillService.selectTAmssPassBillById(id);
        if (bill == null) {
            return AjaxResult.error("核准单不存在");
        }
        return AjaxResult.successData(200, bill);
    }

    /** 节点待办：上一道没走完的件，下一道在这里看不见 */
    @ApiOperation(value = "节点待办", notes = "只摆压在该节点的在办件")
    @GetMapping("/todo/{nodeNo}")
    @RequiresPermissions("amss:amssPassBill:view")
    @ResponseBody
    public AjaxResult todo(@PathVariable("nodeNo") Integer nodeNo) {
        List<TAmssPassBill> list = amssPassBillService.todoAtNode(nodeNo);
        return AjaxResult.successData(200, list);
    }

    /** 第一道·受理：四样少一样，服务层当场拦下 */
    @Log(title = "跨区作业证核准受理", action = "receive")
    @ApiOperation(value = "第一道受理", notes = "机主/准驾代号/在册牌证字号/拟出证文四要件")
    @PostMapping("/receive")
    @RequiresPermissions("amss:amssPassBill:receive")
    @ResponseBody
    public AjaxResult receive(TAmssPassBill form) {
        return AjaxResult.successData(200, amssPassBillService.receive(form));
    }

    /**
     * 签批：VERIFY 核验 / TEAM_A 名册分管 / CARD_B 牌证分管 / SEAL 本站用章 /
     * CITY_FILE 报市局 / REJECT 不予出证。
     * 第三道两位分管同走这个口子，slot 各是各的，两笔永不并笔。
     */
    @Log(title = "跨区作业证核准签批", action = "approve")
    @ApiOperation(value = "节点签批（含不予出证）", notes = "按名目在当前节点落一笔")
    @PostMapping("/approve")
    @RequiresPermissions("amss:amssPassBill:approve")
    @ResponseBody
    public AjaxResult approve(PassApproveForm form) {
        return AjaxResult.successData(200, amssPassBillService.approve(form.getId(), form));
    }

    /** 第四道·市局存案：章落到底，各栏封死 */
    @Log(title = "跨区作业证市局存案", action = "cityAck")
    @ApiOperation(value = "市局存案", notes = "封档")
    @PostMapping("/cityAck")
    @RequiresPermissions("amss:amssPassBill:city")
    @ResponseBody
    public AjaxResult cityAck(Long id, String officer) {
        return AjaxResult.successData(200, amssPassBillService.cityAck(id, officer));
    }

    /** 第四道·市局打回：各道名目尽数勾销，从第二道重走 */
    @Log(title = "跨区作业证市局打回", action = "cityBack")
    @ApiOperation(value = "市局打回", notes = "勾销重走第二道")
    @PostMapping("/cityBack")
    @RequiresPermissions("amss:amssPassBill:city")
    @ResponseBody
    public AjaxResult cityBack(Long id, String officer, String opinion) {
        return AjaxResult.successData(200, amssPassBillService.cityBack(id, officer, opinion));
    }

    /** 一键转对外证文：字段同出核准单一件；同机主当年第二条当场拦下 */
    @Log(title = "跨区作业证证文转发", action = "issueCert")
    @ApiOperation(value = "一键转证文", notes = "核准结论转对外证文页")
    @PostMapping("/issueCert")
    @RequiresPermissions("amss:amssPassBill:issue")
    @ResponseBody
    public AjaxResult issueCert(Long id, String officer) {
        TAmssPassCert cert = amssPassBillService.issueCert(id, officer);
        return AjaxResult.successData(200, cert);
    }
}
