package org.seckill.service;


import org.seckill.entity.Seckill;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.data.redis.core.ValueOperations;
import org.springframework.data.redis.serializer.Jackson2JsonRedisSerializer;
import org.springframework.data.redis.serializer.StringRedisSerializer;
import org.springframework.stereotype.Service;

import javax.annotation.Resource;
import java.util.concurrent.TimeUnit;

@Service
public class RedisService {
	@Resource
	// redis的key值只能是string类型
	// redis默认不提供系列化对象的功能
	private RedisTemplate<String, Object> redisTemplate;

	public void setSeckill(Long key, Seckill value) {
		// 解决redis内部查看key和value出现的乱码问题
		// redis的key值只能是string类型
		redisTemplate.setKeySerializer(new StringRedisSerializer());
		redisTemplate.setValueSerializer(new Jackson2JsonRedisSerializer<Seckill>(Seckill.class));
		ValueOperations<String,Object> vo = redisTemplate.opsForValue();
		vo.set(String.valueOf(key), value);
	}
	
	public void setSeckill(Long key, Seckill value, long time, TimeUnit t) {
		// 解决redis内部查看key和value出现的乱码问题
		// redis的key值只能是string类型
		redisTemplate.setKeySerializer(new StringRedisSerializer());
		// 使用Jackson2JsonRedisSerializer需要明确指明待序列化对象的类名
		redisTemplate.setValueSerializer(new Jackson2JsonRedisSerializer<Seckill>(Seckill.class));
		ValueOperations<String,Object> vo = redisTemplate.opsForValue();
		vo.set(String.valueOf(key), value, time, t);
	}
	
	public Seckill getSeckill(Long key) {
		ValueOperations<String, Object> vo = redisTemplate.opsForValue();
		return (Seckill) vo.get(String.valueOf(key));
	}
	
}
