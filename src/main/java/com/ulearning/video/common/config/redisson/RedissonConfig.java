package com.ulearning.video.common.config.redisson;

import org.redisson.Redisson;
import org.redisson.api.RedissonClient;
import org.redisson.config.Config;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import java.io.IOException;

@Configuration
public class RedissonConfig {

    @Value("${spring.redis.host}")
    private String host;
    @Value("${spring.redis.port}")
    private String port;

    @Bean(destroyMethod="shutdown")
    public RedissonClient redisson() throws IOException {
        //1 创建配置
        Config config = new Config();
        config.useSingleServer().setAddress("redis://" + host + ":" + port);
        //2.根据Config创建出RedissonClient
        return Redisson.create(config);
    }
}
