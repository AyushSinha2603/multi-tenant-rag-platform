package com.rag.infra.cache;

import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.stereotype.Service;

import java.time.Duration;
import java.util.Optional;

@Service
public class RedisSemanticCacheService {

    private final RedisTemplate<String, String> redisTemplate;
    private static final Duration CACHE_TTL = Duration.ofHours(24);

    public RedisSemanticCacheService(RedisTemplate<String, String> redisTemplate) {
        this.redisTemplate = redisTemplate;
    }

    public void cacheResponse(String hashedQuery, String llmResponse) {
        redisTemplate.opsForValue().set(hashedQuery, llmResponse, CACHE_TTL);
    }

    public Optional<String> getCachedResponse(String hashedQuery) {
        String cachedResponse = redisTemplate.opsForValue().get(hashedQuery);
        return Optional.ofNullable(cachedResponse);
    }

    public void evictCache(String hashedQuery) {
        redisTemplate.delete(hashedQuery);
    }
}