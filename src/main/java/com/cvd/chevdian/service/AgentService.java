package com.cvd.chevdian.service;

import com.cvd.chevdian.bean.distribute.UserDistributor;
import com.github.pagehelper.PageInfo;

public interface AgentService {

    /**
     * 分页展示所有代理
     */
    PageInfo<UserDistributor> getAgentByChannelId(UserDistributor userDistributor);

    /**
     * 查询代理详情
     */
    UserDistributor getAgentById(Integer agentId);

    /**
     * 修改代理人资料
     */
    void editAgent(UserDistributor user);
}
