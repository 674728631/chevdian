
package com.cvd.chevdian.common.annotation;


import java.lang.annotation.*;


/**
 * 登录判断
 *
 * @author paascloud.net@gmail.com
 */
@Target({ElementType.METHOD, ElementType.TYPE})
@Retention(RetentionPolicy.RUNTIME)
@Documented
public @interface NeedLogin {


    /**
     * 是否需要登录判断
     *
     * @return the boolean
     */
    boolean isNeed() default true;
}
