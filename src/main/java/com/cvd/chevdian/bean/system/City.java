package com.cvd.chevdian.bean.system;

import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;
import lombok.Data;

import java.util.Date;
import java.util.List;

/**
 * 城市MODEL
 */
@Data
@ApiModel
public class City {

    @ApiModelProperty(value = "城市ID", example = "1")
    private Integer id;
    @ApiModelProperty(value = "城市名称", example = "成都市")
    private String cityName;
    @ApiModelProperty(value = "省份ID", example = "0")
    private Integer provinceId;
    @ApiModelProperty(value = "城市车牌前缀", example = "川A")
    private String plateNoPre;
    @ApiModelProperty(value = "创建时间", example = "2019-04-11 17:20:00")
    private Date createAt;
    @ApiModelProperty(value = "省份所属城市列表", example = "{id:1,cityName:成都市}")
    private List<City> cities;
}
