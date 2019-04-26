package com.cvd.chevdian.bean.system;

import com.cvd.chevdian.common.constant.GlobalConstant;
import lombok.Data;
import org.apache.commons.lang3.StringUtils;

import java.io.Serializable;
import java.util.Arrays;
import java.util.Date;


/**
 * The class Operation log dto.
 */
@Data
public class OperationLogDto implements Serializable {
    private static final long serialVersionUID = -5606865665592482762L;
    private Long id;

    /**
     * 日志类型
     */
    private String logType;

    /**
     * 日志类型名称
     */
    private String logName;

    /**
     * 操作系统
     */
    private String os;

    /**
     * 浏览器类型
     */
    private String browser;

    /**
     * IP地址
     */
    private String ip;

    /**
     * 物理地址
     */
    private String mac;

    /**
     * 请求参数
     */
    private String requestData;

    /**
     * 请求地址
     */
    private String requestUrl;

    /**
     * 响应结果
     */
    private String responseData;

    /**
     * 类名
     */
    private String className;

    /**
     * 方法名
     */
    private String methodName;

    /**
     * 开始时间
     */
    private Date startTime;

    /**
     * 结束时间
     */
    private Date endTime;

    /**
     * 耗时,秒
     */
    private Long excuteTime;

    /**
     * 创建人
     */
    private String creator;

    /**
     * 创建人ID
     */
    private Integer creatorId;

    /**
     * 创建时间
     */
    private Date createdTime;

    /**
     * 异常信息(通过exception.getMessage()获取到的内容)
     */
    private String exceptionMessage;

    /**
     * 异常原因(通过exception.getCause()获取到的内容)
     */
    private String exceptionCause;

    /**
     * 异常类型
     */
    private String exceptionSimpleName;

    /**
     * 异常堆栈信息
     */
    private String exceptionStack;

    public OperationLogDto getOperationLogDto(Exception ex) {
        String message = ex.getMessage();
        if (StringUtils.isNotBlank(message) && message.length() > GlobalConstant.EXCEPTION_MESSAGE_MAX_LENGTH) {
            this.exceptionMessage = StringUtils.substring(message, 0, GlobalConstant.EXCEPTION_MESSAGE_MAX_LENGTH) + "...";
        }
        this.exceptionSimpleName = ex.getClass().getSimpleName();
        String cause = ex.getCause() == null ? null : ex.getCause().toString();
        if (StringUtils.isNotBlank(cause) && cause.length() > GlobalConstant.EXCEPTION_CAUSE_MAX_LENGTH) {
            this.exceptionCause = StringUtils.substring(cause, 0, GlobalConstant.EXCEPTION_CAUSE_MAX_LENGTH) + "...";
        }
        this.exceptionStack = Arrays.toString(ex.getStackTrace());
        return this;
    }
}