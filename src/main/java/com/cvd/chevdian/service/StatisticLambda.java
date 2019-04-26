package com.cvd.chevdian.service;

import java.util.Map;

@FunctionalInterface
public interface StatisticLambda<T, E> {

    /**
     * 不同角色实现方法，用来
     * 获取自己的一级互助用户 + 互助用户的邀请用户
     */
    Map<String, Object> getOwnerAndOhtersCarList(T t, E e);
}
