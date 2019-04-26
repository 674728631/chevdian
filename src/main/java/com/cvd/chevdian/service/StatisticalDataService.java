package com.cvd.chevdian.service;

import com.cvd.chevdian.bean.distribute.UserDistributor;
import com.cvd.chevdian.bean.vo.Car;
import com.cvd.chevdian.bean.vo.ScreenVo;

import java.util.List;
import java.util.Map;

public interface StatisticalDataService {

    /**
     * 渠道数量
     * @param screenVo 筛选条件
     */
    Integer getChannelCount(ScreenVo screenVo);

    /**
     * 代理数量
     */
    Integer getAgentCountByUserType(UserDistributor user, ScreenVo screenVo);

    /**
     * 用户数
     */
    List<Car> getCarListByUserType(UserDistributor user, ScreenVo screenVo);

    /**
     * 充值金额
     */
    String recharger(UserDistributor user, ScreenVo screenVo);

    /**
     * 退款金额
     */
    String refund(UserDistributor user, ScreenVo screenVo);

    /**
     *  自己的一级客户统计列表
     */
    List<Car> lookCarList(UserDistributor userDistributor, ScreenVo screenVo);

    /**
     * 根据首字母检索
     */
    List<Map<String, Object>> getStatisticalByLetter(UserDistributor userDistributor, ScreenVo screenVo, Integer type, String letter);

    /**
     * 根据点击标签ID 查询互助用户列表
     */
    List<Car> lookCarListById(UserDistributor userDistributor, ScreenVo screenVo, Integer type, Integer id);
}
