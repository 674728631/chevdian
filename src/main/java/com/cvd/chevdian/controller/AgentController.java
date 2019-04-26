package com.cvd.chevdian.controller;

import com.cvd.chevdian.bean.distribute.UserDistributor;
import com.cvd.chevdian.common.annotation.ApiAuth;
import com.cvd.chevdian.common.annotation.LogAnnotation;
import com.cvd.chevdian.common.constant.GlobalConstant;
import com.cvd.chevdian.common.enums.ErrorCodeEnum;
import com.cvd.chevdian.common.enums.LogTypeEnum;
import com.cvd.chevdian.common.enums.RoleAuth;
import com.cvd.chevdian.common.exception.BusinessException;
import com.cvd.chevdian.common.wrapper.WrapMapper;
import com.cvd.chevdian.common.wrapper.Wrapper;
import com.cvd.chevdian.service.AgentService;
import com.cvd.chevdian.service.ChannelService;
import com.cvd.chevdian.service.UserDistributorService;
import com.github.pagehelper.PageInfo;
import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import io.swagger.annotations.ApiResponse;
import io.swagger.annotations.ApiResponses;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.MediaType;
import org.springframework.web.bind.annotation.*;

import javax.servlet.http.HttpServletRequest;

@ApiAuth(role = {RoleAuth.CHANNEL,RoleAuth.ADMIN})
@Api(tags = "代理API")
@ApiResponses({@ApiResponse(code = 200, message = "OK", response = Wrapper.class)})
@RestController
@RequestMapping(value = "/agent", produces = MediaType.APPLICATION_JSON_UTF8_VALUE)
public class AgentController {

    @Autowired
    AgentService agentService;
    @Autowired
    ChannelService channelService;

    @ApiAuth(role = {RoleAuth.CHANNEL})
    @ApiOperation("邀请代理")
    @GetMapping(value = "/invited")
    public Wrapper<UserDistributor> inviteAgent(HttpServletRequest request) throws Exception {
        UserDistributor userDistributor = (UserDistributor) request.getSession().getAttribute(GlobalConstant.LOGIN_USER);
        return WrapMapper.ok(channelService.getInviteAgentQRcode(userDistributor));
    }

    @ApiOperation("查看代理人列表")
    @GetMapping("/list")
    public Wrapper<PageInfo<UserDistributor>> showAgentList(HttpServletRequest request,
                                                            @RequestParam(name = "pageNo", required = false, defaultValue = "1") Integer pageNo,
                                                            @RequestParam(name = "pageSize", required = false, defaultValue = "10") Integer pageSize) {
        UserDistributor userDistributor = (UserDistributor) request.getSession().getAttribute(GlobalConstant.LOGIN_USER);
        userDistributor.setPageNo(pageNo);
        userDistributor.setPageSize(pageSize);
        return WrapMapper.ok(agentService.getAgentByChannelId(userDistributor));
    }

    @ApiOperation("查看代理人详情")
    @GetMapping("/detail/{id}")
    public Wrapper<UserDistributor> showAgentDetail(@PathVariable("id") Integer agentId) {
        return WrapMapper.ok(agentService.getAgentById(agentId));
    }

    @LogAnnotation(logType = LogTypeEnum.OPERATION_LOG, isSaveRequestData = true)
    @ApiOperation("修改代理人资料")
    @PostMapping("/edit")
    public Wrapper editAgent(@RequestBody UserDistributor user) {
        agentService.editAgent(user);
        return WrapMapper.ok();
    }
}
