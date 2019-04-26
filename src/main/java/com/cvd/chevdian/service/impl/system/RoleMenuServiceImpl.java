package com.cvd.chevdian.service.impl.system;

import com.cvd.chevdian.bean.distribute.UserDistributor;
import com.cvd.chevdian.bean.system.RoleMenu;
import com.cvd.chevdian.common.enums.ErrorCodeEnum;
import com.cvd.chevdian.common.exception.BusinessException;
import com.cvd.chevdian.mapper.system.RoleMenuMapper;
import com.cvd.chevdian.service.UserDistributorService;
import com.cvd.chevdian.service.system.RoleMenuService;
import com.github.pagehelper.PageHelper;
import com.github.pagehelper.PageInfo;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Map;

/**
 * @author xiaowuge
 * @date 2019-03-28 9:31
 */
@Service
public class RoleMenuServiceImpl implements RoleMenuService {

    @Autowired
    private RoleMenuMapper roleMenuMapper;
    @Autowired
    private UserDistributorService userDistributorService;

    @Override
    @Transactional(rollbackFor = Exception.class)
    public int save(RoleMenu roleMenu) {
        int i = roleMenuMapper.saveEntitySingle(roleMenu);
        if (i != 1)
            throw new BusinessException(ErrorCodeEnum.GL99990108);
        return roleMenu.getId();
    }

    @Override
    public PageInfo<RoleMenu> ListRoleMenu(Integer pageNo, Integer pageSize) {
        return PageHelper.startPage(pageNo, pageSize)
                .doSelectPageInfo(() -> roleMenuMapper.findMore());
    }

    @Override
    public RoleMenu getRoleMenu(Integer id) {
        return roleMenuMapper.queryById(id);
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public int update(RoleMenu roleMenu) {
        int i = roleMenuMapper.updateModel(roleMenu);
        if (i != 1)
            throw new BusinessException(ErrorCodeEnum.GL99990108);
        return i;
    }

    @Override
    public int delete(Integer id) {
        // 判断是否有人使用
        List<UserDistributor> userList = userDistributorService.selectByRoleId(id);
        if (userList.size() > 0) {
            throw new BusinessException(ErrorCodeEnum.GL99990117);
        }
        int i = roleMenuMapper.delete(id);
        if (i != 1)
            throw new BusinessException(ErrorCodeEnum.GL99990108);
        return i;
    }

}
