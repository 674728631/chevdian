package com.cvd.chevdian.common.enums;


/**
 * The class Error code enum.
 *
 * @author paascloud.net @gmail.com
 */
public enum ErrorCodeEnum {


    GL99990500(500, "未知异常"),

    GL99991000(99991000, "没有获取到OPENID,需要重新授权"),

    GL99990100(99990100, "参数异常"),

    GL99990101(99990101, "验证码获取超过次数了"),

    GL99990102(99990102, "验证码过期了"),

    GL99990103(99990103, "验证码错误"),

    GL99990104(99990104, "登录验证失败"),

    GL99990105(99990105, "该微信号已被绑定"),

    GL99990106(99990106, "该微信号没有对应的用户"),

    GL99990107(99990107, "用户没有该权限"),

    GL99990108(99990108, "操作失败"),

    GL99990109(99990109, "该手机号未注册"),

    GL99990110(99990110, "创建渠道失败，获取maintenanceshop表信息失败"),

    GL99990111(99990111, "创建渠道失败，/saveChannel接口调用失败"),

//    GL99990112(99990112, "更新渠道失败"),
//
//    GL99990113(99990113, "保存用户失败"),
//
//    GL99990114(99990114, "更新代理失败"),
//
//    GL99990115(99990115, "创建角色失败"),
//
//    GL99990116(99990116, "更新角色失败"),

    GL99990117(99990117, "有用户在用该角色，请先核实再操作"),

    GL99990118(99990118, "有车辆在包年中，不能充值"),

    GL99990119(99990119, "微信支付通知查询订单失败！！！"),

    ;
    private int code;
    private String msg;

    /**
     * Msg string.
     *
     * @return the string
     */
    public String msg() {
        return msg;
    }

    /**
     * Code int.
     *
     * @return the int
     */
    public int code() {
        return code;
    }

    ErrorCodeEnum(int code, String msg) {
        this.code = code;
        this.msg = msg;
    }

    /**
     * Gets enum.
     *
     * @param code the code
     * @return the enum
     */
    public static ErrorCodeEnum getEnum(int code) {
        for (ErrorCodeEnum ele : ErrorCodeEnum.values()) {
            if (ele.code() == code) {
                return ele;
            }
        }
        return null;
    }
}
