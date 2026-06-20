package com.library.config;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.core.io.ClassPathResource;
import org.springframework.data.redis.core.script.DefaultRedisScript;

@Configuration
public class RedisConfig {

    /**
     * 配置模糊删除（根据前缀删除）的 Lua 脚本 Bean
     * Bean 的名字默认就是方法名 deletePrefixScript，正好对应你 RedisUtils 里的变量名
     */
    @Bean
    public DefaultRedisScript<Long> deletePrefixScript() {
        DefaultRedisScript<Long> redisScript = new DefaultRedisScript<>();

        // 1. 指定脚本在 resources 目录下的具体位置
        redisScript.setLocation(new ClassPathResource("lua/deletePrefix.lua"));

        // 2. 指定 Lua 脚本执行完后的返回值类型（对应 Lua 脚本中的 return 0 或 del 的返回值）
        redisScript.setResultType(Long.class);

        return redisScript;
    }
}