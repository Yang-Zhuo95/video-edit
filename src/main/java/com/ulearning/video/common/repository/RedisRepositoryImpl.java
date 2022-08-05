package com.ulearning.video.common.repository;

import com.ulearning.video.utils.StringUtil;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.dao.DataAccessException;
import org.springframework.data.redis.connection.RedisConnection;
import org.springframework.data.redis.core.RedisCallback;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.data.redis.core.ValueOperations;
import org.springframework.data.redis.serializer.RedisSerializer;
import org.springframework.stereotype.Service;

import javax.annotation.Resource;
import java.util.concurrent.TimeUnit;

@Service
public class RedisRepositoryImpl implements RedisRepository{

	@Value("${token.expiredSeconds}")
	private Integer expiredSeconds;

	@Resource
	private StringRedisTemplate stringRedisTemplate;

	@Override
	public String getKey(String key) {
		return stringRedisTemplate.opsForValue().get(key);
	}

	@Override
	public boolean hasKey(String key) {
		return stringRedisTemplate.hasKey(key);
	}

	@Override
	public String save(String key, String value) {
		return this.save(key, value, expiredSeconds);
	}

	@Override
	public String save(String key, String value, Integer expiredSeconds) {
		if (this.hasKey(key)) {
			this.clear(key);
		}
		ValueOperations<String, String> operations= stringRedisTemplate.opsForValue();
		if(null == expiredSeconds){
			operations.set(key, value);  //有效期永久
		}else{
			operations.set(key, value, expiredSeconds, TimeUnit.SECONDS);
		}
		return key;
	}

	@Override
	public String saveByMilliseconds(String key, String value, Long expiredSeconds) {
		if (this.hasKey(key)) {
			this.clear(key);
		}
		ValueOperations<String, String> operations= stringRedisTemplate.opsForValue();
		operations.set(key, value, expiredSeconds, TimeUnit.MILLISECONDS);
		return key;
	}

	@Override
	public void clear(String key) {
		stringRedisTemplate.delete(key);
	}

	/**
	 * @author Huangjin
	 * @version 2019年4月29日下午6:36:48
	 */
	@Override
	public Long incr(String key) {
		return stringRedisTemplate.execute(new RedisCallback<Long>() {
            @Override
            public Long doInRedis(RedisConnection redisConnection) throws DataAccessException {
            	 RedisSerializer<String> redisSerializer = stringRedisTemplate.getStringSerializer();
                byte keys[] = redisSerializer.serialize(key);
                return redisConnection.incr(keys);
            }
        });
	}
	/**
	 * @author Huangjin
	 * @version 2019年4月29日下午6:57:21
	 */
	@Override
	public Long decr(String key) {
		return stringRedisTemplate.execute(new RedisCallback<Long>() {
            @Override
            public Long doInRedis(RedisConnection redisConnection) throws DataAccessException {
            	 RedisSerializer<String> redisSerializer = stringRedisTemplate.getStringSerializer();
                byte keys[] = redisSerializer.serialize(key);
                return redisConnection.decr(keys);
            }
        });
	}

	/**
	 * 加锁
	 * @param targetId   targetId - 商品的唯一标志
	 * @param timeStamp  当前时间+超时时间 也就是时间戳
	 * @return
	 */
	@Override
	public boolean lock(String targetId, String timeStamp) {
		if(stringRedisTemplate.opsForValue().setIfAbsent(targetId,timeStamp)){
			// 对应setnx命令，可以成功设置,也就是key不存在
			return true;
		}

		// 判断锁超时 - 防止原来的操作异常，没有运行解锁操作  防止死锁
		String currentLock = stringRedisTemplate.opsForValue().get(targetId);
		// 如果锁过期 currentLock不为空且小于当前时间
		if(!StringUtil.isNullOrEmpty(currentLock) && Long.parseLong(currentLock) < System.currentTimeMillis()){
			// 获取上一个锁的时间value 对应getset，如果lock存在
			String preLock = stringRedisTemplate.opsForValue().getAndSet(targetId,timeStamp);

			// 假设两个线程同时进来这里，因为key被占用了，而且锁过期了。获取的值currentLock=A(get取的旧的值肯定是一样的),两个线程的timeStamp都是B,key都是K.锁时间已经过期了。
			// 而这里面的getAndSet一次只会一个执行，也就是一个执行之后，上一个的timeStamp已经变成了B。只有一个线程获取的上一个值会是A，另一个线程拿到的值是B。
			if(!StringUtil.isNullOrEmpty(preLock) && preLock.equals(currentLock) ){
				// preLock不为空且preLock等于currentLock，也就是校验是不是上个对应的商品时间戳，也是防止并发
				return true;
			}
		}
		return false;
	}

	/**
	 * 解锁
	 * @param target
	 * @param timeStamp
	 */
	@Override
	public void unlock(String target, String timeStamp) {
		try {
			String currentValue = stringRedisTemplate.opsForValue().get(target);
			if(!StringUtil.isNullOrEmpty(currentValue) && currentValue.equals(timeStamp) ){
				// 删除锁状态
				stringRedisTemplate.opsForValue().getOperations().delete(target);
			}
		} catch (Exception e) {
		}
	}
	
	/**
	 * 设置过期时间
	 * @author Huangjin
	 * @version 2019年5月5日下午2:55:34
	 */
	@Override
    public Boolean expire(String key, int expireTime) {
        return stringRedisTemplate.execute(new RedisCallback<Boolean>() {
            @Override
            public Boolean doInRedis(RedisConnection redisConnection) throws DataAccessException {
            	RedisSerializer<String> redisSerializer = stringRedisTemplate.getStringSerializer();
                byte keys[] = redisSerializer.serialize(key);
                return redisConnection.expire(keys,(long)expireTime);
            }
        });
    }

	/**
	 * 设置过期时间
	 *
	 * @param key
	 * @param timeout
	 * @param unit
	 * @return
	 */
	@Override
	public Boolean expire(String key, long timeout, TimeUnit unit) {
		return stringRedisTemplate.expire(key, timeout, unit);
	}

	/**
	 * 增加(自增长), 负数则为自减
	 *
	 * @param key
	 * @param value
	 * @return
	 */
	@Override
	public Long incrBy(String key, long increment) {
		return stringRedisTemplate.opsForValue().increment(key, increment);
	}
}
