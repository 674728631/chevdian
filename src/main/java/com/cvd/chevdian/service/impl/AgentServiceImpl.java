package com.cvd.chevdian.service.impl;

import com.cvd.chevdian.bean.distribute.UserDistributor;
import com.cvd.chevdian.bean.vo.ScreenVo;
import com.cvd.chevdian.common.constant.DBDict;
import com.cvd.chevdian.common.enums.ErrorCodeEnum;
import com.cvd.chevdian.common.exception.BusinessException;
import com.cvd.chevdian.mapper.MaintenanceshopMapper;
import com.cvd.chevdian.mapper.UserDistributorMapper;
import com.cvd.chevdian.service.AgentService;
import com.cvd.chevdian.service.UserDistributorService;
import com.github.pagehelper.PageHelper;
import com.github.pagehelper.PageInfo;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Slf4j
@Service
public class AgentServiceImpl implements AgentService {

    @Autowired
    UserDistributorMapper userDistributorMapper;
    @Autowired
    UserDistributorService userDistributorService;
    @Autowired
    MaintenanceshopMapper maintenanceshopMapper;

    @Override
    public PageInfo<UserDistributor> getAgentByChannelId(UserDistributor userDistributor) {
        return PageHelper.startPage(userDistributor.getPageNo(), userDistributor.getPageSize())
                .doSelectPageInfo(() -> userDistributorMapper.selectListByType(userDistributor, DBDict.USER_TYPE_AGENT, new ScreenVo()));
    }

    @Override
    public UserDistributor getAgentById(Integer agentId) {
        return userDistributorService.queryById(agentId);
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void editAgent(UserDistributor user) {
        Integer id = user.getId();
        UserDistributor oldUser = getAgentById(id);
        int i = userDistributorMapper.updateEntityModel(user);
        if (i != 1)
            throw new BusinessException(ErrorCodeEnum.GL99990108);
        UserDistributor newUser = getAgentById(id);
        // 比较变动项 名称、电话、角色、地区、状态
        if (!newUser.getUserName().equals(oldUser.getUserName())
                || !newUser.getUserPn().equals(oldUser.getUserPn())
                || newUser.getCityId() != oldUser.getCityId()
                || newUser.getStatus() != oldUser.getStatus()) {
            i = maintenanceshopMapper.updateById(newUser);
            if (i != 1) {
                throw new BusinessException(ErrorCodeEnum.GL99990108);
            }
        }
    }

}
