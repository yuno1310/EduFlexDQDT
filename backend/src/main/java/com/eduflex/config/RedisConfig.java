package com.eduflex.config;

import org.springframework.cache.CacheManager;
import org.springframework.cache.annotation.EnableCaching;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.data.redis.cache.RedisCacheConfiguration;
import org.springframework.data.redis.cache.RedisCacheManager;
import org.springframework.data.redis.connection.RedisConnectionFactory;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.data.redis.serializer.GenericJackson2JsonRedisSerializer;
import org.springframework.data.redis.serializer.RedisSerializationContext;
import org.springframework.data.redis.serializer.StringRedisSerializer;

import java.time.Duration;
import java.util.HashMap;
import java.util.Map;

@Configuration
@EnableCaching
public class RedisConfig {

  @Bean
  public RedisTemplate<String, Object> redisTemplate(RedisConnectionFactory connectionFactory) {
    RedisTemplate<String, Object> template = new RedisTemplate<>();
    template.setConnectionFactory(connectionFactory);
    template.setKeySerializer(new StringRedisSerializer());
    template.setValueSerializer(new GenericJackson2JsonRedisSerializer());
    template.setHashKeySerializer(new StringRedisSerializer());
    template.setHashValueSerializer(new GenericJackson2JsonRedisSerializer());
    template.afterPropertiesSet();
    return template;
  }

  @Bean
  public CacheManager cacheManager(RedisConnectionFactory connectionFactory) {
    // Default cache config: 5 minutes TTL
    RedisCacheConfiguration defaultConfig = RedisCacheConfiguration.defaultCacheConfig()
        .entryTtl(Duration.ofMinutes(5))
        .serializeKeysWith(RedisSerializationContext.SerializationPair
            .fromSerializer(new StringRedisSerializer()))
        .serializeValuesWith(RedisSerializationContext.SerializationPair
            .fromSerializer(new GenericJackson2JsonRedisSerializer()))
        .disableCachingNullValues();

    // Per-cache TTL overrides
    Map<String, RedisCacheConfiguration> cacheConfigs = new HashMap<>();
    cacheConfigs.put("courses", defaultConfig.entryTtl(Duration.ofMinutes(5)));
    cacheConfigs.put("lessons", defaultConfig.entryTtl(Duration.ofMinutes(5)));
    cacheConfigs.put("quizzes", defaultConfig.entryTtl(Duration.ofMinutes(10)));
    cacheConfigs.put("leaderboard", defaultConfig.entryTtl(Duration.ofMinutes(1)));
    cacheConfigs.put("userStats", defaultConfig.entryTtl(Duration.ofMinutes(2)));
    cacheConfigs.put("badges", defaultConfig.entryTtl(Duration.ofMinutes(30)));
    cacheConfigs.put("userBadges", defaultConfig.entryTtl(Duration.ofMinutes(5)));
    cacheConfigs.put("courseReviews", defaultConfig.entryTtl(Duration.ofMinutes(3)));

    return RedisCacheManager.builder(connectionFactory)
        .cacheDefaults(defaultConfig)
        .withInitialCacheConfigurations(cacheConfigs)
        .transactionAware()
        .build();
  }
}
