package com.cvd.chevdian.config;

import com.github.pagehelper.PageInterceptor;
import org.apache.ibatis.plugin.Interceptor;
import org.mybatis.spring.SqlSessionFactoryBean;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import java.util.Properties;

/**
 * pegehelper分页插件 interceptor配置
 */
@Configuration
public class PageHelperConfiguration {

    @Bean
    PageInterceptor pageHelper() {
        PageInterceptor pageInterceptor = new PageInterceptor();
        Properties properties = new Properties();
        properties.setProperty("helperDialect","mysql");
        properties.setProperty("pageSizeZero","true");
        properties.setProperty("reasonable","true");
        pageInterceptor.setProperties(properties);
        new SqlSessionFactoryBean().setPlugins(new Interceptor[]{pageInterceptor});
        return pageInterceptor;
    }

}
