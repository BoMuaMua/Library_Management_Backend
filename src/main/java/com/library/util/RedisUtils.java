package com.library.util;

import com.library.auth.util.JwtUtils;
import jakarta.annotation.Resource;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.data.redis.core.script.DefaultRedisScript;
import org.springframework.stereotype.Component;

import java.util.Set;
import java.util.concurrent.TimeUnit;

@Component
@Slf4j
public class RedisUtils {

    @Resource
    private RedisTemplate<String, Object> redisTemplate;

    @Autowired
    private JwtUtils jwtUtils;

    @Autowired
    private DefaultRedisScript<Long> deletePrefixScript;


    /**
     * 删除Token
     *
     * @param  token
     */
    public void deleteToken(String token) {
        String blackListKey = "auth:blacklist:" + token;
        long remainingTime = jwtUtils.getRemainingTime("refresh"); // 获取 long token 的剩余过期时间
        if (remainingTime > 0) {
            redisTemplate.opsForValue().set(blackListKey, "1", remainingTime, TimeUnit.SECONDS);
        }
    }

    //保存数据
    public void save(String key, Object value) {
        //默认存储时间30分钟
        int saveTime = 30;
        redisTemplate.opsForValue().set(key, value, saveTime, TimeUnit.MINUTES);
    }
    
    //保存数据（自定义过期时间）
    public void save(String key, Object value, long timeout, TimeUnit unit) {
        redisTemplate.opsForValue().set(key, value, timeout, unit);
    }
    
    //提取数据
    public Object get(String key) {
        return redisTemplate.opsForValue().get(key);
    }
    
    //提取数据（带类型转换）
    @SuppressWarnings("unchecked")
    public <T> T get(String key, Class<T> clazz) {
        Object value = redisTemplate.opsForValue().get(key);
        if (value != null && clazz.isInstance(value)) {
            return (T) value;
        }
        return null;
    }
    
    //删除数据
    public void delete(String key) {
        redisTemplate.delete(key);
    }


    //以一个前缀为依据来批量删除缓存数据
    public void clearCache( String prefix) {

        if (prefix == null || prefix.isEmpty()) return;

        // 1. 获取所有匹配的 Key (这一步 RedisTemplate 会处理分片聚合)
        Set<String> keys = redisTemplate.keys(prefix + "*");

        if (keys != null && !keys.isEmpty()) {
            // 2. 直接利用 RedisTemplate 的 delete 方法
            // 它可以跨分片处理集合删除，比自己写 Lua 更安全、更兼容
            Long deletedCount = redisTemplate.delete(keys);
            System.out.println("成功删除数量: " + deletedCount);
        } else {
            System.out.println("未找到匹配前缀的缓存");
        }
    }

}
