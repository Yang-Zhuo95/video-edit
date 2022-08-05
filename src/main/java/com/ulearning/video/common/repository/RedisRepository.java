package com.ulearning.video.common.repository;

import java.util.concurrent.TimeUnit;

public interface RedisRepository {

	String getKey(String key);

	boolean hasKey(String key);

	String save(String key, String value);

	String save(String key, String value, Integer expiredSeconds);

	String saveByMilliseconds(String key, String value, Long expiredSeconds);

	void clear(String key);
	
	Long incr(String key);
	
	Long decr(String key);

	/**
	 * 加锁
	 * @param targetId   targetId - 商品的唯一标志
	 * @param timeStamp  当前时间+超时时间 也就是时间戳
	 * @return
	 */
	boolean lock(String targetId, String timeStamp);

	/**
	 * 解锁
	 * @param target
	 * @param timeStamp
	 */
	void unlock(String target, String timeStamp);

	Boolean expire(String key, int expireTime);

	Boolean expire(String key, long timeout, TimeUnit unit);

	public Long incrBy(String key, long increment);

}
