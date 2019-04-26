package com.cvd.chevdian.service;

import com.cvd.chevdian.bean.distribute.UserDistributor;
import com.cvd.chevdian.bean.vo.ScreenVo;
import com.cvd.chevdian.bean.vo.SearchInfo;
import com.github.pagehelper.PageInfo;

import java.util.List;
import java.util.Map;

public interface UserDistributorService {

    /**
     * 保存用户
     */
    int save(UserDistributor userDistributor) throws Exception;

    int save(Map<String, Object> user) throws Exception;

    /**
     * 根据openid查询用户
     */
    UserDistributor queryByOpenId(String openId);

    /**
     * 根据手机号查询用户
     */
    UserDistributor queryByPhoneNo(String phoneNo);

    /**
     * 登录逻辑处理
     *
     * @param openId   微信
     * @param phoneNo  手机号
     * @param code     验证码
     * @param userName 真实信息
     * @return User
     */
    UserDistributor login(String openId, String phoneNo, String code, String userName) throws Exception;

    /**
     * 绑定微信信息
     */
    void bindUserWxInfo(String openid, UserDistributor user);

    /**
     * nickname,username解密处理
     *
     * @param userDistributor User
     * @return User
     */
    UserDistributor formatUserBase64Field(UserDistributor userDistributor);


    /**
     * 根据id查询代理/渠道/管理员详情
     */
    UserDistributor queryById(Integer id);

    /**
     * 根据username查询
     */
    List<UserDistributor> getChannelByName(String name);

    /**
     * 搜索
     */
    PageInfo<UserDistributor> searchByTypeAndInfo(UserDistributor loginUser, SearchInfo search);

    /**
     * 渠道数量统计
     * @param screenVo
     */
    Integer channelCount(ScreenVo screenVo);

    /**
     * 代理数量统计
     */
    Integer selectAgentCountByUserType(UserDistributor user, ScreenVo screenVo);

    /**
     * 根据角色Id查询
     */
    List<UserDistributor> selectByRoleId(Integer roleId);
}
