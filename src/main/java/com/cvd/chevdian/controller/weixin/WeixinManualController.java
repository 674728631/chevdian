package com.cvd.chevdian.controller.weixin;


import com.alibaba.fastjson.JSONObject;
import com.cvd.chevdian.common.annotation.NeedLogin;
import com.cvd.chevdian.common.constant.WeixinConstants;
import com.cvd.chevdian.common.util.FileUtil;
import com.cvd.chevdian.common.util.HttpUtil;
import com.cvd.chevdian.common.util.WeiXinUtils;
import com.cvd.chevdian.common.wrapper.WrapMapper;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.MediaType;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping(value = "/manual", produces = MediaType.APPLICATION_JSON_UTF8_VALUE)
@NeedLogin(isNeed = false)
public class WeixinManualController {

    @Autowired
    WeiXinUtils weiXinUtils;

    @GetMapping("/createMenu")
    public Object createMenu(@RequestParam String token) {
        if (token.equals("liuzhentest")) {
            String menu = FileUtil.getFileJsonToString("/weixin/menu.json");
            String result = HttpUtil.doPostJson(WeixinConstants.CREATE_MENU_URL.replace("ACCESS_TOKEN", weiXinUtils.getAccessToken()), menu);
            JSONObject json = JSONObject.parseObject(result);
            if ("0".equals(json.get("errcode").toString())) {
                return WrapMapper.ok(json);
            }
        }
        return WrapMapper.error("创建失败");
    }

    @GetMapping("/hello")
    public Object hello(){
        return WrapMapper.ok("hello,welcom");
    }
}
