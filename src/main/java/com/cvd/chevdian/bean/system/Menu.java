package com.cvd.chevdian.bean.system;

import lombok.Data;

import java.util.Date;

@Data
public class Menu {

    private Integer id;

    //菜单名
    private String menuName;

    //父级菜单id
    private Integer parentId;

    //菜单连接
    private String menuUrl;

    //菜单标志
    private String menuIcon;

    //所属系统
    private Integer belongSys;

    private Date createAt;

}