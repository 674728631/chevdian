package com.cvd.chevdian;

import com.cvd.chevdian.common.constant.GlobalConstant;
import com.cvd.chevdian.common.constant.WeixinConstants;
import com.cvd.chevdian.common.enums.SmsTypeEnum;
import com.cvd.chevdian.common.util.FileUtil;
import com.cvd.chevdian.common.util.HttpUtil;
import com.cvd.chevdian.common.util.SmsDemo;
import com.cvd.chevdian.common.util.WeiXinUtils;
import com.cvd.chevdian.config.WeixinProperties;
import com.cvd.chevdian.service.system.RoleMenuService;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.junit4.SpringRunner;

import java.util.Date;
import java.util.HashMap;
import java.util.Map;

@RunWith(SpringRunner.class)
@SpringBootTest
public class ChevdianApplicationTests {

    @Autowired
    WeixinProperties weixinProperties;

    @Autowired
    WeiXinUtils weiXinUtils;

    @Autowired
    SmsDemo smsDemo;

    @Autowired
    RoleMenuService roleMenuService;

    @Test
    public void contextLoads() {
        System.out.println(weixinProperties.getAppid());
        System.out.println(weixinProperties.getAppsecret());
    }

    @Test
    public void getNickname(){
        Map<String,String> user = weiXinUtils.getNickname("o7dkZ6MFKon6wJFUfLEC1vgbK2qk");
        System.out.println(user);
    }

    @Test
    public void createMenu(){
        String menu = FileUtil.getFileJsonToString("E:\\idea_works\\chevdian\\src\\main\\resources\\weixin\\menu.json");
        String result = HttpUtil.doPostJson(WeixinConstants.CREATE_MENU_URL.replace("ACCESS_TOKEN", weiXinUtils.getAccessToken()),menu);
        System.out.println(result);
    }

    @Test
    public void sendSms(){
        smsDemo.sendSms(SmsTypeEnum.VerificationCode,"18284546959");
    }

    @Test
    public void testAtValue(){
        System.out.println("=======================================");
    }

    @Test
    public void testQR() throws Exception {
        String eventKey = String.format("c_%s", 1);
        String file =weiXinUtils.createForeverQrcode(eventKey,weiXinUtils.getAccessToken());
        String url = FileUtil.getImgURLFromOSS(GlobalConstant.OSS_AGENT_IMG_URL, file);
        System.out.println(url);
    }

}
