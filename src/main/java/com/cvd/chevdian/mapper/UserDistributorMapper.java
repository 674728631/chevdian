package com.cvd.chevdian.mapper;

import com.cvd.chevdian.bean.distribute.UserDistributor;
import com.cvd.chevdian.bean.vo.ScreenVo;
import org.apache.ibatis.annotations.Param;

import java.util.List;
import java.util.Map;

public interface UserDistributorMapper extends BaseMapper<UserDistributor> {

    /**
     * 根据微信openid查询用户
     */
    UserDistributor queryByOpenId(String openId);

    /**
     * 根据手机号查询用户
     */
    UserDistributor queryByPhoneNo(String phoneNo);

    /**
     * 更新用户
     */
    int updateEntityModel(UserDistributor userDistributor);

    /**
     * 根据渠道id查询所有用户
     */
    Map<String, Object> queryByChannelId(Map<String, Object> map);

    /**
     * 获取渠道代理列表;
     * 查询代理详情
     * 管理员--查询所有渠道/代理
     * 代理--啥都查不到
     * 渠道--代理
     */
    List<UserDistributor> selectListByType(@Param("user") UserDistributor user, @Param("type") Integer type, @Param("screenVo") ScreenVo screenVo);

    /**
     * 根据name询渠道
     */
    List<UserDistributor> selectChannelByName(String name);

    /**
     * 根据Id查询
     */
    UserDistributor queryById(Integer id);

    /**
     * 搜索：
     * 渠道--代理
     * 代理--无
     * 管理员--代理/渠道
     */
    List<UserDistributor> searchByTypeAndInfo(@Param("searchInfo") String searchInfo, @Param("searchType") Integer searchType, @Param("user") UserDistributor loginUser);

    /**
     * 渠道数量
     */
    Integer channelCount(ScreenVo screenVo);

    /**
     * 代理数量
     */
    Integer selectAgentCountByUserType(@Param("user") UserDistributor userDistributor, @Param("screenVo") ScreenVo screenVo);

    /**
     * 首字母检索:
     * 管理员-- 所有代理 / 渠道;
     * 渠道-- 渠道（自己）/ 代理;
     * 代理-- 代理（自己）
     */
    List<UserDistributor> selectUserByFirstPinYinChar(@Param("user") UserDistributor user, @Param("type") Integer type, @Param("screenVo") ScreenVo screenVo, @Param("letter") String letter);

    /**
     * 根据角色查询
     */
    List<UserDistributor> selectByRoleId(Integer roleId);

}
