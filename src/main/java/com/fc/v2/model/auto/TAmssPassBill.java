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

    /** 当前所在层 0受理 1核验 2复核 3用章 */
    @TableField("node_no")
    @ApiModelProperty(value = "当前所在层 0受理 1核验 2复核 3用章")
    private Integer nodeNo;

    /** 同层核准办法 0任一人 1两名点齐 */
    @TableField("sign_mode")
    @ApiModelProperty(value = "同层核准办法 0任一人 1两名点齐")
    private Integer signMode;

    /** 本层应签人数（随层配置，不由页面填） */
    @TableField("need_count")
    @ApiModelProperty(value = "本层应签人数（随层配置，不由页面填）")
    private Integer needCount;

    /** 本层已签人数（一律由签字流水回算后写回） */
    @TableField("sign_count")
    @ApiModelProperty(value = "本层已签人数（一律由签字流水回算后写回）")
    private Integer signCount;

    /** 机主姓名（受理四要件之一） */
    @TableField("owner_name")
    @ApiModelProperty(value = "机主姓名（受理四要件之一）")
    private String ownerName;

    /** 准驾代号类别（受理四要件之一） */
    @TableField("drive_code")
    @ApiModelProperty(value = "准驾代号类别（受理四要件之一）")
    private String driveCode;

    /** 本机在册牌证字号（受理四要件之一，核验只对字号） */
    @TableField("plate_no")
    @ApiModelProperty(value = "本机在册牌证字号（受理四要件之一，核验只对字号）")
    private String plateNo;

    /** 拟出证文（受理四要件之一） */
    @TableField("cert_text")
    @ApiModelProperty(value = "拟出证文（受理四要件之一）")
    private String certText;

    /** 作业合同指向的目的地省（随证文外发） */
    @TableField("dest_province")
    @ApiModelProperty(value = "作业合同指向的目的地省（随证文外发）")
    private String destProvince;

    /** 作业证起日（随证文外发） */
    @TableField("valid_from")
    @JsonFormat(pattern = "yyyy-MM-dd", timezone = "GMT+8")
    @ApiModelProperty(value = "作业证起日（随证文外发）")
    private Date validFrom;

    /** 作业证讫日（随证文外发） */
    @TableField("valid_to")
    @JsonFormat(pattern = "yyyy-MM-dd", timezone = "GMT+8")
    @ApiModelProperty(value = "作业证讫日（随证文外发）")
    private Date validTo;

    /** 申报的跨区作业队在队名册卡号 */
    @TableField("team_card_no")
    @ApiModelProperty(value = "申报的跨区作业队在队名册卡号")
    private String teamCardNo;

    /** 申领年度（年检口径与当年重复抄发拦截） */
    @TableField("issue_year")
    @ApiModelProperty(value = "申领年度（年检口径与当年重复抄发拦截）")
    private Integer issueYear;

    /** 核准轮次 首轮1，市局每打回一次加一（勾销与后补不同轮，不摆在一处） */
    @TableField("round_no")
    @ApiModelProperty(value = "核准轮次 首轮1，市局每打回一次加一")
    private Integer roundNo;

    /** 核验压死标记 0正常 1年检未做或未过一次压死再齐也走不动 */
    @TableField("inspect_block")
    @ApiModelProperty(value = "核验压死标记 0正常 1年检未做或未过一次压死再齐也走不动")
    private Integer inspectBlock;

    /** 是否已落本站公章 0未盖 1已落（落后各栏封死） */
    @TableField("sealed")
    @ApiModelProperty(value = "是否已落本站公章 0未盖 1已落（落后各栏封死）")
    private Integer sealed;

    /** 已转对外证文号（空即尚未出证） */
    @TableField("cert_no")
    @ApiModelProperty(value = "已转对外证文号（空即尚未出证）")
    private String certNo;

    /** 报批情形 0在核 1已核讫 2已打回 3不予出证(终局) */
    @TableField("status")
    @ApiModelProperty(value = "报批情形 0在核 1已核讫 2已打回 3不予出证(终局)")
    private Integer status;

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

    public String getPlateNo() {
        return plateNo;
    }

    public void setPlateNo(String plateNo) {
        this.plateNo = plateNo;
    }

    public String getCertText() {
        return certText;
    }

    public void setCertText(String certText) {
        this.certText = certText;
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

    public String getTeamCardNo() {
        return teamCardNo;
    }

    public void setTeamCardNo(String teamCardNo) {
        this.teamCardNo = teamCardNo;
    }

    public Integer getIssueYear() {
        return issueYear;
    }

    public void setIssueYear(Integer issueYear) {
        this.issueYear = issueYear;
    }

    public Integer getRoundNo() {
        return roundNo;
    }

    public void setRoundNo(Integer roundNo) {
        this.roundNo = roundNo;
    }

    public Integer getInspectBlock() {
        return inspectBlock;
    }

    public void setInspectBlock(Integer inspectBlock) {
        this.inspectBlock = inspectBlock;
    }

    public Integer getSealed() {
        return sealed;
    }

    public void setSealed(Integer sealed) {
        this.sealed = sealed;
    }

    public String getCertNo() {
        return certNo;
    }

    public void setCertNo(String certNo) {
        this.certNo = certNo;
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
}
