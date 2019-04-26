package com.cvd.chevdian.bean.finance;

import lombok.Data;

@Data
public class Wallet {
    private Integer id;
    //钱包id
    private Integer walletId;
    //第三方账户
    private String thirdAccount;
    //冻结金额
    private Double freezeAmt;
    //可提金额
    private Double useableAmt;
    //以提金额
    private Double refundAmt;
    //总金额
    private Double totalAmt;

}