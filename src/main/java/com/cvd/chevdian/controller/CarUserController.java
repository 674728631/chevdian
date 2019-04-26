package com.cvd.chevdian.controller;

import com.cvd.chevdian.bean.distribute.UserDistributor;
import com.cvd.chevdian.bean.vo.Car;
import com.cvd.chevdian.common.annotation.ApiAuth;
import com.cvd.chevdian.common.annotation.LogAnnotation;
import com.cvd.chevdian.common.constant.GlobalConstant;
import com.cvd.chevdian.common.enums.LogTypeEnum;
import com.cvd.chevdian.common.enums.RoleAuth;
import com.cvd.chevdian.common.wrapper.WrapMapper;
import com.cvd.chevdian.common.wrapper.Wrapper;
import com.cvd.chevdian.service.CarService;
import com.github.pagehelper.PageInfo;
import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.MediaType;
import org.springframework.web.bind.annotation.*;

import javax.servlet.http.HttpServletRequest;
import java.util.List;

@ApiAuth(role = {RoleAuth.AGENT, RoleAuth.CHANNEL, RoleAuth.ADMIN})
@Api(tags = "客户API")
@RestController
@RequestMapping(value = "/car", produces = MediaType.APPLICATION_JSON_UTF8_VALUE)
public class CarUserController {

    @Autowired
    CarService carService;

    @ApiAuth(role = {RoleAuth.AGENT, RoleAuth.CHANNEL})
    @ApiOperation("邀请用户")
    @GetMapping("/invited")
    public Wrapper<UserDistributor> inviteUser(HttpServletRequest request) throws Exception {
        UserDistributor userDistributor = (UserDistributor) request.getSession().getAttribute(GlobalConstant.LOGIN_USER);
        return WrapMapper.ok(carService.getInviteQRcode(userDistributor));
    }

    @ApiOperation("查看车辆列表")
    @GetMapping("/list")
    public Wrapper<PageInfo<Car>> showUser(HttpServletRequest request,
                                           @RequestParam(name = "pageNo", required = false, defaultValue = "1") Integer pageNo,
                                           @RequestParam(name = "pageSize", required = false, defaultValue = "10") Integer pageSize) {
        UserDistributor userDistributor = (UserDistributor) request.getSession().getAttribute(GlobalConstant.LOGIN_USER);
        userDistributor.setPageNo(pageNo);
        userDistributor.setPageSize(pageSize);
        return WrapMapper.ok(carService.getCarUserByDifferentUserType(userDistributor));
    }

    @ApiOperation("查看车辆详情")
    @GetMapping("/detail/{id}")
    public Wrapper<Car> showUser(@PathVariable("id") Integer carId) {
        return WrapMapper.ok(carService.showCarDetail(carId));
    }

    @LogAnnotation(logType = LogTypeEnum.OPERATION_LOG, isSaveRequestData = true)
    @ApiOperation("修改车辆资料")
    @PostMapping("/edit")
    public Wrapper showUser(@RequestBody Car carInfo) {
        carService.modifyCarInfo(carInfo);
        return WrapMapper.ok();
    }

    @ApiOperation("保险公司列表")
    @GetMapping("/list/insurance")
    public Wrapper getInsuranceList() {
        return WrapMapper.ok(carService.getInsuranceList());
    }

    @ApiOperation("可充值列表")
    @GetMapping("/list/recharge")
    public Object rechargeList(HttpServletRequest request,
                               @RequestParam(name = "pageNo", required = false, defaultValue = "1") Integer pageNo,
                               @RequestParam(name = "pageSize", required = false, defaultValue = "10") Integer pageSize) {
        UserDistributor loginUser = (UserDistributor) request.getSession().getAttribute(GlobalConstant.LOGIN_USER);
        loginUser.setPageNo(pageNo);
        loginUser.setPageSize(pageSize);
        return WrapMapper.ok(carService.getRechargeList(loginUser));
    }
}
