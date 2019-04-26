package com.cvd.chevdian.bean.distribute;

import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
import java.util.Date;

@Data
@NoArgsConstructor
@ApiModel
public class UserDistributor {
    @ApiModelProperty(value = "userID", example = "1")
    private Integer id;
    @ApiModelProperty(value = "walletId", example = "1")
    private Integer walletId;
    @ApiModelProperty(value = "roleId", example = "1")
    private Integer roleId;
    @ApiModelProperty(value = "真实姓名", example = "张三")
    private String userName;
    @ApiModelProperty(value = "手机号", example = "18888888888")
    private String userPn;
    @ApiModelProperty(value = "密码", example = "123456")
    private String password;
    @ApiModelProperty(value = "父级ID", example = "1")
    private Integer parentId;
    @ApiModelProperty(value = "头像", example = "xx")
    private String portrait;
    @ApiModelProperty(value = "昵称", example = "张三")
    private String nickName;
    @ApiModelProperty(value = "城市ID", example = "1")
    private Integer cityId;
    @ApiModelProperty(value = "省份ID", example = "1")
    private Integer provinceId;
    @ApiModelProperty(value = "登录时间", example = "2019-03-03 10:11:11")
    private Date loginDate;
    @ApiModelProperty(value = "loginDefaultTimes", example = "2019-03-03 10:11:11")
    private Integer loginDefaultTimes;
    @ApiModelProperty(value = "accountLockTime", example = "2019-03-03 10:11:11")
    private Date accountLockTime;
    @ApiModelProperty(value = "lockType", example = "1")
    private Integer lockType;
    @ApiModelProperty(value = "用户类型:10-渠道用户;20-渠道代理;30-管理员", example = "10")
    private Integer userType;
    @ApiModelProperty(value = "状态", example = "1")
    private Integer status;
    @ApiModelProperty(value = "token", example = "xx")
    private String token;
    @ApiModelProperty(value = "tokenAge", example = "2019-03-03 10:11:11")
    private Date tokenAge;
    @ApiModelProperty(value = "微信openId",example = "xx")
    private String openId;
    @ApiModelProperty(value = "distributePer",example = "0.8")
    private BigDecimal distributePer;
    @ApiModelProperty(value = "创建时间", example = "2019-03-03 10:11:11")
    private Date createAt;
    @ApiModelProperty(value = "对应渠道表ID", example = "1")
    private Integer channelId;
    @ApiModelProperty( value = "邀请用户二维码", example = "xx")
    private String userQRcode;
    @ApiModelProperty(value = "邀请代理二维码", example = "xx")
    private String agentQRcode;
    @ApiModelProperty(value = "地区", example = "四川省成都市")
    private String cityName;
    @ApiModelProperty(value = "角色名", example = "代理人")
    private String roleName;
    @ApiModelProperty(value = "渠道类型", example = "0-普通渠道，10-分销渠道")
    private Integer flag;


    private Integer pageNo = 1;
    private Integer pageSize = 10;


    public UserDistributor(String openId) {
        this.openId = openId;
    }

    public UserDistributor(String openId, Integer parentId) {
        this.openId = openId;
        this.parentId = parentId;
    }
    public UserDistributor(Integer id, String userQRcode) {
        this.id = id;
        this.userQRcode = userQRcode;
    }

}