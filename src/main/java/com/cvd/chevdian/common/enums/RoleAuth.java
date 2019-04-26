package com.cvd.chevdian.common.enums;


import com.cvd.chevdian.common.constant.DBDict;

public enum RoleAuth {

    ADMIN(DBDict.USER_TYPE_ADMIN, "管理员"),

    AGENT(DBDict.USER_TYPE_AGENT, "代理"),

    CHANNEL(DBDict.USER_TYPE_CHANNEL, "渠道"),;
    private int code;
    private String msg;

    public String msg() {
        return msg;
    }

    public int code() {
        return code;
    }

    RoleAuth(int code, String msg) {
        this.code = code;
        this.msg = msg;
    }
}
