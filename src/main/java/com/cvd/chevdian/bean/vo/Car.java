package com.cvd.chevdian.bean.vo;

import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;
import lombok.Data;

import java.math.BigDecimal;
import java.util.Date;

/**
 * 用户车辆详情MODEL
 */
@ApiModel
@Data
public class Car {

    @ApiModelProperty(name = "carId", value = "车辆ID", dataType = "Integer", example = "1")
    private Integer carId;
    @ApiModelProperty(name = "licensePlateNumber", value = "车牌", dataType = "String", example = "川A88888")
    private String licensePlateNumber;
    @ApiModelProperty(name = "customerId", value = "用户ID", dataType = "Integer", example = "1")
    private Integer customerId;
    @ApiModelProperty(name = "customerPN", value = "用户手机", dataType = "String", example = "18888888888")
    private String customerPN;
    @ApiModelProperty(name = "openId", value = "微信openid", dataType = "String", example = "xxx")
    private String openId;
    @ApiModelProperty(name = "amtCooperation", value = "互助金", dataType = "Double", example = "9.1")
    private Double amtCooperation;
    @ApiModelProperty(name = "status", value = "车辆状态", dataType = "Integer", example = "1-待支付；13-观察期；20-为保障中；30-已退出；31-不可用")
    private Integer status;
    @ApiModelProperty(name = "nameCarOwner", value = "车主姓名", dataType = "String", example = "张三")
    private String nameCarOwner;
    @ApiModelProperty(name = "cityId", value = "城市ID", dataType = "Integer", example = "1")
    private Integer cityId;
    @ApiModelProperty(value = "省份ID", dataType = "Integer", example = "1")
    private Integer provinceId;
    @ApiModelProperty(name = "cityName", value = "车辆城市", dataType = "String", example = "成都")
    private String cityName;
    @ApiModelProperty(name = "insuranceId", value = "保险公司ID", dataType = "Integer", example = "1")
    private Integer insuranceId;
    @ApiModelProperty(name = "insurance", value = "保险公司名称", dataType = "String", example = "中国平安")
    private String insurance;
    @ApiModelProperty(name = "endTime", value = "保险到期时间", dataType = "Date", example = "2019-03-03 10:11:11")
    private Date endTime;
    @ApiModelProperty(name = "isAutoRemind", value = "保险到期提醒", dataType = "Integer", example = "1")
    private Integer isAutoRemind;
    @ApiModelProperty(name = "rechargeAmt", value = "充值金额", dataType = "Double", example = "99")
    private Double rechargeAmt;
    @ApiModelProperty(value = "用户创建时间",example = "2019-03-03 10:11:11")
    private Date createAt;
    @ApiModelProperty(value = "保障类型",example = "1-非包年；2-包年")
    private Integer typeGuarantee;
    @ApiModelProperty(value = "第一次充值时间",example = "2019-03-03 10:11:11")
    private String minT;
    @ApiModelProperty(value = "年度充值金额",example = "9")
    private BigDecimal amount;
    @ApiModelProperty(value = "保障结束时间",example = "2019-03-03 10:11:11")
    private Date timeEnd;
    @ApiModelProperty(value = "保障开始时间",example = "2019-03-03 10:11:11")
    private Date timeBegin;
}
