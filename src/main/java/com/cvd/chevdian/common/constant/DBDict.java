package com.cvd.chevdian.common.constant;

import java.math.BigDecimal;

/**
 * 数据库字典数据
 */
public class DBDict {

    /**
     * cvd_user_distributor user_type-渠道用户
     */
    public final static Integer USER_TYPE_CHANNEL = 10;
    /**
     * cvd_user_distributor user_type-渠道代理
     */
    public final static Integer USER_TYPE_AGENT = 20;
    /**
     * cvd_user_distributor user_type-管理员
     */
    public final static Integer USER_TYPE_ADMIN = 30;
    /**
     * 锁定类型
     */
    public final static Integer LOCK_TYPE_ = 20;

    /**
     * cbh_maintenanceshop type-渠道
     */
    public final static Integer SHOP_TYPE_CHANNEL = 20;

    /**
     * cbh_maintenanceshop type-代理
     */
    public final static Integer SHOP_TYPE_AGENT = 30;

    /**
     * cbh_record_recharge eventType - 缴纳互助金
     */
    public final static Integer RECHARGE_EVENTTYPE_PAY = 1;
    /**
     * cbh_record_recharge eventType - 充值
     */
    public final static Integer RECHARGE_EVENTTYPE_RECHARGE = 2;
    /**
     * cbh_record_recharge eventType - 退款
     */
    public final static Integer RECHARGE_EVENTTYPE_REFUND = 7;

    /**
     * 搜索类型字典数据--车辆
     */
    public static final Integer SEARCH_CAR = 1;
    /**
     * 搜索类型字典数据--代理
     */
    public static final Integer SEARCH_AGENT = 20;
    /**
     * 搜索类型字典数据-渠道
     */
    public static final Integer SEARCH_CHANNEL = 10;

    /**
     * 充值金额-9元
     */
    public static final BigDecimal RECHARGE_AMT_9 = new BigDecimal("9");
    /**
     * 充值金额-29元
     */
    public static final BigDecimal RECHARGE_AMT_29 = new BigDecimal("29");
    /**
     * 充值金额-99元
     */
    public static final BigDecimal RECHARGE_AMT_99 = new BigDecimal("99");
    /**
     * 理赔额度-1000
     */
    public static final BigDecimal AMTCOMPENSATION = new BigDecimal("1000");
    /**
     * 基准金额-100
     */
    public static final BigDecimal BASE_PAY = new BigDecimal("100");

    /**
     * 充值类型（1微信，2支付宝,3优惠券4.后台充值10 车妈妈充值,20年结充值）
     * cbh_record_recharge  type - 1
     */
    public static final Integer RECHARGE_TYPE_1 = 1;
    /**
     * (是否到账) 1.到账 2.没有到账
     * cbh_record_recharge  status - 1
     */
    public static final Integer RECHARGE_STATUS_1 = 1;
    public static final Integer RECHARGE_STATUS_2 = 2;
    /**
     * 充值类型（1缴纳互助金，2充值，3支付理赔, 7.退款, 8.余额清零,9-现金提现，20年结充值）
     * cbh_record_recharge  eventType - 1
     */
    public static final Integer RECHARGE_EVENTTYPE_1 = 1;
    public static final Integer RECHARGE_EVENTTYPE_2 = 2;
}
