package com.cvd.chevdian.config;


import io.swagger.annotations.ApiOperation;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.bind.annotation.RequestMethod;
import springfox.documentation.builders.ApiInfoBuilder;
import springfox.documentation.builders.PathSelectors;
import springfox.documentation.builders.RequestHandlerSelectors;
import springfox.documentation.builders.ResponseMessageBuilder;
import springfox.documentation.service.ApiInfo;
import springfox.documentation.service.ResponseMessage;
import springfox.documentation.spi.DocumentationType;
import springfox.documentation.spring.web.plugins.Docket;
import springfox.documentation.swagger2.annotations.EnableSwagger2;

import java.util.ArrayList;
import java.util.List;

/**
 * swagger 配置类
 */

@Configuration
@EnableSwagger2
public class Swagger2Configuration {

    //是否开启swagger，正式环境一般是需要关闭的，可根据springboot的多环境配置进行设置
    @Value(value = "${swagger.enabled}")
    Boolean swaggerEnabled;

    @Bean
    public Docket createRestApi() {
        return new Docket(DocumentationType.SWAGGER_2)
                .useDefaultResponseMessages(false)
                .globalResponseMessage(RequestMethod.GET, responseMessageList())
                .globalResponseMessage(RequestMethod.POST, responseMessageList())
                // 是否开启
                .apiInfo(apiInfo())
                .enable(swaggerEnabled).select()
                // 扫描的路径包
//                .apis(RequestHandlerSelectors.basePackage("com.cvd.chevdian.controller"))
                .apis(RequestHandlerSelectors.withMethodAnnotation(ApiOperation.class))
                // 指定路径处理PathSelectors.any()代表所有的路径
                .paths(PathSelectors.any()).build().pathMapping("/");
    }

    private ApiInfo apiInfo() {
        return new ApiInfoBuilder()
                .title("chevdian")
                .description("chevdian")
                .version("1.0.0")
                .build();
    }

    private List<ResponseMessage> responseMessageList() {
        List<ResponseMessage> responseMessageList = new ArrayList<>();
        responseMessageList.add(new ResponseMessageBuilder().code(500).message("系统异常").build());
//        responseMessageList.add(new ResponseMessageBuilder().code(100).message("操作成功").responseModel(new ModelRef("Wrapper")).build());
//        responseMessageList.add(new ResponseMessageBuilder().code(200).message("操作成功").responseModel(new ModelRef("Wrapper")).build());
//        responseMessageList.add(new ResponseMessageBuilder().code(9999).message("自定义异常").build());
        return responseMessageList;
    }
}
