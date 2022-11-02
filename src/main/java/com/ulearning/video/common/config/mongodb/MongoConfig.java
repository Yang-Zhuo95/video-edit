package com.ulearning.video.common.config.mongodb;

import org.springframework.context.annotation.Bean;
import org.springframework.data.mongodb.MongoDatabaseFactory;
import org.springframework.data.mongodb.MongoTransactionManager;

/**
 * @author yangzhuo
 * @description mongo 配置类
 * @date 2022-09-28 10:16
 */
public class MongoConfig {

    // 配置事务
    @Bean
    MongoTransactionManager transactionManager(MongoDatabaseFactory factory){
        return new MongoTransactionManager(factory);
    }

}
