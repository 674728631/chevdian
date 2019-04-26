package com.cvd.chevdian.common.util;


import com.aliyuncs.CommonRequest;
import com.aliyuncs.CommonResponse;
import com.aliyuncs.DefaultAcsClient;
import com.aliyuncs.IAcsClient;
import com.aliyuncs.exceptions.ClientException;
import com.aliyuncs.http.MethodType;
import com.aliyuncs.profile.DefaultProfile;
import com.cvd.chevdian.common.enums.SmsTypeEnum;
import com.cvd.chevdian.config.AliyunProperties;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

@Component
@Slf4j
public class SmsDemo {

    @Autowired
    AliyunProperties aliyunProperties;

    /**
     * 阿里短信
     */
    public void sendSms(SmsTypeEnum type, String phoneNo, String... parm) {
        DefaultProfile profile = DefaultProfile.getProfile("default", aliyunProperties.getAccessKeyId(), aliyunProperties.getAccessKeySecret());
        IAcsClient client = new DefaultAcsClient(profile);

        CommonRequest request = new CommonRequest();
        request.setMethod(MethodType.POST);
        request.setDomain("dysmsapi.aliyuncs.com");
        request.setVersion("2017-05-25");
        request.setAction("SendSms");
        request.putQueryParameter("SignName", "车v店");
        request.putQueryParameter("PhoneNumbers", phoneNo);
        switch (type) {
            case VerificationCode:
                requestParam(request, parm);
                break;
            default:
        }
        try {
            CommonResponse response = client.getCommonResponse(request);
            log.info("发送结果={}", response.getData());
        } catch (ClientException e) {
            log.error("", e);
        }
    }

    /**
     * 发送验证码
     */
    private void requestParam(CommonRequest request, String... parm) {
        String msg = String.format("{\"code\":\"%s\"}", parm[0]);
        request.putQueryParameter("TemplateParam", msg);
        request.putQueryParameter("TemplateCode", "SMS_112795252");
    }

}