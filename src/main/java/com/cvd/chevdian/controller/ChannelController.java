package com.cvd.chevdian.controller;

import com.cvd.chevdian.bean.distribute.UserDistributor;
import com.cvd.chevdian.bean.vo.ChannelVo;
import com.cvd.chevdian.common.annotation.ApiAuth;
import com.cvd.chevdian.common.annotation.LogAnnotation;
import com.cvd.chevdian.common.constant.GlobalConstant;
import com.cvd.chevdian.common.enums.LogTypeEnum;
import com.cvd.chevdian.common.enums.RoleAuth;
import com.cvd.chevdian.common.wrapper.WrapMapper;
import com.cvd.chevdian.common.wrapper.Wrapper;
import com.cvd.chevdian.service.ChannelService;
import com.github.pagehelper.PageInfo;
import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.MediaType;
import org.springframework.web.bind.annotation.*;

import javax.servlet.http.HttpServletRequest;
import java.util.List;

@ApiAuth(role = {RoleAuth.ADMIN})
@Api(tags = "渠道API")
@RestController
@RequestMapping(value = "/channel", produces = MediaType.APPLICATION_JSON_UTF8_VALUE)
public class ChannelController {


    @Autowired
    private ChannelService channelService;

    @ApiOperation("分页查看渠道列表")
    @GetMapping(value = "/list")
    public Wrapper<PageInfo<UserDistributor>> listChannel(HttpServletRequest request,
                                                          @RequestParam(name = "pageNo", required = false, defaultValue = "1") Integer pageNo,
                                                          @RequestParam(name = "pageSize", required = false, defaultValue = "10") Integer pageSize) throws Exception {
        UserDistributor userDistributor = (UserDistributor) request.getSession().getAttribute(GlobalConstant.LOGIN_USER);
        userDistributor.setPageNo(pageNo);
        userDistributor.setPageSize(pageSize);
        return WrapMapper.ok(channelService.getChannelList(userDistributor));
    }

    @ApiOperation("查看渠道详细信息")
    @GetMapping(value = "/detail/{id}")
    public Wrapper<UserDistributor> getChannelInfo(@PathVariable("id") Integer channelId) {
        return WrapMapper.ok(channelService.getChannelById(channelId));
    }

    @LogAnnotation(logType = LogTypeEnum.OPERATION_LOG, isSaveRequestData = true)
    @ApiOperation("创建渠道")
    @PostMapping(value = "/save")
    public Wrapper saveChannel(@RequestBody UserDistributor channel) throws Exception {
        channelService.saveChannel(channel);
        return WrapMapper.ok();
    }

    @LogAnnotation(logType = LogTypeEnum.OPERATION_LOG, isSaveRequestData = true)
    @ApiOperation("编辑渠道")
    @PostMapping(value = "/edit")
    public Wrapper editChannel(@RequestBody UserDistributor channel) {
        channelService.editChannel(channel);
        return WrapMapper.ok();
    }


    @ApiOperation("输入渠道名称自动检索关联渠道")
    @GetMapping("/searchByName")
    public Wrapper<List<ChannelVo>> searchByName(@RequestParam("name") String channelName) {
        return WrapMapper.ok(channelService.selectLikeName(channelName));
    }

    @ApiOperation("检查渠道名是否占用")
    @GetMapping("/duplicateCheck/name/{name}")
    public Wrapper<Boolean> checkDuplicateName(@PathVariable("name") String channelName) {
        return WrapMapper.ok(channelService.checkDuplicateName(channelName));
    }

    @ApiOperation("检查手机号是否占用")
    @GetMapping("/duplicateCheck/tel/{tel}")
    public Wrapper<Boolean> checkDuplicateTel(@PathVariable("tel") String channelTel) {
        return WrapMapper.ok(channelService.checkDuplicateTel(channelTel));
    }
}
