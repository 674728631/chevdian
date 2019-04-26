package com.cvd.chevdian.service;

import com.cvd.chevdian.bean.distribute.UserDistributor;
import com.cvd.chevdian.bean.vo.ChannelVo;
import com.github.pagehelper.PageInfo;

import java.util.List;

public interface ChannelService {

    /**
     * 渠道列表
     */
    PageInfo<UserDistributor> getChannelList(UserDistributor userDistributor);

    /**
     * 渠道详情
     */
    UserDistributor getChannelById(Integer channelId);

    /**
     * 根据渠道名模糊查询
     */
    List<ChannelVo> selectLikeName(String channelName);

    /**
     * 新建渠道
     */
    void saveChannel(UserDistributor map) throws Exception;

    /**
     * 获取邀请代理二维码
     *
     * @param user 渠道用户
     */
    UserDistributor getInviteAgentQRcode(UserDistributor user) throws Exception;

    /**
     * 检查渠道名是否占用
     */
    Boolean checkDuplicateName(String channelName);

    /**
     * 检查手机号是否占用
     */
    Boolean checkDuplicateTel(String channelTel);

    /**
     * 编辑渠道
     */
    void editChannel(UserDistributor channel);

    /**
     * 保存车V互助端渠道
     */
    ChannelVo saveChannelToCvhz(UserDistributor channel, Integer type);
}
