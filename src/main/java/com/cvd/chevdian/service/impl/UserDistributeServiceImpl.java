package com.cvd.chevdian.service.impl;

import com.cvd.chevdian.bean.distribute.UserDistributor;
import com.cvd.chevdian.bean.vo.ChannelVo;
import com.cvd.chevdian.bean.vo.ScreenVo;
import com.cvd.chevdian.bean.vo.SearchInfo;
import com.cvd.chevdian.common.constant.DBDict;
import com.cvd.chevdian.common.constant.GlobalConstant;
import com.cvd.chevdian.common.enums.ErrorCodeEnum;
import com.cvd.chevdian.common.exception.BusinessException;
import com.cvd.chevdian.common.util.Base64;
import com.cvd.chevdian.common.util.RedisUtil;
import com.cvd.chevdian.common.util.WeiXinUtils;
import com.cvd.chevdian.mapper.CarMapper;
import com.cvd.chevdian.mapper.UserDistributorMapper;
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
import org.springframework.util.CollectionUtils;

import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;


@Service
@Slf4j
public class UserDistributeServiceImpl implements UserDistributorService {

    @Autowired
    RedisUtil redisUtil;
    @Autowired
    UserDistributorMapper userDistributorMapper;
    @Autowired
    WeiXinUtils weiXinUtils;
    @Autowired
    CarMapper carMapper;
    @Autowired
    ChannelService channelService;


    @Value("${chevhuzhu.url}")
    private String chevhuzhu;


    @Override
    @Transactional(rollbackFor = Exception.class)
    public int save(UserDistributor userDistributor) throws Exception {
        int i = userDistributorMapper.saveEntitySingle(userDistributor);
        if (i != 1)
            throw new BusinessException(ErrorCodeEnum.GL99990108);
        return i;
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public int save(Map<String, Object> user) throws Exception {
        int i = userDistributorMapper.saveSingle(user);
        if (i != 1)
            throw new BusinessException(ErrorCodeEnum.GL99990108);
        return i;
    }

    @Override
    public UserDistributor queryByOpenId(String openId) {
        return userDistributorMapper.queryByOpenId(openId);
    }

    @Override
    public UserDistributor queryByPhoneNo(String phoneNo) {
        return userDistributorMapper.queryByPhoneNo(phoneNo);
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public UserDistributor login(String openId, String phoneNo, String code, String userName) throws Exception {
        /* 参数判断 */
        if (StringUtils.isAnyEmpty(phoneNo, code) || code.length() != 4 || phoneNo.length() != 11)
            throw new IllegalArgumentException("手机号或验证码不能为空");
        /* 验证验证码 */
        String redisCode = redisUtil.getString(phoneNo);
        if (StringUtils.isEmpty(redisCode))
            throw new BusinessException(ErrorCodeEnum.GL99990102);
        if (!redisCode.equals(code))
            throw new BusinessException(ErrorCodeEnum.GL99990103);

        /* 判断是否绑定 */
        UserDistributor user = queryByOpenId(openId);
        if (null == user)
            throw new BusinessException(ErrorCodeEnum.GL99990106);
        String oldPhoneNo = user.getUserPn();

        if (StringUtils.isEmpty(oldPhoneNo)) { // 微信未绑定
            UserDistributor oldUser = queryByPhoneNo(phoneNo);
            // 手机号未绑定
            if (null == oldUser) {
                // 判断是否代理--非代理不能登录
                if (DBDict.USER_TYPE_AGENT != user.getUserType())
                    throw new BusinessException(ErrorCodeEnum.GL99990109);
                // 处理userName
                if (StringUtils.isAnyEmpty(userName))
                    throw new IllegalArgumentException("用户名不能为空");
//                user.setUserName(Base64.getBase64(userName));
                user.setUserName(userName);
                // 绑定
                user.setUserPn(phoneNo);
                // 生成车V互助端渠道
                ChannelVo shop = channelService.saveChannelToCvhz(user, DBDict.USER_TYPE_AGENT);
                user.setChannelId(shop.getId());
                user.setUserQRcode(shop.getQrcode());
            } else { // 手机号已绑定，管理员/渠道/重新绑定微信账号
                userDistributorMapper.deleteModel(user.getId());// 解绑
                // 绑定新微信
                bindUserWxInfo(openId, oldUser);
                user = oldUser;
            }
            userDistributorMapper.updateEntityModel(user);
        } else  // 微信已绑定
            throw new BusinessException(ErrorCodeEnum.GL99990105);

        redisUtil.delect(phoneNo);
        // 返回用户信息和角色信息
        return formatUserBase64Field(user);
    }

    @Transactional(rollbackFor = Exception.class)
    public int updateUserById(UserDistributor userDistributor) {
        return userDistributorMapper.updateEntityModel(userDistributor);
    }

    @Transactional(rollbackFor = Exception.class)
    public int deleteUserById(UserDistributor userDistributor) throws Exception {
        return userDistributorMapper.deleteModel(userDistributor.getId());
    }

    @Override
    public void bindUserWxInfo(String openid, UserDistributor user) {
        // 2.1 微信信息
        Map<String, String> wxInfo = weiXinUtils.getNickname(openid);
        user.setOpenId(wxInfo.get("openid"));
        if (!CollectionUtils.isEmpty(wxInfo)) {
            user.setNickName(wxInfo.get("nickname_base64"));
            user.setPortrait(wxInfo.get("headimgurl"));
            return;
        }
        log.error("getNickname 失败重试1次");
        wxInfo = weiXinUtils.getNickname(openid);
        if (!CollectionUtils.isEmpty(wxInfo)) {
            user.setNickName(wxInfo.get("nickname_base64"));
            user.setPortrait(wxInfo.get("headimgurl"));
        }
    }

    @Override
    public UserDistributor formatUserBase64Field(UserDistributor userDistributor) {
        String nikeName = userDistributor.getNickName();
//        String userName = userDistributor.getUserName();
        if (StringUtils.isNoneEmpty(nikeName))
            userDistributor.setNickName(Base64.getFromBase64(nikeName));
//        if (StringUtils.isNoneEmpty(userName))
//            userDistributor.setUserName(Base64.getFromBase64(userName));
        return userDistributor;
    }


    @Override
    public UserDistributor queryById(Integer agentId) {
        return userDistributorMapper.queryById(agentId);
    }

    @Override
    public List<UserDistributor> getChannelByName(String name) {
        return userDistributorMapper.selectChannelByName(name);
    }

    @Override
    public PageInfo<UserDistributor> searchByTypeAndInfo(UserDistributor loginUser, SearchInfo search) {
        String searchInfo = search.getSearchInfo();
        Integer searchType = search.getSearchType();
        Integer pageNum = search.getPageNo();
        Integer pageSize = search.getPageSize();

        if (searchType == DBDict.SEARCH_CAR) {
            Set<Integer> shopIdList = new HashSet<>();
            shopIdList.add(loginUser.getChannelId() == null ? 0 : loginUser.getChannelId());
            if (DBDict.USER_TYPE_ADMIN == loginUser.getUserType()) {
                // 所有代理
                List<UserDistributor> agentList = userDistributorMapper.selectListByType(loginUser, DBDict.USER_TYPE_AGENT, new ScreenVo());
                // 所有渠道
                List<UserDistributor> channelList = userDistributorMapper.selectListByType(loginUser, DBDict.USER_TYPE_CHANNEL, new ScreenVo());
                agentList.addAll(channelList);
                agentList.forEach(u -> shopIdList.add(u.getChannelId()));
            }
            return PageHelper.startPage(pageNum, pageSize)
                    .doSelectPageInfo(() -> carMapper.search(searchInfo, shopIdList));
        } else if (searchType == DBDict.SEARCH_AGENT || searchType == DBDict.SEARCH_CHANNEL) {
            return PageHelper.startPage(pageNum, pageSize)
                    .doSelectPageInfo(() -> userDistributorMapper.searchByTypeAndInfo(searchInfo, searchType, loginUser));
        }
        return null;
    }

    @Override
    public Integer channelCount(ScreenVo screenVo) {
        return userDistributorMapper.channelCount(screenVo);
    }

    @Override
    public Integer selectAgentCountByUserType(UserDistributor user, ScreenVo screenVo) {
        return userDistributorMapper.selectAgentCountByUserType(user, screenVo);
    }

    @Override
    public List<UserDistributor> selectByRoleId(Integer roleId) {
        return userDistributorMapper.selectByRoleId(roleId);
    }
}
