package com.cvd.chevdian.service.impl;

import com.alibaba.fastjson.JSONObject;
import com.cvd.chevdian.bean.distribute.UserDistributor;
import com.cvd.chevdian.bean.vo.ChannelVo;
import com.cvd.chevdian.bean.vo.ScreenVo;
import com.cvd.chevdian.common.constant.DBDict;
import com.cvd.chevdian.common.constant.GlobalConstant;
import com.cvd.chevdian.common.enums.ErrorCodeEnum;
import com.cvd.chevdian.common.exception.BusinessException;
import com.cvd.chevdian.common.util.FileUtil;
import com.cvd.chevdian.common.util.HttpUtil;
import com.cvd.chevdian.common.util.WeiXinUtils;
import com.cvd.chevdian.mapper.MaintenanceshopMapper;
import com.cvd.chevdian.mapper.UserDistributorMapper;
import com.cvd.chevdian.service.AgentService;
import com.cvd.chevdian.service.ChannelService;
import com.cvd.chevdian.service.UserDistributorService;
import com.github.pagehelper.PageHelper;
import com.github.pagehelper.PageInfo;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;


@Slf4j
@Service
public class ChannelServiceImpl implements ChannelService {

    @Autowired
    WeiXinUtils weiXinUtils;
    @Autowired
    MaintenanceshopMapper maintenanceshopMapper;
    @Autowired
    UserDistributorService userDistributorService;
    @Autowired
    UserDistributorMapper userDistributorMapper;
    @Autowired
    AgentService agentService;

    @Value("${chevhuzhu.url}")
    private String chevhuzhu;

    @Override
    @Transactional(rollbackFor = Exception.class)
    public UserDistributor getInviteAgentQRcode(UserDistributor user) throws Exception {
        // 2. 获取渠道信息
        Integer channelId = user.getChannelId();
        if (StringUtils.isEmpty(user.getAgentQRcode())) {
            String eventKey = String.format("c_%s", channelId);
            String qrcodeFileName = weiXinUtils.createForeverQrcode(eventKey, weiXinUtils.getAccessToken());
            user.setAgentQRcode(qrcodeFileName);
            UserDistributor newUser = new UserDistributor();
            newUser.setId(user.getId());
            newUser.setAgentQRcode(qrcodeFileName);
            userDistributorMapper.updateEntityModel(newUser);
        }
        String url = FileUtil.getImgURLFromOSS(GlobalConstant.OSS_AGENT_IMG_URL, user.getAgentQRcode());
        user.setAgentQRcode(url);
        return user;
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void saveChannel(UserDistributor channel) throws Exception {
        log.info("新建渠道用户user={}", channel);
        Integer channelId = channel.getChannelId();
        ChannelVo shop;
        // 传入id为空，新建渠道
        if (null == channelId) {
            // 添加新渠道
            shop = saveChannelToCvhz(channel, DBDict.SHOP_TYPE_CHANNEL);
            channelId = shop.getId();
            channel.setChannelId(channelId);
            channel.setUserQRcode(shop.getQrcode());
        }
        // 更新渠道为分销渠道
        shop = new ChannelVo();
        shop.setId(channelId);
        shop.setFlag(10);
        int i = maintenanceshopMapper.updateFlagById(shop);
        if (i != 1)
            throw new BusinessException(ErrorCodeEnum.GL99990108);
        // 保存渠道用户
        channel.setUserType(DBDict.USER_TYPE_CHANNEL);
        channel.setParentId(0);
        i = userDistributorMapper.saveEntitySingle(channel);
        if (i != 1)
            throw new BusinessException(ErrorCodeEnum.GL99990108);
    }

    @Override
    public Boolean checkDuplicateName(String channelName) {
        List<UserDistributor> userList = userDistributorService.getChannelByName(channelName);
        if (userList.size() > 0)
            return true;
        return false;
    }

    @Override
    public Boolean checkDuplicateTel(String channelTel) {
        UserDistributor user = userDistributorService.queryByPhoneNo(channelTel);
        if (null != user)
            return true;
        return false;
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void editChannel(UserDistributor channel) {
        agentService.editAgent(channel);
    }

    @Override
    public UserDistributor getChannelById(Integer channelId) {
        return userDistributorService.queryById(channelId);
    }

    @Override
    public List<ChannelVo> selectLikeName(String channelName) {
        return maintenanceshopMapper.selectLikeName(channelName);
    }

    @Override
    public PageInfo<UserDistributor> getChannelList(UserDistributor userDistributor) {
        userDistributor.setId(null);
        return PageHelper.startPage(userDistributor.getPageNo(), userDistributor.getPageSize())
                .doSelectPageInfo(() -> userDistributorMapper.selectListByType(userDistributor, DBDict.USER_TYPE_CHANNEL, new ScreenVo()));
    }

    @Override
    public ChannelVo saveChannelToCvhz(UserDistributor channel, Integer type) {
        JSONObject json = new JSONObject();
        json.put("name", channel.getUserName());
        json.put("uuidArea", channel.getCityId());
        json.put("tel", channel.getUserPn());
        json.put("type", type);
        log.info("json={}", json);
        String rs = HttpUtil.doPostJson(chevhuzhu + "/maintenanceshop/saveChannel", json.toJSONString());
        json = JSONObject.parseObject(rs);
        log.info("/saveChannel接口返回结果==》{}", json);
        if ("0".equals(json.getString("code"))) {
            // 根据添加渠道名查询渠道id
            Integer id = Integer.valueOf(json.getString("data"));
            ChannelVo shop = maintenanceshopMapper.queryById(id);
            if (null == shop)
                throw new BusinessException(ErrorCodeEnum.GL99990110);
            log.info("根据渠道名称查询结果==》{}", shop);
            return shop;
        } else
            throw new BusinessException(ErrorCodeEnum.GL99990111);
    }

}
