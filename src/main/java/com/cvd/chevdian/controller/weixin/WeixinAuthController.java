package com.cvd.chevdian.controller.weixin;

import com.alibaba.fastjson.JSONObject;
import com.cvd.chevdian.common.annotation.NeedLogin;
import com.cvd.chevdian.common.constant.WeixinConstants;
import com.cvd.chevdian.common.enums.ErrorCodeEnum;
import com.cvd.chevdian.common.util.HttpUtil;
import com.cvd.chevdian.common.util.WeiXinUtils;
import com.cvd.chevdian.common.wrapper.WrapMapper;
import com.cvd.chevdian.common.wrapper.Wrapper;
import com.cvd.chevdian.config.WeixinProperties;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.MediaType;
import org.springframework.web.bind.annotation.*;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.UnsupportedEncodingException;
import java.net.URLDecoder;
import java.net.URLEncoder;
import java.util.Map;

/**
 * @author xiaowuge
 * @ClassName: WeixinAuthController
 * @Description: TODO(这里用一句话描述这个类的作用)
 * @date 2018年10月26日 下午2:25:43
 */
@RestController
@RequestMapping(value = "/authorize", produces = MediaType.APPLICATION_JSON_UTF8_VALUE)
@Slf4j
@NeedLogin(isNeed = false)
public class WeixinAuthController {

    @Autowired
    WeixinProperties weixinProperties;

    @Autowired
    WeiXinUtils weiXinUtils;


    @GetMapping(value = "/getOpenid")
    public Wrapper getOpenid(HttpServletRequest request) {
        String openId = String.valueOf(request.getSession().getAttribute("fromUserName"));
        if (StringUtils.isEmpty(openId)) {
            return WrapMapper.error(ErrorCodeEnum.GL99991000);
        } else {
            return WrapMapper.ok();
        }
    }

    @PostMapping(value = "/weixinAuth")
    public Wrapper<String> weixinAuth(HttpServletRequest request ,@RequestBody Map<String,Object> strJson) throws UnsupportedEncodingException {
        log.info("进入用户授权，来源地址====>{}", strJson);
        StringBuffer baseUrl = request.getRequestURL();
        String tempContextUrl = baseUrl.delete(baseUrl.length() - request.getRequestURI().length(), baseUrl.length()).toString();

        log.info("授权回调地址===>{}", tempContextUrl + WeixinConstants.BASE_PATH);
        request.getSession().setAttribute("path", strJson.get("strJson"));
        String uri = WeixinConstants.AUTH_URL
                .replace("APPID", weixinProperties.getAppid())
                .replace("REDIRECT_URI", URLEncoder.encode(tempContextUrl + WeixinConstants.BASE_PATH, "UTF-8"))
                .replace("SCOPE", "snsapi_userinfo")
                .replace("STATE", "1");
        return WrapMapper.ok(uri);
    }

    /**
     * 微信回调
     *
     * @author xiaowuge
     * @date 2018年10月26日
     * @version 1.0
     */
    @GetMapping(value = "/baseRequest")
    public void baseRequest(HttpServletRequest request, HttpServletResponse response) throws Exception {
        StringBuffer baseUrl = request.getRequestURL();
        String tempContextUrl = baseUrl.delete(baseUrl.length() - request.getRequestURI().length(), baseUrl.length()).toString();

        String code = request.getParameter("code");
        String url = WeixinConstants.AUTH_GET_OID
                .replace("APPID", weixinProperties.getAppid())
                .replace("SECRET", weixinProperties.getAppsecret())
                .replace("CODE", code);
        String json = HttpUtil.doGet(url);
        String path2 = URLEncoder.encode(tempContextUrl + "/view/bind.html", "UTF-8");
        if (!StringUtils.isEmpty(json) && request.getSession().getAttribute("path").toString() != null) {
            JSONObject jsonObject = JSONObject.parseObject(json);
            if (jsonObject.containsKey("openid")) {
                request.getSession().setAttribute("fromUserName", jsonObject.getString("openid"));
            }
            response.sendRedirect(request.getSession().getAttribute("path").toString());
            return;
        }
        response.sendRedirect(path2);
    }

    /**
     * js 微信参数
     */
    @PostMapping(value = "/jsInfo")
    public Object wxInfo(String url) throws UnsupportedEncodingException {
        String url_1 = URLDecoder.decode(url, "UTF-8");
        return WrapMapper.ok(weiXinUtils.toWxJsInfo(url_1, weiXinUtils.getAccessToken()));
    }
}
