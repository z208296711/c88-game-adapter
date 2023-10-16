package com.c88.game.adapter.utils;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.data.redis.core.script.DefaultRedisScript;
import org.springframework.data.redis.core.script.RedisScript;
import org.springframework.stereotype.Component;

import java.util.Collections;
import java.util.Objects;
import java.util.concurrent.TimeUnit;

@Component
public  class RedisUtils {

    private static RedisTemplate<String,Object> redisTemplate;
    @Autowired
    public void setRedisTemplate(RedisTemplate<String, Object> redisTemplate) {
        RedisUtils.redisTemplate = redisTemplate;
    }

    /**
     * @Author Terry
     * @Date 2023/3/25 8:33 PM
     * @Description lock
     */
    public static boolean lock(String key, String value, long time) {
//        ApplicationContext applicationContext = SpringUtils.getApplicationContext();
        return Boolean.TRUE.equals(redisTemplate.opsForValue().setIfAbsent("lockKey:" + key, value, time, TimeUnit.SECONDS));
    }

    /**
     * @Author Terry
     * @Date 2023/3/25 8:33 PM
     * @Description unlock
     */

    public static boolean unlock(String key, String value) {
        String script = "if redis.call('get', KEYS[1]) == ARGV[1] then return redis.call('del', KEYS[1]) else return 0 end";
        RedisScript<Long> redisScript = new DefaultRedisScript<>(script, Long.class);
        Long result = redisTemplate.execute(redisScript, Collections.singletonList("lockKey:" + key), value);
        return Objects.equals(1L, result);
    }

}
