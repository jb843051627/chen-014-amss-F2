package com.fc.v2.model.auto;

import com.baomidou.mybatisplus.annotation.FieldFill;
import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableField;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import com.fasterxml.jackson.annotation.JsonFormat;
import com.fasterxml.jackson.databind.annotation.JsonSerialize;
import com.fasterxml.jackson.databind.ser.std.ToStringSerializer;
import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;

import java.io.Serializable;
import java.util.Date;
import java.util.List;

/**
 * 跨区作业证逐级核准单对象 t_amss_pass_bill
 *
 * @author fuce
 * @date 2026-09-12
 */
@TableName("t_amss_pass_bill")
@ApiModel(value = "TAmssPassBill", description = "跨区作业证逐级核准单")
public class TAmssPassBill implements Serializable {
    private static final long serialVersionUID = 1L;

    /** 主键 */
    @TableId(type = IdType.ASSIGN_ID)
    @JsonSerialize(using = ToStringSerializer.class)
    @ApiModelProperty(value = "主键")
    private Long id;

    /** 跨区作业证核准单号 */
    @TableField("bill_no")
    @ApiModelProperty(value = "跨区作业证核准单号")
    private String billNo;

    /** 当前所在层 0..3 */
    @TableField("node_no")
    @ApiModelProperty(value = "当前所在层 0..3")
    private Integer nodeNo;

    /** 同层核准办法 0任一人 1两名点齐 */
    @TableField("sign_mode")
    @ApiModelProperty(value = "同层核准办法 0任一人 1两名点齐")
    private Integer signMode;

    /** 本层应签人数 */
    @TableField("need_count")
    @ApiModelProperty(value = "本层应签人数")
    private Integer needCount;

    /** 本层已签人数 */
    @TableField("sign_count")
    @ApiModelProperty(value = "本层已签人数")
    private Integer signCount;

    /** 报批情形 0在核 1已核讫 2已打回 3不予出证(终局) */
    @TableField("status")
    @ApiModelProperty(value = "报批情形 0在核 1已核讫 2已打回 3不予出证(终局)")
    private Integer status;

    /** 核准轮次：市局打回勾销后轮次+1，从第二道重走 */
    @TableField("round_no")
    @ApiModelProperty(value = "核准轮次：市局打回勾销后轮次+1，从第二道重走")
    private Integer roundNo;

    /** 封档标记 0未封 1已封（章落或不予后各栏封死） */
    @TableField("sealed")
    @ApiModelProperty(value = "封档标记 0未封 1已封（章落或不予后各栏封死）")
    private Integer sealed;

    /** 对外证文是否已转 0未转 1已转 */
    @TableField("cert_issued")
    @ApiModelProperty(value = "对外证文是否已转 0未转 1已转")
    private Integer certIssued;

    /** 机主姓名（受理四要件之一） */
    @TableField("owner_name")
    @ApiModelProperty(value = "机主姓名（受理四要件之一）")
    private String ownerName;

    /** 准驾代号（受理四要件之一） */
    @TableField("drive_code")
    @ApiModelProperty(value = "准驾代号（受理四要件之一）")
    private String driveCode;

    /** 本机在册牌证字号（受理四要件之一） */
    @TableField("reg_no")
    @ApiModelProperty(value = "本机在册牌证字号（受理四要件之一）")
    private String regNo;

    /** 拟出证文写法（受理四要件之一） */
    @TableField("cert_text")
    @ApiModelProperty(value = "拟出证文写法（受理四要件之一）")
    private String certText;

    /** 年度安全技术检验 0未做或未过 1已过（核验关压件闸） */
    @TableField("insp_passed")
    @ApiModelProperty(value = "年度安全技术检验 0未做或未过 1已过（核验关压件闸）")
    private Integer inspPassed;

    /** 作业合同指向省（目的地省，分管A核得） */
    @TableField("dest_province")
    @ApiModelProperty(value = "作业合同指向省（目的地省，分管A核得）")
    private String destProvince;

    /** 作业起讫日子·起（随作业合同核得） */
    @TableField("valid_from")
    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss", timezone = "GMT+8")
    @ApiModelProperty(value = "作业起讫日子·起（随作业合同核得）")
    private Date validFrom;

    /** 作业起讫日子·讫（随作业合同核得） */
    @TableField("valid_to")
    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss", timezone = "GMT+8")
    @ApiModelProperty(value = "作业起讫日子·讫（随作业合同核得）")
    private Date validTo;

    /** 最近一笔流水名目 */
    @TableField("last_slot")
    @ApiModelProperty(value = "最近一笔流水名目")
    private String lastSlot;

    /** 删除标记 0正常 1删除 */
    @TableField("del_flag")
    @ApiModelProperty(value = "删除标记 0正常 1删除")
    private Integer delFlag;

    /** 创建者 */
    @TableField(value = "create_by", fill = FieldFill.INSERT)
    @ApiModelProperty(value = "创建者")
    private String createBy;

    /** 创建时间 */
    @TableField(value = "create_time", fill = FieldFill.INSERT)
    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss", timezone = "GMT+8")
    @ApiModelProperty(value = "创建时间")
    private Date createTime;

    /** 更新者 */
    @TableField(value = "update_by", fill = FieldFill.UPDATE)
    @ApiModelProperty(value = "更新者")
    private String updateBy;

    /** 更新时间 */
    @TableField(value = "update_time", fill = FieldFill.UPDATE)
    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss", timezone = "GMT+8")
    @ApiModelProperty(value = "更新时间")
    private Date updateTime;

    /** 备注 */
    @TableField("remark")
    @ApiModelProperty(value = "备注")
    private String remark;

    /** 本层还差哪几双手（服务层按流水回算后摆上屏幕，页面不得自填） */
    @TableField(exist = false)
    @ApiModelProperty(value = "本层还差哪几双手（服务层回算）")
    private List<String> pendingSlots;

    /** 各道落过的名目，一道一道往回捋（与当日经办一屏同出一次计算） */
    @TableField(exist = false)
    @ApiModelProperty(value = "流水回捋")
    private List<TAmssPassSign> trace;

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public String getBillNo() {
        return billNo;
    }

    public void setBillNo(String billNo) {
        this.billNo = billNo;
    }

    public Integer getNodeNo() {
        return nodeNo;
    }

    public void setNodeNo(Integer nodeNo) {
        this.nodeNo = nodeNo;
    }

    public Integer getSignMode() {
        return signMode;
    }

    public void setSignMode(Integer signMode) {
        this.signMode = signMode;
    }

    public Integer getNeedCount() {
        return needCount;
    }

    public void setNeedCount(Integer needCount) {
        this.needCount = needCount;
    }

    public Integer getSignCount() {
        return signCount;
    }

    public void setSignCount(Integer signCount) {
        this.signCount = signCount;
    }

    public Integer getStatus() {
        return status;
    }

    public void setStatus(Integer status) {
        this.status = status;
    }

    public Integer getDelFlag() {
        return delFlag;
    }

    public void setDelFlag(Integer delFlag) {
        this.delFlag = delFlag;
    }

    public String getCreateBy() {
        return createBy;
    }

    public void setCreateBy(String createBy) {
        this.createBy = createBy;
    }

    public Date getCreateTime() {
        return createTime;
    }

    public void setCreateTime(Date createTime) {
        this.createTime = createTime;
    }

    public String getUpdateBy() {
        return updateBy;
    }

    public void setUpdateBy(String updateBy) {
        this.updateBy = updateBy;
    }

    public Date getUpdateTime() {
        return updateTime;
    }

    public void setUpdateTime(Date updateTime) {
        this.updateTime = updateTime;
    }

    public String getRemark() {
        return remark;
    }

    public void setRemark(String remark) {
        this.remark = remark;
    }

    public Integer getRoundNo() {
        return roundNo;
    }

    public void setRoundNo(Integer roundNo) {
        this.roundNo = roundNo;
    }

    public Integer getSealed() {
        return sealed;
    }

    public void setSealed(Integer sealed) {
        this.sealed = sealed;
    }

    public Integer getCertIssued() {
        return certIssued;
    }

    public void setCertIssued(Integer certIssued) {
        this.certIssued = certIssued;
    }

    public String getOwnerName() {
        return ownerName;
    }

    public void setOwnerName(String ownerName) {
        this.ownerName = ownerName;
    }

    public String getDriveCode() {
        return driveCode;
    }

    public void setDriveCode(String driveCode) {
        this.driveCode = driveCode;
    }

    public String getRegNo() {
        return regNo;
    }

    public void setRegNo(String regNo) {
        this.regNo = regNo;
    }

    public String getCertText() {
        return certText;
    }

    public void setCertText(String certText) {
        this.certText = certText;
    }

    public Integer getInspPassed() {
        return inspPassed;
    }

    public void setInspPassed(Integer inspPassed) {
        this.inspPassed = inspPassed;
    }

    public String getDestProvince() {
        return destProvince;
    }

    public void setDestProvince(String destProvince) {
        this.destProvince = destProvince;
    }

    public Date getValidFrom() {
        return validFrom;
    }

    public void setValidFrom(Date validFrom) {
        this.validFrom = validFrom;
    }

    public Date getValidTo() {
        return validTo;
    }

    public void setValidTo(Date validTo) {
        this.validTo = validTo;
    }

    public String getLastSlot() {
        return lastSlot;
    }

    public void setLastSlot(String lastSlot) {
        this.lastSlot = lastSlot;
    }

    public List<String> getPendingSlots() {
        return pendingSlots;
    }

    public void setPendingSlots(List<String> pendingSlots) {
        this.pendingSlots = pendingSlots;
    }

    public List<TAmssPassSign> getTrace() {
        return trace;
    }

    public void setTrace(List<TAmssPassSign> trace) {
        this.trace = trace;
    }
}
