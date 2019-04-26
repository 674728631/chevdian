package com.cvd.chevdian.common.wrapper;


/**
 * 结果模板
 *
 * @author admin
 */
public class OutDataUtil {

    /**
     * 返回成功信息
     *
     * @param data
     * @return
     */
    public static OutData<Object> getOK(Object data) {
        OutData<Object> out = new OutData<>();
        out.setCode(0);
        out.setSuccess("T");
        out.setInfo("成功");
        out.setData(data);
        return out;
    }

    /**
     * 返回成功
     *
     * @return
     */
    public static OutData<Object> getOK() {
        return getOK(null);
    }

    /**
     * 返回错误
     *
     * @param code
     * @param info
     * @return
     */
    public static OutData<Object> getError(Integer code, String info) {
        OutData<Object> out = new OutData<>();
        out.setCode(code);
        out.setSuccess("F");
        out.setInfo(info);
        return out;
    }
}
