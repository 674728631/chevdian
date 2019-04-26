/*
 * Copyright (c) 2018. paascloud.net All Rights Reserved.
 * 项目名称：paascloud快速搭建企业级分布式微服务平台
 * 类名称：JacksonUtil.java
 * 创建人：刘兆明
 * 联系方式：paascloud.net@gmail.com
 * 开源地址: https://github.com/paascloud
 * 博客地址: http://blog.paascloud.net
 * 项目官网: http://paascloud.net
 */

package com.cvd.chevdian.common.util;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.google.common.base.Preconditions;
import lombok.AccessLevel;
import lombok.NoArgsConstructor;

import java.io.IOException;
import java.text.SimpleDateFormat;

/**
 * Jackson Json 工具类
 */
@NoArgsConstructor(access = AccessLevel.PRIVATE)
public class JacksonUtil {

    private static ObjectMapper formatedMapper;

    static {
        // 设置输入时忽略在JSON字符串中存在但Java对象实际没有的属性
        formatedMapper = new ObjectMapper();
        // 所有日期格式都统一为固定格式
        formatedMapper.setDateFormat(new SimpleDateFormat("yyyy-MM-dd HH:mm:ss"));
    }

    /**
     * 将对象转化为json数据(时间转换格式： "yyyy-MM-dd HH:mm:ss")
     */
    public static String toJsonWithFormat(Object obj) throws IOException {
        Preconditions.checkArgument(obj != null, "this argument is required; it must not be null");
        return formatedMapper.writeValueAsString(obj);
    }

}
