package com.cvd.chevdian.controller;


import com.cvd.chevdian.bean.distribute.UserDistributor;
import com.cvd.chevdian.bean.vo.Car;
import com.cvd.chevdian.bean.vo.ScreenVo;
import com.cvd.chevdian.common.annotation.ApiAuth;
import com.cvd.chevdian.common.constant.GlobalConstant;
import com.cvd.chevdian.common.enums.RoleAuth;
import com.cvd.chevdian.common.wrapper.WrapMapper;
import com.cvd.chevdian.common.wrapper.Wrapper;
import com.cvd.chevdian.service.StatisticalDataService;
import io.swagger.annotations.Api;
import io.swagger.annotations.ApiImplicitParam;
import io.swagger.annotations.ApiOperation;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.MediaType;
import org.springframework.web.bind.annotation.*;

import javax.servlet.http.HttpServletRequest;
import java.util.List;
import java.util.Map;

@Api(tags = "统计API")
@RestController
@RequestMapping(value = "/statistical", produces = MediaType.APPLICATION_JSON_UTF8_VALUE)
public class StatisticalDataController {

    @Autowired
    StatisticalDataService statisticalDataService;

    @ApiAuth(role = {RoleAuth.ADMIN})
    @ApiOperation("统计渠道数量")
    @PostMapping("/count/channel")
    public Wrapper<Integer> channelCount(@RequestBody ScreenVo screenVo) {
        return WrapMapper.ok(statisticalDataService.getChannelCount(screenVo));
    }

    @ApiAuth(role = {RoleAuth.ADMIN, RoleAuth.CHANNEL})
    @ApiOperation("根据登录用户类型统计代理数量")
    @PostMapping("/count/agent")
    public Wrapper<Integer> agentCount(HttpServletRequest request,
                                       @RequestBody ScreenVo screenVo) {
        UserDistributor userDistributor = (UserDistributor) request.getSession().getAttribute(GlobalConstant.LOGIN_USER);
        return WrapMapper.ok(statisticalDataService.getAgentCountByUserType(userDistributor, screenVo));
    }

    @ApiOperation("根据登录用户类型统计下级互助用户数量")
    @PostMapping("/count/car")
    public Wrapper<Integer> carCount(HttpServletRequest request,
                                     @RequestBody ScreenVo screenVo) {
        UserDistributor userDistributor = (UserDistributor) request.getSession().getAttribute(GlobalConstant.LOGIN_USER);
        return WrapMapper.ok(statisticalDataService.getCarListByUserType(userDistributor, screenVo).size());
    }

    @ApiOperation("充值金额")
    @PostMapping("/amt/agent")
    public Wrapper<String> rechargeData(HttpServletRequest request,
                                        @RequestBody ScreenVo screenVo) {
        UserDistributor userDistributor = (UserDistributor) request.getSession().getAttribute(GlobalConstant.LOGIN_USER);
        return WrapMapper.ok(statisticalDataService.recharger(userDistributor, screenVo));
    }

    @ApiOperation("退款金额")
    @PostMapping("/amt/refund")
    public Wrapper<String> refundData(HttpServletRequest request,
                                      @RequestBody ScreenVo screenVo) {
        UserDistributor userDistributor = (UserDistributor) request.getSession().getAttribute(GlobalConstant.LOGIN_USER);
        return WrapMapper.ok(statisticalDataService.refund(userDistributor, screenVo));
    }

    @ApiOperation("统计页面点击客户lab，跳转页面展示客户列表")
    @PostMapping("/car/page")
    public Wrapper<List<Car>> carList(HttpServletRequest request,
                                      @RequestBody ScreenVo screenVo) {
        UserDistributor userDistributor = (UserDistributor) request.getSession().getAttribute(GlobalConstant.LOGIN_USER);
        return WrapMapper.ok(statisticalDataService.lookCarList(userDistributor, screenVo));
    }

    @ApiOperation("客户统计列表页面，渠道/代理人 按钮选项")
    @PostMapping("/car/page/{type}")
    public Wrapper carList(HttpServletRequest request,
                           @RequestBody ScreenVo screenVo,
                           @PathVariable(value = "type") Integer type) {
        UserDistributor userDistributor = (UserDistributor) request.getSession().getAttribute(GlobalConstant.LOGIN_USER);
        return WrapMapper.ok(statisticalDataService.getStatisticalByLetter(userDistributor, screenVo, type, null));
    }


    @ApiOperation("根据首字母检索渠道、代理发展的客户数据统计")
    @PostMapping("/car/page/{type}/{char}")
    public Wrapper getStatisticalByLetter(HttpServletRequest request,
                                          @RequestBody ScreenVo screenVo,
                                          @PathVariable("type") Integer type,
                                          @PathVariable("char") String letter) {
        UserDistributor userDistributor = (UserDistributor) request.getSession().getAttribute(GlobalConstant.LOGIN_USER);
        return WrapMapper.ok(statisticalDataService.getStatisticalByLetter(userDistributor, screenVo, type, letter));
    }

    @ApiOperation("点击 渠道/代理人 选项标签查看详情列表 ")
    @PostMapping("/car/lab/{type}/{id}")
    public Wrapper<List<Car>> getStatisticalByLetter(HttpServletRequest request,
                                          @RequestBody ScreenVo screenVo,
                                          @PathVariable("type") Integer type,
                                          @PathVariable("id") Integer id) {
        UserDistributor userDistributor = (UserDistributor) request.getSession().getAttribute(GlobalConstant.LOGIN_USER);
        return WrapMapper.ok(statisticalDataService.lookCarListById(userDistributor, screenVo, type, id));
    }
}
