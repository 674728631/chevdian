package com.cvd.chevdian.mapper;

import org.apache.ibatis.annotations.Param;

import java.util.List;
import java.util.Map;

public interface PayResultMapper {

    /**
     * 根据订单号查询车辆信息
     */
    Map<String, Object> findCarByRecordRechargeId(@Param("id") String id);

    /**
     * 根据用户id查询用户信息及车辆信息
     */
    Map<String, Object> findUser(Map<String, Object> map);

    /**
     * 保存用户日志
     */
    int saveUserCustomerLog(Map<String, Object> map);

    /**
     * 根据车辆信息查询用户信息
     */
    Map<String, Object> selectUserByCarId(Map<String, Object> map);

    int updateCar(Map<String,Object> map);

    Map<String,Object> findEntitySingle();

    Integer updateData(Map<String,Object> map);

    Map<String,Object> findDictionarySingle(Map<String, Object> map);

    List<Map<String, Object>> findMore(Map<String, Object> map);

    int wechatLoginUpdateModel(Map<String, Object> map);

    int wechatLoginSaveSingle(Map<String, Object> map);

    Map<String, Object> findCar(Map<String, Object> map);

    Map<String, Object> getUserInfoById(@Param("customerId") Integer customerId);

    int userUpdateModel(Map<String,Object> map);

    Map<String, Object> findCarById(Integer carId);
}
