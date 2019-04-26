package com.cvd.chevdian.bean.vo;

import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;
import lombok.Data;

/**
 * 车V互助渠道MODEL
 */
@ApiModel
@Data
public class ChannelVo {

    @ApiModelProperty(value = "渠道ID", example = "1")
    private Integer id;
    @ApiModelProperty(value = "商家类型", example = "10修理厂,20其他渠道")
    private Integer type;
    @ApiModelProperty(value = "邀请用户二维码", example = "xxx.png")
    private String qrcode;
    @ApiModelProperty(value = "渠道名称", example = "中国平安成都分公司")
    private String name;
    @ApiModelProperty(value = "手机号码", example = "18725416541")
    private String tel;
    @ApiModelProperty(value = "角色", example = "1")
    private Integer roleId;
    @ApiModelProperty(value = "地区", example = "1")
    private Integer uuidArea;
    @ApiModelProperty(value = "状态", example = "正常")
    private Integer status;
    @ApiModelProperty(value = "渠道类型", example = "0-普通渠道，10-分销渠道")
    private Integer flag;
}
