package com.cvd.chevdian.service.system;

import com.cvd.chevdian.bean.system.RoleMenu;
import com.github.pagehelper.PageInfo;

import java.util.List;
import java.util.Map;

/**
 * @author xiaowuge
 * @date 2019-03-28 9:13
 */
public interface RoleMenuService {

    /**
     * 创建角色
     */
    int save(RoleMenu roleMenu);

    /**
     * 分页展示角色列表
     */
    PageInfo<RoleMenu> ListRoleMenu(Integer pageNo, Integer pageSize);

    /**
     * 角色详情
     */
    RoleMenu getRoleMenu(Integer id);

    /**
     * 编辑角色
     */
    int update(RoleMenu roleMenu);

    /**
     * 删除角色
     */
    int delete(Integer id);
}
