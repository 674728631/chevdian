package com.cvd.chevdian.common.constant;

public class GlobalConstant {

    /**
     * session存储用户key
     */
    public static final String LOGIN_USER = "loginUser";

    /**
     * redis存smsCode的前缀
     */
    public final static String CODE_SIZE_PRE = "size";

    /**
     * redis存储微信 access_token 的key
     */
    public final static String WEIXIN_ACCESSTOKEN = "weixin_accessToken";

    /**
     * oss存储邀请代理二维码文件夹
     */
    public static final String OSS_AGENT_IMG_URL = "agentQR/";
    /**
     * oss存储邀请互助用户文件夹
     */
    public static final String OSS_CAR_IMG_URL = "maintenanceshop/qrcode/";

    /**
     * 时间格式
     */
    public static final String LONG_FORMAT = "yyyyMMddHHmmss";

    public static final String DEFAULT_FORMAT = "yyyy-MM-dd HH:mm:ss";


    public static final String UNKNOWN = "unknown";
    public static final String X_FORWARDED_FOR = "X-Forwarded-For";
    public static final String X_REAL_IP = "X-Real-IP";
    public static final String PROXY_CLIENT_IP = "Proxy-Client-IP";
    public static final String WL_PROXY_CLIENT_IP = "WL-Proxy-Client-IP";
    public static final String HTTP_CLIENT_IP = "HTTP_CLIENT_IP";
    public static final String HTTP_X_FORWARDED_FOR = "HTTP_X_FORWARDED_FOR";


    public static final String LOCALHOST_IP = "127.0.0.1";
    public static final String LOCALHOST_IP_16 = "0:0:0:0:0:0:0:1";
    public static final int MAX_IP_LENGTH = 15;
    public static final String COMMA = ",";

    public static final int EXCEPTION_CAUSE_MAX_LENGTH = 2048;
    public static final int EXCEPTION_MESSAGE_MAX_LENGTH = 2048;
}