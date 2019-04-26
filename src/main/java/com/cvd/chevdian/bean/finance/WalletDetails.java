package com.cvd.chevdian.bean.finance;

import lombok.Data;

import java.util.Date;

@Data
public class WalletDetails {
    private Integer id;
    //用户id
    private Integer customerId;
    //车辆id
    private Integer carId;
    //渠道id
    private Integer channelId;
    //代理人id
    private Integer agentId;
    //用户支付金额
    private Double customerPayAmt;
    //渠道分润比例
    private Double channelPer;
    //渠道分润金额
    private Double channelAmt;
    //代理分润比例
    private Double agentPer;
    //代理分润金额
    private Double agentAmt;
    //类型
    private Integer status;

    private Date createAt;

    private String description;

}