package com.cvd.chevdian.bean.vo;

import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;
import lombok.Data;

/**
 * 搜索入参VO
 */
@ApiModel("搜索入参VO")
@Data
public class SearchInfo {
    @ApiModelProperty(value = "搜索内容", example = "13811111111")
    private String searchInfo;
    @ApiModelProperty(value = "搜索类型", example = "1-车辆;10-渠道;20-代理")
    private Integer searchType;
    @ApiModelProperty(value = "页码", example = "1")
    private Integer pageNo = 1;
    @ApiModelProperty(value = "页面大小", example = "10")
    private Integer pageSize = 10;
}
