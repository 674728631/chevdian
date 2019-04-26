package com.cvd.chevdian.interceptor;


import com.cvd.chevdian.bean.distribute.UserDistributor;
import com.cvd.chevdian.common.annotation.ApiAuth;
import com.cvd.chevdian.common.annotation.NeedLogin;
import com.cvd.chevdian.common.constant.GlobalConstant;
import com.cvd.chevdian.common.enums.ErrorCodeEnum;
import com.cvd.chevdian.common.enums.RoleAuth;
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
public class ApiAuthInterceptor implements HandlerInterceptor {

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
        // 判断接口需要角色权限
        ApiAuth methodAnnotation = method.getAnnotation(ApiAuth.class);
        Class clazz = handlerMethod.getBeanType();
        ApiAuth classAnnotation = (ApiAuth) clazz.getAnnotation(ApiAuth.class);

        // 先判断方法上的注解，再判断类注解，方法注解覆盖类注解
        boolean isNeed = false;
        RoleAuth[] roles = null;

        if (null != methodAnnotation) {
            isNeed = methodAnnotation.isNeed();
            roles = methodAnnotation.role();
        } else if (null != classAnnotation) {
            isNeed = classAnnotation.isNeed();
            roles = classAnnotation.role();
        }

        if (isNeed) {
            // 获取角色
            UserDistributor user = (UserDistributor) request.getSession().getAttribute(GlobalConstant.LOGIN_USER);
            boolean flag = false;
            for (RoleAuth role : roles) {
                if (role.code() == user.getUserType()) {
                    flag = true;
                    break;
                }
            }
            if (!flag)
                throw new BusinessException(ErrorCodeEnum.GL99990107);
        }
        return true;
    }
}
