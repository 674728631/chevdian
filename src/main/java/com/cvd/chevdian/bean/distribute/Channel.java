package com.cvd.chevdian.bean.distribute;

import lombok.Data;

import java.math.BigDecimal;
import java.util.Date;

@Data
public class Channel {
    private Integer id;

    private Integer accountId;

    private Integer walletId;

    private Integer type;

    private Integer status;

    private String name;

    private String img;

    private String businessLicenseImg;

    private String poster;

    private String qrcode;

    private Integer orderquantity;

    private Integer ordermonth;

    private Integer target;

    private Integer ordersradius;

    private Integer cityid;

    private String longitude;

    private String latitude;

    private String shopdescribe;

    private String advantage;

    private String linkman;

    private String tel;

    private String wechat;

    private String address;

    private String uuidarea;

    private String businesshours;

    private String logo;

    private Integer repairnum;

    private Integer commentnum;

    private Integer score;

    private Double servicepoints;

    private BigDecimal ratio;

    private Integer level;

    private Integer levelshare;

    private Integer levelsettlement;

    private Date timejoin;

    private Date createat;

    private BigDecimal distributeper;

    private String agentQR;
}