package com.cvd.chevdian.config;


import lombok.Data;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.stereotype.Component;

@ConfigurationProperties(prefix = "weixin")
@Component
@Data
public class WeixinProperties {

    // 第三方用户唯一凭证
    private String appid;
    // 第三方用户唯一凭证密钥
    private String appsecret;
    // 商户号
    private String partner;
    // 秘钥
    private String partnerKey;
    // 微信服务器配置的请求验证token
    private String token;

}
