package com.cvd.chevdian.bean.system;

import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;
import lombok.Data;

import java.util.Date;

/**
 * 角色MODEL
 */
@Data
@ApiModel
public class RoleMenu {

    @ApiModelProperty(value = "角色名称ID", example = "1-超级管理员")
    private Integer id;
    @ApiModelProperty(value = "角色名称", example = "分公司")
    private String name;
    @ApiModelProperty(value = "角色类型ID", example = "1-超级管理员")
    private Integer roleId;
    @ApiModelProperty(value = "", example = "")
    private Integer menuId;
    @ApiModelProperty(value = "", example = "")
    private String power;
    @ApiModelProperty(value = "创建时间", example = "2019-04-11 17:20:00")
    private Date createAt;

}