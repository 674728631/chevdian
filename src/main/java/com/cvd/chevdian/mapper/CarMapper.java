package com.cvd.chevdian.mapper;

import com.cvd.chevdian.bean.vo.Car;
import com.cvd.chevdian.bean.vo.ScreenVo;
import org.apache.ibatis.annotations.Param;

import java.util.List;
import java.util.Map;
import java.util.Set;

public interface CarMapper {

    /**
     * 获取所有分销车辆
     */
    Car getCarById(Integer carId);

    /**
     * 根据渠道/代理id获取名下所有车辆
     */
    List<Car> getAllUserByShopId(@Param("id") Integer id, @Param("screenVo") ScreenVo screenVo);

    /**
     * 查询车辆城市信息
     */
    Car getCarCity(Integer carId);

    /**
     * 查询车辆保险信息
     */
    Car getCarInsurance(Integer carId);

    /**
     * 查询车辆充值信息
     */
    Car getCarRecharge(Integer carId);

    /**
     * 更新车主姓名
     */
    int modifyCarOwner(Car carInfo);

    /**
     * 更新保险信息
     */
    int modifyCarInsurance(Car carInfo);

    /**
     * 插入保险信息
     */
    int saveCarInsurance(Car carInfo);

    /**
     * 车牌，手机号模糊搜索--渠道，代理
     */
    List<Car> search(@Param("searchInfo") String searchInfo, @Param("shopIdList") Set<Integer> shopIdList);

    /**
     * 根据用户id获取所有邀请的用户
     */
    List<Car> getInvitedUserOfUser(@Param("customerIdList") Set<Integer> customerIdList, @Param("screenVo") ScreenVo screenVo);

    /**
     * 根据类型获取充值/退款金额
     */
    Map<String, Object> getAmtByType(@Param("types") List<Integer> types, @Param("carIdList") List<Integer> carIdList, @Param("screenVo") ScreenVo screenVo);

    /**
     * 保险公司列表
     */
    List<Map<String, Object>> getInsuranceList();

    /**
     * 查询渠道/代理可充值用户-- 非包年,额度>0,未冻结
     */
    List<Car> getValidRechargeCars(@Param("shopIdList") List<Integer> shopIdList);

    /**
     * 查询用户id获取所有邀请的用户中可充值的用户
     */
    List<Car> getValidRechargeInvitedUser(@Param("customerIdList") Set<Integer> customerIdList);

}
