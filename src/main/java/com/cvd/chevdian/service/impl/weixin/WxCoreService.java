package com.cvd.chevdian.service.impl.weixin;

import com.cvd.chevdian.bean.distribute.UserDistributor;
import com.cvd.chevdian.bean.vo.ChannelVo;
import com.cvd.chevdian.bean.weixin.InMessage;
import com.cvd.chevdian.bean.weixin.OutMessage;
import com.cvd.chevdian.common.constant.DBDict;
import com.cvd.chevdian.common.constant.WeixinConstants;
import com.cvd.chevdian.common.util.WeiXinUtils;
import com.cvd.chevdian.mapper.MaintenanceshopMapper;
import com.cvd.chevdian.service.ChannelService;
import com.cvd.chevdian.service.UserDistributorService;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

@Component
@Slf4j
public class WxCoreService {

    @Autowired
    UserDistributorService userDistributorService;
    @Autowired
    ChannelService channelService;

    @Autowired
    WeiXinUtils weiXinUtils;

    @Autowired
    MaintenanceshopMapper maintenanceshopMapper;

    /**
     * 微信回调核心逻辑处理
     */
    public OutMessage process(InMessage inMessage) {
        OutMessage outMessage = new OutMessage(inMessage);
        try {
            // 菜单事件、推送事件不做处理
            if (WeixinConstants.EVENT_TYPE_VIEW.equalsIgnoreCase(inMessage.getEvent())
                    || WeixinConstants.EVENT_TYPE_TEMP.equalsIgnoreCase(inMessage.getEvent()))
                return outMessage;

            // 关注事件处理
            if (!StringUtils.isEmpty(inMessage.getEvent())
                    && inMessage.getEvent().equals(WeixinConstants.EVENT_TYPE_SUBSCRIBE)) {
                outMessage = subscribe(inMessage);
            }
            // 文本消息处理
            else if (StringUtils.isEmpty(inMessage.getEvent())
                    && inMessage.getMsgType().equals(WeixinConstants.REQ_MESSAGE_TYPE_TEXT)) {
                outMessage.setMsgType("text");
                outMessage.setContent("欢迎关注");
            }
            // 取关
        } catch (Exception e) {
            log.error("", e);
            outMessage.setMsgType("text");
            outMessage.setContent("欢迎关注");
        }
        return outMessage;
    }

    private OutMessage subscribe(InMessage inMessage) throws Exception {
        String openId = inMessage.getFromUserName();
        log.info("关注用户openId={}", openId);
        OutMessage outMessage = new OutMessage(inMessage);
        // 1.推送欢迎语
        outMessage.setMsgType("text");
        outMessage.setContent("欢迎关注");
        // 判断是否关注过
        UserDistributor user = userDistributorService.queryByOpenId(openId);
        if (null != user)
            return outMessage;

        // 2.1 微信信息
        user = new UserDistributor();
        userDistributorService.bindUserWxInfo(openId, user);

        // qrscene_c_1
        if (StringUtils.isEmpty(inMessage.getEventKey())) {
            // 自然关注 渠道/管理员
//            user.setUserType(DBDict.USER_TYPE_CHANNEL);
            userDistributorService.save(user);
            return outMessage;
        }
        // 扫码关注 -- 代理
        // 1. 获取渠道id
        Integer channelId = Integer.valueOf(inMessage.getEventKey().split("_")[2]);
        log.info("上级渠道id={}", channelId);
        // 2.2 城市信息
        ChannelVo channel = maintenanceshopMapper.queryById(channelId);
        if (null == channel)
            throw new RuntimeException("微信关注，渠道id查询失败");

        user.setCityId(channel.getUuidArea());
        user.setParentId(channelId);
        user.setUserType(DBDict.USER_TYPE_AGENT);
        userDistributorService.save(user);
        return outMessage;
    }
}
