
package com.cvd.chevdian.common.annotation;


import com.cvd.chevdian.common.enums.RoleAuth;

import java.lang.annotation.*;


/**
 * 接口权限
 */
@Target({ElementType.METHOD, ElementType.TYPE})
@Retention(RetentionPolicy.RUNTIME)
@Documented
public @interface ApiAuth {


    /**
     * 是否需要登录判断
     *
     * @return the boolean
     */
    boolean isNeed() default true;

    RoleAuth[] role() default {};

}
