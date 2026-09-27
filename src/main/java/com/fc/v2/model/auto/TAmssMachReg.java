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
 * 农机在册牌证与年度检验底档对象 t_amss_mach_reg
 *
 * @author fuce
 * @date 2026-09-27
 */
@TableName("t_amss_mach_reg")
@ApiModel(value = "TAmssMachReg", description = "农机在册牌证与年度检验底档")
public class TAmssMachReg implements Serializable {
    private static final long serialVersionUID = 1L;

    /** 主键 */
    @TableId(type = IdType.ASSIGN_ID)
    @JsonSerialize(using = ToStringSerializer.class)
    @ApiModelProperty(value = "主键")
    private Long id;

    /** 在册牌证字号（核验唯一对得上的东西） */
    @TableField("plate_no")
    @ApiModelProperty(value = "在册牌证字号")
    private String plateNo;

    /** 在册登记机主（姓名一模一样也不替字号顶用） */
    @TableField("reg_owner")
    @ApiModelProperty(value = "在册登记机主")
    private String regOwner;

    /** 机型名称 */
    @TableField("machine_model")
    @ApiModelProperty(value = "机型名称")
    private String machineModel;

    /** 准驾代号类别 */
    @TableField("drive_code")
    @ApiModelProperty(value = "准驾代号类别")
    private String driveCode;

    /** 最近一次安全技术检验年度 */
    @TableField("inspect_year")
    @ApiModelProperty(value = "最近一次安全技术检验年度")
    private Integer inspectYear;

    /** 年检情形 0未做 1合格 2不合格 */
    @TableField("inspect_result")
    @ApiModelProperty(value = "年检情形 0未做 1合格 2不合格")
    private Integer inspectResult;

    /** 底档情形 0在册 1已注销 */
    @TableField("status")
    @ApiModelProperty(value = "底档情形 0在册 1已注销")
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

    public String getPlateNo() {
        return plateNo;
    }

    public void setPlateNo(String plateNo) {
        this.plateNo = plateNo;
    }

    public String getRegOwner() {
        return regOwner;
    }

    public void setRegOwner(String regOwner) {
        this.regOwner = regOwner;
    }

    public String getMachineModel() {
        return machineModel;
    }

    public void setMachineModel(String machineModel) {
        this.machineModel = machineModel;
    }

    public String getDriveCode() {
        return driveCode;
    }

    public void setDriveCode(String driveCode) {
        this.driveCode = driveCode;
    }

    public Integer getInspectYear() {
        return inspectYear;
    }

    public void setInspectYear(Integer inspectYear) {
        this.inspectYear = inspectYear;
    }

    public Integer getInspectResult() {
        return inspectResult;
    }

    public void setInspectResult(Integer inspectResult) {
        this.inspectResult = inspectResult;
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
