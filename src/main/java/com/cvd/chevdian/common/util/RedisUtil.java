package com.cvd.chevdian.common.util;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.redis.core.RedisCallback;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.stereotype.Component;

import java.nio.charset.Charset;
import java.time.Instant;
import java.time.LocalDate;
import java.time.ZoneId;
import java.util.Date;
import java.util.concurrent.TimeUnit;

@Component
public class RedisUtil {

    @Autowired
    private StringRedisTemplate stringRedisTemplate;//redis操作模板

    public int incrementAndExpire(String key) {
        //每次从1开始自增
        int i = stringRedisTemplate.opsForValue().increment(key, 1).intValue();
        //获取第二天的0点
        Instant instant = LocalDate.now().plusDays(1).atStartOfDay(ZoneId.systemDefault()).toInstant();
        Date date = Date.from(instant);
        //如果每次都是每天第一次获取到的自增1，就设置过期时间是第二天零点
        if (i == 1) {
            stringRedisTemplate.expireAt(key, date);
        }
        return i;
    }

    /**
     * @param key 删除key
     */
    public void delect(String key) {
        stringRedisTemplate.delete(key);
    }

    /**
     * 从0自增
     */
    public void hIncr(String key, String field, long add) {
        stringRedisTemplate.execute((RedisCallback<Object>) redisConnection -> {
            Long record = redisConnection.hIncrBy(toBytes(key), toBytes(field), add);
            return record == null ? 0L : record;
        });
    }

    private byte[] toBytes(String key) {
        if (null != key) {
            return key.getBytes(Charset.forName("UTF-8"));
        }
        return null;
    }


    // 添加string的key-value
    public void putToString(String key, String value) {
        putToString(key, value, -1);
    }

    public void putToString(String key, String value, long time) {
        if (time < 1)
            stringRedisTemplate.opsForValue().set(key, value);
        else
            putToString(key, value, time, TimeUnit.SECONDS);
    }

    public void putToString(String key, String value, long time, TimeUnit unit) {
        if (time < 1)
            stringRedisTemplate.opsForValue().set(key, value);
        else
            stringRedisTemplate.opsForValue().set(key, value, time, unit);
    }

    public String getString(String key) {
        return stringRedisTemplate.opsForValue().get(key);
    }


}