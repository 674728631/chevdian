package com.cvd.chevdian.controller;

import com.cvd.chevdian.bean.distribute.UserDistributor;
import com.cvd.chevdian.common.annotation.LogAnnotation;
import com.cvd.chevdian.common.annotation.NeedLogin;
import com.cvd.chevdian.common.constant.GlobalConstant;
import com.cvd.chevdian.common.enums.ErrorCodeEnum;
import com.cvd.chevdian.common.enums.LogTypeEnum;
import com.cvd.chevdian.common.enums.SmsTypeEnum;
import com.cvd.chevdian.common.exception.BusinessException;
import com.cvd.chevdian.common.util.RandomUtil;
import com.cvd.chevdian.common.util.RedisUtil;
import com.cvd.chevdian.common.util.SmsDemo;
import com.cvd.chevdian.common.util.ValidateUtil;
import com.cvd.chevdian.common.wrapper.WrapMapper;
import com.cvd.chevdian.common.wrapper.Wrapper;
import com.cvd.chevdian.config.AliyunProperties;
import com.cvd.chevdian.service.UserDistributorService;
import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.MediaType;
import org.springframework.web.bind.annotation.*;

import javax.servlet.http.HttpServletRequest;
import java.util.Map;
import java.util.concurrent.TimeUnit;

@Api(tags = "登录API")
@RestController
@Slf4j
@RequestMapping(produces = MediaType.APPLICATION_JSON_UTF8_VALUE)
@NeedLogin(isNeed = false)
public class LoginController {

    @Autowired
    SmsDemo smsDemo;

    @Autowired
    RedisUtil redisUtil;

    @Autowired
    AliyunProperties aliyunProperties;

    @Autowired
    UserDistributorService userDistributorService;

//    @LogAnnotation(logType = LogTypeEnum.LOGIN_LOG, isSaveRequestData = true,isSaveResponseData = true)
    @ApiOperation("登录接口")
    @PostMapping(value = "/login")
    public Wrapper login(HttpServletRequest request, @RequestBody Map<String,Object> loginInfo) throws Exception {
                String phoneNo = (String) loginInfo.get("phoneNo");
                String code = (String) loginInfo.get("code");
                String userName = (String) loginInfo.get("userName");
        String openId = String.valueOf(request.getSession().getAttribute("fromUserName"));
//        String openId = "o7dkZ6MFKon6wJFUfLEC1vgbK2qk";
        request.getSession().setAttribute("fromUserName",openId);
        return WrapMapper.ok(userDistributorService.login(openId, phoneNo, code, userName));
    }

    @ApiOperation("获取4位数字验证码")
    @GetMapping(value = "/msm/verificationCode")
    public Wrapper getVerificationCode(@RequestParam String phoneNo) {
        if(!ValidateUtil.isMobileNumber(phoneNo))
            throw new IllegalArgumentException("手机号错误");
        // 限制次数
        // 1.从缓存获取当天次数
        String count = redisUtil.getString(GlobalConstant.CODE_SIZE_PRE + phoneNo);
        if (null == count || Long.valueOf(count) < aliyunProperties.getSmsCodeCount()) {
            // 生成
            String verificationCode = RandomUtil.createNumberCode(4);
            log.debug("{} code===>{}", phoneNo, verificationCode);
            // 保存
            redisUtil.putToString(phoneNo, verificationCode, aliyunProperties.getSmsCodeTime(), TimeUnit.MINUTES); // 保存5分钟
            // 发送
            smsDemo.sendSms(SmsTypeEnum.VerificationCode, phoneNo, verificationCode);
            // 2.发送成功次数+1
            redisUtil.incrementAndExpire(GlobalConstant.CODE_SIZE_PRE + phoneNo);
        } else {
            // 超过次数了
            log.info("{} 验证码获取超过次数了", phoneNo);
            throw new BusinessException(ErrorCodeEnum.GL99990101);
        }
        // 判断手机号是否绑定过
        UserDistributor oldUser = userDistributorService.queryByPhoneNo(phoneNo);
        if (oldUser != null && oldUser.getUserName() != null)
            return WrapMapper.ok(1);
        return WrapMapper.ok(0);
    }

}
