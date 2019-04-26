package com.cvd.chevdian.mapper.system;

import com.cvd.chevdian.bean.system.Role;
import com.cvd.chevdian.mapper.BaseMapper;
import org.apache.ibatis.annotations.Mapper;

import java.util.List;

/**
 * @author xiaowuge
 * @date 2019-03-19 10:53
 */

public interface RoleMapper extends BaseMapper {
    /**
     *
     */
    Role findSingle(Integer id);

    /**
     * 角色类型列表
     */
    List<Role> findMore();
}
