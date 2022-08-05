package com.ulearning.video.common.distributedLock;

import java.lang.annotation.Documented;
import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

/**
 * 分布式锁注解，在定时任务方法上加上此注解则多个服务器只有一个服务器会执行此定时任务
 * @author chenbin
 * @date 2021年1月11日
 */
@Documented
@Retention(RetentionPolicy.RUNTIME)
@Target(ElementType.METHOD)
public @interface DistributedLockAnnotation {

}
