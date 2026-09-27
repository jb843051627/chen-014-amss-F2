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
import java.util.Calendar;
import java.util.Date;

/**
 * 跨区作业证对外证文对象 t_amss_pass_cert
 * 核准结论一键转写：机主/牌证字号/目的地省/起讫日子同出核准单一件，不另抄。
 *
 * @author fuce
 * @date 2026-09-27
 */
@TableName("t_amss_pass_cert")
@ApiModel(value = "TAmssPassCert", description = "跨区作业证对外证文")
public class TAmssPassCert implements Serializable {
    private static final long serialVersionUID = 1L;

    /** 主键 */
    @TableId(type = IdType.ASSIGN_ID)
    @JsonSerialize(using = ToStringSerializer.class)
    @ApiModelProperty(value = "主键")
    private Long id;

    /** 源核准单（同一件带过来，不准第二个人外抄） */
    @TableField("bill_id")
    @JsonSerialize(using = ToStringSerializer.class)
    @ApiModelProperty(value = "源核准单")
    private Long billId;

    /** 核准单号 */
    @TableField("bill_no")
    @ApiModelProperty(value = "核准单号")
    private String billNo;

    /** 跨区作业证证文字号 */
    @TableField("cert_no")
    @ApiModelProperty(value = "跨区作业证证文字号")
    private String certNo;

    /** 机主 */
    @TableField("owner_name")
    @ApiModelProperty(value = "机主")
    private String ownerName;

    /** 联合收割机在册牌证字号 */
    @TableField("reg_no")
    @ApiModelProperty(value = "联合收割机在册牌证字号")
    private String regNo;

    /** 目的地省 */
    @TableField("dest_province")
    @ApiModelProperty(value = "目的地省")
    private String destProvince;

    /** 作业起始日子 */
    @TableField("valid_from")
    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss", timezone = "GMT+8")
    @ApiModelProperty(value = "作业起始日子")
    private Date validFrom;

    /** 作业截止日子 */
    @TableField("valid_to")
    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss", timezone = "GMT+8")
    @ApiModelProperty(value = "作业截止日子")
    private Date validTo;

    /** 作业年度（同机主当年不得第二条） */
    @TableField("issue_year")
    @ApiModelProperty(value = "作业年度")
    private Integer issueYear;

    /** 证文转发时刻 */
    @TableField("issue_time")
    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss", timezone = "GMT+8")
    @ApiModelProperty(value = "证文转发时刻")
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

    /** 由核准单一件带成证文：字段同源，不由第二个人员外抄 */
    public static TAmssPassCert fromBill(TAmssPassBill bill, String certNo) {
        TAmssPassCert cert = new TAmssPassCert();
        cert.setBillId(bill.getId());
        cert.setBillNo(bill.getBillNo());
        cert.setCertNo(certNo);
        cert.setOwnerName(bill.getOwnerName());
        cert.setRegNo(bill.getRegNo());
        cert.setDestProvince(bill.getDestProvince());
        cert.setValidFrom(bill.getValidFrom());
        cert.setValidTo(bill.getValidTo());
        Calendar cal = Calendar.getInstance();
        cal.setTime(bill.getValidFrom() != null ? bill.getValidFrom() : new Date());
        cert.setIssueYear(cal.get(Calendar.YEAR));
        cert.setIssueTime(new Date());
        cert.setDelFlag(0);
        return cert;
    }

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

    public String getBillNo() {
        return billNo;
    }

    public void setBillNo(String billNo) {
        this.billNo = billNo;
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

    public String getRegNo() {
        return regNo;
    }

    public void setRegNo(String regNo) {
        this.regNo = regNo;
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
