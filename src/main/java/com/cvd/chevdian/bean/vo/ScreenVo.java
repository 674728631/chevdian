package com.cvd.chevdian.bean.vo;

import com.fasterxml.jackson.annotation.JsonFormat;
import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;
import lombok.Data;

import java.util.Date;

@Data
@ApiModel("统计筛选入参VO")
public class ScreenVo {

    @ApiModelProperty(value = "开始时间", example = "2019-04-17 13:42:01")
    @JsonFormat(pattern = "yyyy-MM-dd")
    private Date beginTime;

    @ApiModelProperty(value = "结束时间", example = "2019-04-17 13:42:01")
    @JsonFormat(pattern = "yyyy-MM-dd")
    private Date endTime;

    @ApiModelProperty(value = "城市ID", example = "1")
    private Integer cityId;
}
