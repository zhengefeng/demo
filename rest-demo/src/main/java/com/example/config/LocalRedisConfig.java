package com.example.config;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.ComponentScan;
import org.springframework.context.annotation.Configuration;
import org.springframework.core.env.Environment;
import org.springframework.data.redis.cache.RedisCacheManager;
import org.springframework.data.redis.connection.RedisConnectionFactory;
import org.springframework.data.redis.connection.jedis.JedisConnectionFactory;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.data.redis.listener.ChannelTopic;
import org.springframework.data.redis.listener.RedisMessageListenerContainer;
import org.springframework.data.redis.listener.adapter.MessageListenerAdapter;
import org.springframework.data.redis.serializer.JdkSerializationRedisSerializer;
import org.springframework.data.redis.serializer.StringRedisSerializer;

import redis.clients.jedis.JedisPoolConfig;

import com.example.service.RedisListenerService;

/**
 * @author bloodkilory
 *         generate on 15/6/4
 */
@Configuration
@ComponentScan(basePackages = "com.example.dao")
public class LocalRedisConfig {

	@Value("${jedis.hostname}")
	private String hostname;

	@Value("${jedis.port}")
	private Integer port;

	@Value("${redis.topic.tv}")
	private String subChannelTv;

	@Autowired
	Environment env;

	/**
	 * 注入用来订阅的类
	 */
	@Autowired
	private RedisListenerService redisListenerService;

	@Bean
	public RedisConnectionFactory jedisConnectionFactory() {
		JedisPoolConfig poolConfig = new JedisPoolConfig();
		poolConfig.setMaxTotal(10);
		poolConfig.setMaxIdle(5);
		poolConfig.setMinIdle(1);
		poolConfig.setTestOnBorrow(true);
		poolConfig.setTestOnReturn(true);
		poolConfig.setTestWhileIdle(true);
		JedisConnectionFactory jedisConnectionFactory = new JedisConnectionFactory(poolConfig);
		jedisConnectionFactory.setHostName(env.getProperty("jedis.hostname"));
		jedisConnectionFactory.setPort(Integer.parseInt(env.getProperty("jedis.port")));
		return jedisConnectionFactory;
	}

	@Bean(name = "redisTemplate")
	public StringRedisTemplate stringRedisTemplate() {
		return new StringRedisTemplate(jedisConnectionFactory());
	}

	@SuppressWarnings("unchecked")
	@Bean(name = "objectRedisTemplate")
	public RedisTemplate redisTemplate() {
		RedisTemplate redisTemplate = new RedisTemplate();
		redisTemplate.setConnectionFactory(jedisConnectionFactory());
		redisTemplate.setKeySerializer(new StringRedisSerializer());
		redisTemplate.setHashKeySerializer(new StringRedisSerializer());
		redisTemplate.setValueSerializer(new JdkSerializationRedisSerializer());
		redisTemplate.setHashValueSerializer(new JdkSerializationRedisSerializer());
		redisTemplate.setEnableTransactionSupport(true);
		return redisTemplate;
	}

	/**
	 * Support for Spring Cache Abstraction
	 *
	 * @return
	 */
	@Bean
	public RedisCacheManager redisCacheManager() {
		return new RedisCacheManager(redisTemplate());
	}

	@Bean
	public MessageListenerAdapter messageListenerAdapter() {
		return new MessageListenerAdapter(redisListenerService);
	}

	@Bean
	public RedisMessageListenerContainer messageListenerContainer() {
		RedisMessageListenerContainer container = new RedisMessageListenerContainer();
		container.setConnectionFactory(jedisConnectionFactory());
//		Map<MessageListener, Collection<Topic>> listenerMap = new HashMap<>();
//		listenerMap.put(messageListenerAdapter(), Arrays.asList(new ChannelTopic("tv"), new ChannelTopic("tv2")));
		container.addMessageListener(messageListenerAdapter(), new ChannelTopic(subChannelTv));
		return container;
	}
}
