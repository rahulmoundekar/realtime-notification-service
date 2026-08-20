package com.rahul.realtime.notification.redis;

import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class RedisConnectionRegistryImpl implements RedisConnectionRegistry {

    private final RedisTemplate<String, String> redisTemplate;

    @Value("${app.instance.id}")
    private String instanceId;

    private static final String KEY_PREFIX = "notification:connection:";

    @Override
    public void register(String userId) {

        redisTemplate.opsForValue().set(KEY_PREFIX + userId, instanceId);
    }

    @Override
    public void unregister(String userId) {

        redisTemplate.delete(KEY_PREFIX + userId);
    }

    @Override
    public String getInstanceId(String userId) {

        return redisTemplate.opsForValue().get(KEY_PREFIX + userId);
    }
}