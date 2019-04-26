package com.cvd.chevdian.config;

import com.cvd.chevdian.interceptor.ApiAuthInterceptor;
import com.cvd.chevdian.interceptor.LoginInterceptor;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.servlet.config.annotation.CorsRegistry;
import org.springframework.web.servlet.config.annotation.InterceptorRegistry;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurer;

/**
 * 添加拦截器
 */
@Configuration
public class WebMvcConfiguration implements WebMvcConfigurer {

    @Override
    public void addInterceptors(InterceptorRegistry registry) {
        // 拦截所有请求，通过判断是否有 @NeedLogin 注解 决定是否需要登录
        registry.addInterceptor(loginInterceptor()).addPathPatterns("/**").excludePathPatterns("/error","/swagger-*/**","/webjars**/**","/","/csrf","/wx","/css/**"
                ,"/img/**","/js/**","/lib/**","/view/*.html");
        registry.addInterceptor(apiAuthInterceptor()).addPathPatterns("/**").excludePathPatterns("/error","/swagger-*/**","/webjars**/**","/","/csrf","/wx","/css/**"
                ,"/img/**","/js/**","/lib/**","/view/*.html");
    }

    @Bean
    public LoginInterceptor loginInterceptor() {
        return new LoginInterceptor();
    }
    @Bean
    public ApiAuthInterceptor apiAuthInterceptor() {
        return new ApiAuthInterceptor();
    }

    @Override
    public void addCorsMappings(CorsRegistry registry) {
        registry.addMapping("/**").allowedOrigins("*").allowedMethods("POST","GET","DELETE");
    }
}
