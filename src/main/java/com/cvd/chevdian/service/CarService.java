package com.cvd.chevdian.service;

import com.cvd.chevdian.bean.distribute.UserDistributor;
import com.cvd.chevdian.bean.vo.Car;
import com.github.pagehelper.PageInfo;

import java.util.List;
import java.util.Map;

public interface CarService {

    /**
     * 分页展示所有用户车辆
     */
    PageInfo<Car> getCarUserByDifferentUserType(UserDistributor userDistributor);

    /**
     * 车辆详细信息
     */
    Car showCarDetail(Integer carId);

    /**
     * 修改车辆信息
     */
    void modifyCarInfo(Car carInfo);

    /**
     * 获取邀请用户二维码
     */
    UserDistributor getInviteQRcode(UserDistributor userDistributor) throws Exception;

    /**
     * 保险公司列表
     */
    List<Map<String, Object>> getInsuranceList();

    /**
     * 获取可充值车辆列表
     */
    PageInfo<Car> getRechargeList(UserDistributor loginUser);

    /**
     * 查询车辆年度充值记录
     */
    Map<String, Object> getPayAmount(String carId) throws Exception;
}
