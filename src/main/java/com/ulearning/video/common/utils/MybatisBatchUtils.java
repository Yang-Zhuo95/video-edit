package com.ulearning.video.common.utils;

import lombok.extern.slf4j.Slf4j;
import org.apache.ibatis.session.ExecutorType;
import org.apache.ibatis.session.SqlSession;
import org.apache.ibatis.session.SqlSessionFactory;
import org.springframework.stereotype.Component;
import org.springframework.transaction.support.TransactionSynchronizationManager;

import javax.annotation.PostConstruct;
import javax.annotation.Resource;
import java.util.Collection;
import java.util.function.BiFunction;

/**
 * @author Mr.run
 * @description mybatis批处理工具
 * @date 2022-09-23 14:17
 */
@Slf4j
@Component
public class MybatisBatchUtils {

    /**
     * 每次处理1000条
     */
    private static final int BATCH_SIZE = 1000;

    private static SqlSessionFactory factory;

    @Resource
    private SqlSessionFactory sqlSessionFactory;

    @PostConstruct
    private void init() {
        factory = sqlSessionFactory;
    }

    /**
     * 批量处理修改或者插入
     * MybatisBatchUtils.batchUpdateOrInsert(arr, Mapper.class, (d, m) -> m.insert(d));
     * @param data        需要被处理的数据
     * @param mapperClass Mybatis的Mapper类
     * @param function    自定义处理逻辑
     * @return int  影响的总行数
     */
    public static <T, U, R> int batchUpdateOrInsert(Collection<T> data, Class<U> mapperClass, BiFunction<T, U, R> function) {
        int i = 1;
        SqlSession batchSqlSession = factory.openSession(ExecutorType.BATCH);
        try {
            U mapper = batchSqlSession.getMapper(mapperClass);
            int size = data.size();
            for (T element : data) {
                function.apply(element, mapper);
                if ((i % BATCH_SIZE == 0) || i == size) {
                    batchSqlSession.flushStatements();
                }
                i++;
            }
            //  非事务环境下强制commit，事务情况下该commit相当于无效
            batchSqlSession.commit(!TransactionSynchronizationManager.isSynchronizationActive());
        } catch (Exception e) {
            batchSqlSession.rollback();
            throw e;
        } finally {
            batchSqlSession.close();
        }
        return i - 1;
    }
}