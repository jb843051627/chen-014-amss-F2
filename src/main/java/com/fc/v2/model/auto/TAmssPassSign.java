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
 * 跨区作业证核准逐道签字流水对象 t_amss_pass_sign
 *
 * @author fuce
 * @date 2026-09-27
 */
@TableName("t_amss_pass_sign")
@ApiModel(value = "TAmssPassSign", description = "跨区作业证核准逐道签字流水")
public class TAmssPassSign implements Serializable {
    private static final long serialVersionUID = 1L;

    /** 主键 */
    @TableId(type = IdType.ASSIGN_ID)
    @JsonSerialize(using = ToStringSerializer.class)
    @ApiModelProperty(value = "主键")
    private Long id;

    /** 所属核准单 t_amss_pass_bill.id */
    @TableField("bill_id")
    @JsonSerialize(using = ToStringSerializer.class)
    @ApiModelProperty(value = "所属核准单")
    private Long billId;

    /** 落写时所在轮次（打回后新一轮另攒，旧轮整批勾销） */
    @TableField("round_no")
    @ApiModelProperty(value = "落写时所在轮次")
    private Integer roundNo;

    /** 落写的关口 0受理 1核验 2复核 3用章 */
    @TableField("node_no")
    @ApiModelProperty(value = "落写的关口 0受理 1核验 2复核 3用章")
    private Integer nodeNo;

    /** 关口内位次 0单人关 1名册合同位 2牌证证文合龙位 */
    @TableField("seat_no")
    @ApiModelProperty(value = "关口内位次 0单人关 1名册合同位 2牌证证文合龙位")
    private Integer seatNo;

    /** 本笔名目（受理/字号核验/名册合同核验/合龙核验/用印） */
    @TableField("seat_name")
    @ApiModelProperty(value = "本笔名目")
    private String seatName;

    /** 名目类别 0核准签字 1不予出证 */
    @TableField("sign_kind")
    @ApiModelProperty(value = "名目类别 0核准签字 1不予出证")
    private Integer signKind;

    /** 经人手签（同一人顶两个位次按替签论，不计） */
    @TableField("approver")
    @ApiModelProperty(value = "经人手签")
    private String approver;

    /** 判语 */
    @TableField("opinion")
    @ApiModelProperty(value = "判语")
    private String opinion;

    /** 勾销标记 0在账 1市局打回时尽数勾销（保留行，不与后补同列） */
    @TableField("void_flag")
    @ApiModelProperty(value = "勾销标记 0在账 1已勾销")
    private Integer voidFlag;

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

    public Integer getRoundNo() {
        return roundNo;
    }

    public void setRoundNo(Integer roundNo) {
        this.roundNo = roundNo;
    }

    public Integer getNodeNo() {
        return nodeNo;
    }

    public void setNodeNo(Integer nodeNo) {
        this.nodeNo = nodeNo;
    }

    public Integer getSeatNo() {
        return seatNo;
    }

    public void setSeatNo(Integer seatNo) {
        this.seatNo = seatNo;
    }

    public String getSeatName() {
        return seatName;
    }

    public void setSeatName(String seatName) {
        this.seatName = seatName;
    }

    public Integer getSignKind() {
        return signKind;
    }

    public void setSignKind(Integer signKind) {
        this.signKind = signKind;
    }

    public String getApprover() {
        return approver;
    }

    public void setApprover(String approver) {
        this.approver = approver;
    }

    public String getOpinion() {
        return opinion;
    }

    public void setOpinion(String opinion) {
        this.opinion = opinion;
    }

    public Integer getVoidFlag() {
        return voidFlag;
    }

    public void setVoidFlag(Integer voidFlag) {
        this.voidFlag = voidFlag;
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
