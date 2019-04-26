package com.cvd.chevdian.interceptor;


import com.cvd.chevdian.bean.distribute.UserDistributor;
import com.cvd.chevdian.common.annotation.NeedLogin;
import com.cvd.chevdian.common.constant.GlobalConstant;
import com.cvd.chevdian.common.enums.ErrorCodeEnum;
import com.cvd.chevdian.common.exception.BusinessException;
import com.cvd.chevdian.service.UserDistributorService;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.method.HandlerMethod;
import org.springframework.web.servlet.HandlerInterceptor;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.lang.reflect.Method;

/**
 * 登录验证拦截器
 */
@Slf4j
public class LoginInterceptor implements HandlerInterceptor {

    @Autowired
    UserDistributorService userDistributorService;

    /**
     * 在请求处理之前进行调用（Controller方法调用之前） preHandle只要返回true才会继续执行后边的流程
     *
     * @param request
     * @param response
     * @param handler
     * @return
     * @throws Exception
     */
    @Override
    public boolean preHandle(HttpServletRequest request, HttpServletResponse response, Object handler) {
        // 如果不是映射到方法直接通过
        if (!(handler instanceof HandlerMethod))
            return true;
        log.debug("LoginInterceptor url " + request.getRequestURL());
        log.debug("LoginInterceptor uri " + request.getRequestURI());
        log.debug("LoginInterceptor method " + request.getMethod());

        HandlerMethod handlerMethod = (HandlerMethod) handler;
        Method method = handlerMethod.getMethod();
        // 判断接口是否需要登录
        NeedLogin methodAnnotation = method.getAnnotation(NeedLogin.class);
        Class clazz = handlerMethod.getBeanType();
        NeedLogin classAnnotation = (NeedLogin) clazz.getAnnotation(NeedLogin.class);

        // 有 @NeedLogin 注解且 isNeed 为true，需要需要认证
        // 先判断方法上的注解，再判断类注解，方法注解覆盖类注解
        boolean isNeed = true;
        if (null != methodAnnotation)
            isNeed = methodAnnotation.isNeed();
        else if (null != classAnnotation)
            isNeed = classAnnotation.isNeed();

        if (isNeed) {
            // 获取授权
//            Object openId = request.getSession().getAttribute("fromUserName");
            Object openId = "o7dkZ6MFKon6wJFUfLEC1vgbK2qk";
            if (org.springframework.util.StringUtils.isEmpty(openId))
                throw new BusinessException(ErrorCodeEnum.GL99991000);
            // 判断是否绑定
            UserDistributor user = userDistributorService.queryByOpenId(openId.toString());
            if (null == user)
                throw new BusinessException(ErrorCodeEnum.GL99990106);
            if (!StringUtils.isEmpty(user.getUserPn()))
                request.getSession().setAttribute(GlobalConstant.LOGIN_USER, userDistributorService.formatUserBase64Field(user));
            else
                throw new BusinessException(ErrorCodeEnum.GL99990104);
        }
        return true;
    }
}
