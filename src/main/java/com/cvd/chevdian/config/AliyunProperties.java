package com.cvd.chevdian.config;


import lombok.Data;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.stereotype.Component;

@ConfigurationProperties(prefix = "aliyun")
@Component
@Data
public class AliyunProperties {

    private String accessKeyId;

    private String accessKeySecret;

    // 同一个手机号每天获取验证码次数
    private Integer smsCodeCount;

    // 短信验证码有效时间(分钟)
    private Long smsCodeTime;

    private String endpoint;

    private String bucketName;
}
