package com.cvd.chevdian.common.wrapper;

/**
 * 统一返回对象
 *
 * @param <T>
 * @author admin
 */
public class OutData<T> {
    // 状态码
    private Integer code;
    // 服务返回信息
    private String info;
    // 返回数据
    private T data;
    // 成功与否 T-成功；F-失败
    private String success;

    public Integer getCode() {
        return code;
    }

    public void setCode(Integer code) {
        this.code = code;
    }

    public String getInfo() {
        return info;
    }

    public void setInfo(String info) {
        this.info = info;
    }

    public T getData() {
        return data;
    }

    public void setData(T data) {
        this.data = data;
    }

    public String getSuccess() {
        return success;
    }

    public void setSuccess(String success) {
        this.success = success;
    }

    @Override
    public String toString() {
        return "OutData [code=" + code + ", info=" + info + ", data=" + data + ", success=" + success + "]";
    }

}
