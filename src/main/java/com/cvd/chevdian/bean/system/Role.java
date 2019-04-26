package com.cvd.chevdian.bean.system;

import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;
import lombok.Data;

import java.util.Date;

/**
 * 角色类型MODEL
 */
@Data
@ApiModel
public class Role {

    @ApiModelProperty(value = "角色类型ID", example = "1-超级管理员")
    private Integer id;

    @ApiModelProperty(value = "角色名", example = "超级管理员")
    private String roleName;

    @ApiModelProperty(value = "角色组", example = "1")
    private String roleGroup;

    @ApiModelProperty(value = "角色描述", example = "管理员")
    private String roleDesc;

    @ApiModelProperty(value = "创建时间", example = "2019-04-11 17:20:00")
    private Date createAt;

}