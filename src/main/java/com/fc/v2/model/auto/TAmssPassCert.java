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
 * 跨区作业证对外证文存根对象 t_amss_pass_cert
 *
 * @author fuce
 * @date 2026-09-27
 */
@TableName("t_amss_pass_cert")
@ApiModel(value = "TAmssPassCert", description = "跨区作业证对外证文存根")
public class TAmssPassCert implements Serializable {
    private static final long serialVersionUID = 1L;

    /** 主键 */
    @TableId(type = IdType.ASSIGN_ID)
    @JsonSerialize(using = ToStringSerializer.class)
    @ApiModelProperty(value = "主键")
    private Long id;

    /** 来源核准单 */
    @TableField("bill_id")
    @JsonSerialize(using = ToStringSerializer.class)
    @ApiModelProperty(value = "来源核准单")
    private Long billId;

    /** 跨区作业证证号（省级统一印制字号） */
    @TableField("cert_no")
    @ApiModelProperty(value = "跨区作业证证号")
    private String certNo;

    /** 机主姓名（自核准单带过） */
    @TableField("owner_name")
    @ApiModelProperty(value = "机主姓名")
    private String ownerName;

    /** 在册牌证字号（自核准单带过） */
    @TableField("plate_no")
    @ApiModelProperty(value = "在册牌证字号")
    private String plateNo;

    /** 目的地省（自核准单带过） */
    @TableField("dest_province")
    @ApiModelProperty(value = "目的地省")
    private String destProvince;

    /** 作业证起日（自核准单带过） */
    @TableField("valid_from")
    @JsonFormat(pattern = "yyyy-MM-dd", timezone = "GMT+8")
    @ApiModelProperty(value = "作业证起日")
    private Date validFrom;

    /** 作业证讫日（自核准单带过） */
    @TableField("valid_to")
    @JsonFormat(pattern = "yyyy-MM-dd", timezone = "GMT+8")
    @ApiModelProperty(value = "作业证讫日")
    private Date validTo;

    /** 证文全文（自核准单拟文稿带过） */
    @TableField("cert_text")
    @ApiModelProperty(value = "证文全文")
    private String certText;

    /** 发证年度（同机主同年度第二张当场拦） */
    @TableField("issue_year")
    @ApiModelProperty(value = "发证年度")
    private Integer issueYear;

    /** 出证时刻 */
    @TableField("issue_time")
    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss", timezone = "GMT+8")
    @ApiModelProperty(value = "出证时刻")
    private Date issueTime;

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

    public Long getBillId() {
        return billId;
    }

    public void setBillId(Long billId) {
        this.billId = billId;
    }

    public String getCertNo() {
        return certNo;
    }

    public void setCertNo(String certNo) {
        this.certNo = certNo;
    }

    public String getOwnerName() {
        return ownerName;
    }

    public void setOwnerName(String ownerName) {
        this.ownerName = ownerName;
    }

    public String getPlateNo() {
        return plateNo;
    }

    public void setPlateNo(String plateNo) {
        this.plateNo = plateNo;
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

    public String getCertText() {
        return certText;
    }

    public void setCertText(String certText) {
        this.certText = certText;
    }

    public Integer getIssueYear() {
        return issueYear;
    }

    public void setIssueYear(Integer issueYear) {
        this.issueYear = issueYear;
    }

    public Date getIssueTime() {
        return issueTime;
    }

    public void setIssueTime(Date issueTime) {
        this.issueTime = issueTime;
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
