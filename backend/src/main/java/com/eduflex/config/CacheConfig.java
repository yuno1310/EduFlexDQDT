package com.eduflex.config;

import java.time.Duration;

import org.springframework.cache.CacheManager;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.data.redis.cache.RedisCacheConfiguration;
import org.springframework.data.redis.cache.RedisCacheManager;
import org.springframework.data.redis.connection.RedisConnectionFactory;
import org.springframework.data.redis.serializer.GenericJackson2JsonRedisSerializer;
import org.springframework.data.redis.serializer.RedisSerializationContext;

@Configuration
public class CacheConfig {

  @Bean
  CacheManager cacheManager(RedisConnectionFactory connectionFactory) {
    var json = new GenericJackson2JsonRedisSerializer();
    var defaults = RedisCacheConfiguration.defaultCacheConfig()
        .disableCachingNullValues()
        .entryTtl(Duration.ofMinutes(10))
        .serializeValuesWith(RedisSerializationContext.SerializationPair.fromSerializer(json));

    return RedisCacheManager.builder(connectionFactory)
        .cacheDefaults(defaults)
        .withCacheConfiguration("courseCatalog", defaults.entryTtl(Duration.ofMinutes(5)))
        .withCacheConfiguration("courseSummaries", defaults.entryTtl(Duration.ofHours(6)))
        .withCacheConfiguration("semanticSearch", defaults.entryTtl(Duration.ofMinutes(15)))
        .transactionAware()
        .build();
  }
}
