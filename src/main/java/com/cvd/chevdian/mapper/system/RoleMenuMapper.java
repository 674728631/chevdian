package com.cvd.chevdian.mapper.system;

import com.cvd.chevdian.bean.system.RoleMenu;
import com.cvd.chevdian.mapper.BaseMapper;

import java.util.List;

/**
 * @author xiaowuge
 * @date 2019-03-27 17:07
 */

public interface RoleMenuMapper extends BaseMapper {
    /**
     * 创建角色名称
     */
    int saveEntitySingle(RoleMenu roleMenu);

    /**
     * 角色列表
     */
    List<RoleMenu> findMore();

    /**
     * 根据id查询角色
     */
    RoleMenu queryById(Integer id);

    /**
     * 编辑角色
     */
    int updateModel(RoleMenu roleMenu);

    /**
     * 删除角色
     */
    int delete(Integer id);
}
