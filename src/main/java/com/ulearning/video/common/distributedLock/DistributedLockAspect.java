package com.ulearning.video.common.distributedLock;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.aspectj.lang.ProceedingJoinPoint;
import org.aspectj.lang.Signature;
import org.aspectj.lang.annotation.Around;
import org.aspectj.lang.annotation.Aspect;
import org.aspectj.lang.annotation.Pointcut;
import org.redisson.api.RLock;
import org.redisson.api.RedissonClient;
import org.springframework.stereotype.Component;

import javax.annotation.Resource;
import java.util.concurrent.TimeUnit;

/**
 * 分布式锁aop，在定时任务方法上加上DistributedLockAnnotation注解则多个服务器只有一个服务器会执行此定时任务
 * @author chenbin
 * @date 2021年1月11日
 */
@Aspect
@Component
public class DistributedLockAspect {

    private final Log logger = LogFactory.getLog(DistributedLockAspect.class);

    @Resource
    RedissonClient redisson;

    @Pointcut("@annotation(com.ulearning.video.common.distributedLock.DistributedLockAnnotation)")
    public void doLock(){}

    @Around("doLock()")
    public void around(ProceedingJoinPoint proceedingJoinPoint) throws Throwable{
        String os = System.getProperty("os.name");
        if (os.toLowerCase().startsWith("win")) {
            // 本地不执行
            logger.info("windows 环境不执行 获取定时任务分布式锁");
            return;
        }

        Object[] args = proceedingJoinPoint.getArgs();
        Signature signature = proceedingJoinPoint.getSignature();
        String lockName = "paperlibrary_lock." + signature.getDeclaringTypeName() + "." + signature.getName();
        RLock lock = redisson.getLock(lockName);
        try {
            //锁有效期1分钟，不让别的进程获取锁，除非其他服务器的时钟和第一个获取锁的服务器相差超过1分钟才能获取到
            boolean success = lock.tryLock(0,60, TimeUnit.SECONDS);
            if(success){
                logger.info(lockName + ": occupying lock");
                Object val = proceedingJoinPoint.proceed(args);
            }else{
                logger.info(lockName + ": lock occupied, pass");
            }
        }catch (Exception e){
            e.printStackTrace();
        }finally{
            //锁自动过期，不需要手动解锁
//            if(lock.isLocked()){
//                lock.unlock();
//            }
        }
    }

}
