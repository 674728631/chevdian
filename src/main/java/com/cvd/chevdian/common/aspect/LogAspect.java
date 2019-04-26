package com.cvd.chevdian.common.aspect;

import com.cvd.chevdian.bean.distribute.UserDistributor;
import com.cvd.chevdian.bean.system.OperationLogDto;
import com.cvd.chevdian.common.annotation.LogAnnotation;
import com.cvd.chevdian.common.constant.GlobalConstant;
import com.cvd.chevdian.common.util.JacksonUtil;
import com.cvd.chevdian.common.util.RequestUtil;
import com.cvd.chevdian.common.wrapper.Wrapper;
import com.cvd.chevdian.mapper.system.OperationLogMapper;
import eu.bitwalker.useragentutils.UserAgent;
import lombok.extern.slf4j.Slf4j;
import org.aspectj.lang.JoinPoint;
import org.aspectj.lang.ProceedingJoinPoint;
import org.aspectj.lang.annotation.*;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.core.task.TaskExecutor;
import org.springframework.stereotype.Component;
import org.springframework.web.context.request.RequestContextHolder;
import org.springframework.web.context.request.ServletRequestAttributes;

import javax.annotation.Resource;
import javax.servlet.http.HttpServletRequest;
import java.lang.reflect.Method;
import java.util.Date;

/**
 * The class Log aspect.
 */
@Slf4j
@Aspect
@Component
public class LogAspect {

    private ThreadLocal<Date> threadLocal = new ThreadLocal<>();
    private ThreadLocal<Long> threadLocal2 = new ThreadLocal<>();

    @Autowired
    private OperationLogMapper operationLogMapper;

    @Resource
    private TaskExecutor taskExecutor;

    private static final int MAX_SIZE = 2000;

    @Pointcut("@annotation(com.cvd.chevdian.common.annotation.LogAnnotation)")
    public void logAnnotation() {
    }

    @Pointcut("execution(* com.cvd.chevdian.controller..*(..))")
    public void controllerAspect() {
    }

    @Before("logAnnotation()")
    public void doBefore() {
        this.threadLocal.set(new Date(System.currentTimeMillis()));
    }

    @Before("controllerAspect()")
    public void apiBefore(final JoinPoint joinPoint) {
        this.threadLocal2.set(System.currentTimeMillis());
        try {
            HttpServletRequest request = ((ServletRequestAttributes) RequestContextHolder.getRequestAttributes()).getRequest();
            String uri = request.getRequestURI();
            log.info("================={}/{}.{} 开始执行=====================", uri, joinPoint.getTarget().getClass().getName(), joinPoint.getSignature().getName());
//            log.info("=={} 入参 {}==", uri, getRequestData(joinPoint));
        } catch (Exception e) {
            log.error("", e);
        }
    }

    @AfterReturning(pointcut = "logAnnotation()", returning = "returnValue")
    public void doAfter(final JoinPoint joinPoint, final Object returnValue) {
        if (returnValue instanceof Wrapper) {
            Wrapper result = (Wrapper) returnValue;
            if (result.getCode() == Wrapper.SUCCESS_CODE) {
                this.handleLog(joinPoint, result);
            }
        }
    }

    @AfterReturning(pointcut = "controllerAspect()", returning = "returnValue")
    public void apiAfter(final Object returnValue) {
        final Long startTime = this.threadLocal2.get();
        final Long endTime = System.currentTimeMillis();
        threadLocal2.remove();
        try {
            HttpServletRequest request = ((ServletRequestAttributes) RequestContextHolder.getRequestAttributes()).getRequest();
            String uri = request.getRequestURI();
            log.info("================={} 执行完成=====================", uri);
            log.info("===={} 耗时 {} ms====", uri, (endTime - startTime));
            log.info(String.valueOf(returnValue));
        } catch (Exception e) {
            log.error("", e);
        }
    }

//    @Around(value = "controllerAspect()")
//    public String around(ProceedingJoinPoint joinPoint) {
//        String result = null;
//        String uri = null;
//        long start = System.currentTimeMillis();
//        try {
//            HttpServletRequest request = ((ServletRequestAttributes) RequestContextHolder.getRequestAttributes()).getRequest();
//            uri = request.getRequestURI();
//            log.info("================={}/{}.{} 开始执行=====================", uri, joinPoint.getTarget().getClass().getName(), joinPoint.getSignature().getName());
//            log.info("=={} 入参 {}==", uri, getRequestData(joinPoint));
//            result = (String) joinPoint.proceed();
//            long end = System.currentTimeMillis();
//            log.info("===={}耗时{} ms====", uri, (end - start));
//            log.info("================={} 执行完成=====================", uri);
//            log.info(result);
//        } catch (Throwable throwable) {
//            long end = System.currentTimeMillis();
//            log.error("===={} 耗时 {} ms====", uri, (end - start));
//            log.error("================={} 执行异常=====================", uri);
//            log.error("{}", throwable);
//        }
//        return result;
//    }

    private void handleLog(final JoinPoint joinPoint, final Object result) {
        final Date startTime = this.threadLocal.get();
        try {
            final Date endTime = new Date(System.currentTimeMillis());
            HttpServletRequest request = ((ServletRequestAttributes) RequestContextHolder.getRequestAttributes()).getRequest();
            final UserAgent userAgent = UserAgent.parseUserAgentString(request.getHeader("User-Agent"));
            String requestURI = request.getRequestURI();
            threadLocal.remove();
            taskExecutor.execute(() -> {
                LogAnnotation relog = giveController(joinPoint);
                UserDistributor loginUser = (UserDistributor) request.getSession().getAttribute(GlobalConstant.LOGIN_USER);
                if (relog == null) {
                    return;
                }
                OperationLogDto operationLogDto = new OperationLogDto();
                operationLogDto.setClassName(joinPoint.getTarget().getClass().getName());
                operationLogDto.setMethodName(joinPoint.getSignature().getName());
                operationLogDto.setExcuteTime(endTime.getTime() - startTime.getTime());
                operationLogDto.setStartTime(startTime);
                operationLogDto.setEndTime(endTime);
                operationLogDto.setIp(RequestUtil.getRemoteAddr(request));
                operationLogDto.setOs(userAgent.getOperatingSystem().getName());
                operationLogDto.setBrowser(userAgent.getBrowser().getName());
                operationLogDto.setRequestUrl(requestURI);
                operationLogDto.setCreatedTime(new Date());
                if (loginUser != null) {
                    operationLogDto.setCreator(loginUser.getUserName());
                    operationLogDto.setCreatorId(loginUser.getId());
                }
                operationLogDto.setLogType(relog.logType().getType());
                operationLogDto.setLogName(relog.logType().getName());
                getControllerMethodDescription(relog, operationLogDto, result, joinPoint);
                this.operationLogMapper.saveLog(operationLogDto);
            });
        } catch (Exception ex) {
            log.error("获取注解类出现异常={}", ex.getMessage(), ex);
        }
    }

    private void getControllerMethodDescription(LogAnnotation relog, OperationLogDto operationLog, Object result, JoinPoint joinPoint) {
        if (relog.isSaveRequestData()) {
            setRequestData(operationLog, joinPoint);
        }
        if (relog.isSaveResponseData()) {
            setResponseData(operationLog, result);
        }
    }

    private void setResponseData(OperationLogDto requestLog, Object result) {
        try {
            requestLog.setResponseData(String.valueOf(result));
        } catch (Exception e) {
            log.error("获取响应数据,出现错误={}", e.getMessage(), e);
        }
    }

    private void setRequestData(OperationLogDto uacLog, JoinPoint joinPoint) {
        uacLog.setRequestData(getRequestData(joinPoint));
    }

    /**
     * 是否存在注解, 如果存在就记录日志
     */
    private static LogAnnotation giveController(JoinPoint joinPoint) {
        Method[] methods = joinPoint.getTarget().getClass().getDeclaredMethods();
        String methodName = joinPoint.getSignature().getName();
        if (null != methods && 0 < methods.length) {
            for (Method met : methods) {
                LogAnnotation relog = met.getAnnotation(LogAnnotation.class);
                if (null != relog && methodName.equals(met.getName())) {
                    return relog;
                }
            }
        }
        return null;
    }

    private String getRequestData(JoinPoint joinPoint) {
        try {
            Object[] args = joinPoint.getArgs();
            if (args.length == 0) {
                return null;
            }
            Object[] parameter = new Object[args.length];
            int index = 0;
            for (Object object : args) {
                if (object instanceof HttpServletRequest)
                    parameter[index] = "request";
                else
                    parameter[index] = object;
                index++;
            }
            String requestData = JacksonUtil.toJsonWithFormat(parameter);

            if (requestData.length() > MAX_SIZE) {
                requestData = requestData.substring(MAX_SIZE);
            }
            return requestData;
        } catch (Exception e) {
            log.error("获取响应数据,出现错误={}", e.getMessage(), e);
        }
        return null;
    }

}
