package com.cvd.chevdian.service.impl.system;

import com.cvd.chevdian.bean.system.Role;
import com.cvd.chevdian.mapper.system.RoleMapper;
import com.cvd.chevdian.service.system.RoleService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.List;

/**
 * @author xiaowuge
 * @date 2019-03-28 13:52
 */
@Service
public class RoleServiceImpl implements RoleService {

    @Autowired
    private RoleMapper roleMapper;

    @Override
    public List<Role> listRole() {
        return roleMapper.findMore();
    }
}
