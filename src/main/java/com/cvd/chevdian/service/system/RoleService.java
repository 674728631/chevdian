package com.cvd.chevdian.service.system;

import com.cvd.chevdian.bean.system.Role;

import java.util.List;

/**
 * @author xiaowuge
 * @date 2019-03-28 13:43
 */
public interface RoleService {

    List<Role> listRole() throws  Exception;

}
