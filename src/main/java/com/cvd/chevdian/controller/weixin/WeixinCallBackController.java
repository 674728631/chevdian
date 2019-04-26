package com.cvd.chevdian.controller.weixin;

import com.cvd.chevdian.bean.weixin.InMessage;
import com.cvd.chevdian.bean.weixin.OutMessage;
import com.cvd.chevdian.common.annotation.NeedLogin;
import com.cvd.chevdian.common.util.SecurityUtil;
import com.cvd.chevdian.config.WeixinProperties;
import com.cvd.chevdian.service.impl.weixin.WxCoreService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.MediaType;
import org.springframework.web.bind.annotation.*;

import javax.servlet.http.HttpServletRequest;
import java.util.Arrays;

@RestController
@RequestMapping("/wx")
@Slf4j
@NeedLogin(isNeed = false)
public class WeixinCallBackController {

    @Autowired
    WeixinProperties weixinProperties;

    @Autowired
    WxCoreService wxCoreService;

    /**
     * 处理微信接入认证
     */
    @GetMapping(produces = MediaType.APPLICATION_JSON_UTF8_VALUE)
    public String validate(String signature, String timestamp, String nonce, String echostr) {
        // 1.将入参排序
        log.info("微信接入认证===========================>");
        try {
            String[] arr = {timestamp, nonce, weixinProperties.getToken()};
            Arrays.sort(arr);
            // 将入参拼装准备加密
            StringBuilder sb = new StringBuilder();
            for (String s : arr) {
                sb.append(s);
            }
            // 将加密后的字符串进行比较，判断是否来自微信
            if (SecurityUtil.sha1(sb.toString()).equals(signature)) {
                log.info("认证成功===========================>");
                return echostr;
            }
        } catch (Exception e) {
            log.error("认证异常", e);
            return "false";
        }
        log.info("认证失败===========================>");
        return "false";
    }

    /**
     * 微信回调
     */
    @PostMapping(produces = MediaType.TEXT_XML_VALUE)
    public OutMessage handleMessage(@RequestBody InMessage msg, HttpServletRequest request) {
        log.info("微信回调内容===>" + msg);
        // 通过公众号进入的请求，openid放入session
        request.getSession().setAttribute("fromUserName", msg.getFromUserName());
        return wxCoreService.process(msg);
    }

}
