package com.cvd.chevdian.common.exception;

import com.cvd.chevdian.bean.distribute.UserDistributor;
import com.cvd.chevdian.bean.system.OperationLogDto;
import com.cvd.chevdian.common.constant.GlobalConstant;
import com.cvd.chevdian.common.enums.ErrorCodeEnum;
import com.cvd.chevdian.common.enums.LogTypeEnum;
import com.cvd.chevdian.common.exception.BusinessException;
import com.cvd.chevdian.common.wrapper.WrapMapper;
import com.cvd.chevdian.common.wrapper.Wrapper;
import com.cvd.chevdian.mapper.system.OperationLogMapper;
import eu.bitwalker.useragentutils.UserAgent;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.core.task.TaskExecutor;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.ResponseBody;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestControllerAdvice;
import org.springframework.web.context.request.RequestContextHolder;
import org.springframework.web.context.request.ServletRequestAttributes;

import javax.annotation.Resource;
import javax.servlet.http.HttpServletRequest;
import java.util.Date;

/**
 * 全局的的异常拦截器
 *
 * @author paascloud.net @gmail.com
 */
@Slf4j
@RestControllerAdvice
public class GlobalExceptionHandler {
    @Resource
    private TaskExecutor taskExecutor;
    @Autowired
    private OperationLogMapper operationLogMapper;

    /**
     * 参数非法异常.
     *
     * @param e the e
     * @return the wrapper
     */
    @ExceptionHandler(IllegalArgumentException.class)
    @ResponseStatus(HttpStatus.OK)
    @ResponseBody
    public Wrapper illegalArgumentException(IllegalArgumentException e) {
        log.error("参数非法异常={}", e.getMessage(), e);
        return WrapMapper.wrap(ErrorCodeEnum.GL99990100.code(), e.getMessage());
    }

    /**
     * 业务异常.
     *
     * @param e the e
     * @return the wrapper
     */
    @ExceptionHandler(BusinessException.class)
    @ResponseStatus(HttpStatus.OK)
    @ResponseBody
    public Wrapper businessException(BusinessException e) {
        HttpServletRequest request = ((ServletRequestAttributes) RequestContextHolder.getRequestAttributes()).getRequest();
        String uri = request.getRequestURI();
        log.info("================={} 执行完成=====================", uri);
        log.error("业务异常={}", e.getMessage(), e);
        return WrapMapper.wrap(e.getCode() == 0 ? Wrapper.ERROR_CODE : e.getCode(), e.getMessage());
    }


    /**
     * 全局异常.
     *
     * @param e the e
     * @return the wrapper
     */
    @ExceptionHandler(Exception.class)
    @ResponseStatus(HttpStatus.INTERNAL_SERVER_ERROR)
    @ResponseBody
    public Wrapper exception(Exception e) {
        try {
            log.info("保存全局异常信息 ex={}", e.getMessage(), e);
            HttpServletRequest request = ((ServletRequestAttributes) RequestContextHolder.getRequestAttributes()).getRequest();
            UserDistributor loginUser = (UserDistributor) request.getSession().getAttribute(GlobalConstant.LOGIN_USER);
            final UserAgent userAgent = UserAgent.parseUserAgentString(request.getHeader("User-Agent"));
            taskExecutor.execute(() -> {
                OperationLogDto operationLogDto = new OperationLogDto().getOperationLogDto(e);
                operationLogDto.setRequestUrl(request.getRequestURI());
                operationLogDto.setCreatedTime(new Date());
                if (loginUser != null) {
                    operationLogDto.setCreator(loginUser.getUserName());
                    operationLogDto.setCreatorId(loginUser.getId());
                }
                operationLogDto.setOs(userAgent.getOperatingSystem().getName());
                operationLogDto.setBrowser(userAgent.getBrowser().getName());
                operationLogDto.setLogType(LogTypeEnum.EXCEPTION_LOG.getType());
                operationLogDto.setLogName(LogTypeEnum.EXCEPTION_LOG.getName());
                this.operationLogMapper.saveLog(operationLogDto);
            });
        } catch (Exception ex) {
            log.error("保存全局异常信息失败 ex=={}", ex);
        }
        return WrapMapper.error();
    }
}
