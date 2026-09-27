package com.fc.v2.model.custom;

import com.fasterxml.jackson.annotation.JsonFormat;
import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;

import java.io.Serializable;
import java.util.Date;

/**
 * 跨区作业证核准签批表单：一道关落一笔时由该节点经手感官提交
 *
 * @author fuce
 * @date 2026-09-27
 */
@ApiModel(value = "PassApproveForm", description = "跨区作业证核准签批表单")
public class PassApproveForm implements Serializable {
    private static final long serialVersionUID = 1L;

    /** 核准单主键 */
    @ApiModelProperty(value = "核准单主键")
    private Long id;

    /** 本笔名目：VERIFY/TEAM_A/CARD_B/SEAL/CITY_FILE/CITY_ACK/REJECT/BACK */
    @ApiModelProperty(value = "本笔名目")
    private String slot;

    /** 经手感官人（替签不得冒名） */
    @ApiModelProperty(value = "经手感官人")
    private String officer;

    /** 本笔判词 */
    @ApiModelProperty(value = "本笔判词")
    private String opinion;

    /** 核验关：在册牌证字号（只对字号，不对人） */
    @ApiModelProperty(value = "在册牌证字号")
    private String regNo;

    /** 核验关：年度安全技术检验 0未做或未过 1已过 */
    @ApiModelProperty(value = "年度安全技术检验 0未做或未过 1已过")
    private Integer inspPassed;

    /** 名册分管关：是否真在跨区作业队名册上 0不在 1在 */
    @ApiModelProperty(value = "是否在跨区作业队名册上 0不在 1在")
    private Integer rostered;

    /** 名册分管关：作业合同指向省 */
    @ApiModelProperty(value = "作业合同指向省")
    private String destProvince;

    /** 名册分管关：作业起始日子 */
    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss", timezone = "GMT+8")
    @ApiModelProperty(value = "作业起始日子")
    private Date validFrom;

    /** 名册分管关：作业截止日子 */
    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss", timezone = "GMT+8")
    @ApiModelProperty(value = "作业截止日子")
    private Date validTo;

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public String getSlot() {
        return slot;
    }

    public void setSlot(String slot) {
        this.slot = slot;
    }

    public String getOfficer() {
        return officer;
    }

    public void setOfficer(String officer) {
        this.officer = officer;
    }

    public String getOpinion() {
        return opinion;
    }

    public void setOpinion(String opinion) {
        this.opinion = opinion;
    }

    public String getRegNo() {
        return regNo;
    }

    public void setRegNo(String regNo) {
        this.regNo = regNo;
    }

    public Integer getInspPassed() {
        return inspPassed;
    }

    public void setInspPassed(Integer inspPassed) {
        this.inspPassed = inspPassed;
    }

    public Integer getRostered() {
        return rostered;
    }

    public void setRostered(Integer rostered) {
        this.rostered = rostered;
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
}
