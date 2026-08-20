package com.rahul.realtime.notification.redis;

import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.stereotype.Service;

import java.util.Collections;
import java.util.Set;

@Service
@RequiredArgsConstructor
public class RedisConnectionRegistryImpl implements RedisConnectionRegistry {

    private static final String KEY_PREFIX = "notification:connections:";

    private final RedisTemplate<String, String> redisTemplate;

    @Value("${app.instance.id}")
    private String instanceId;

    @Override
    public void register(String userId) {

        redisTemplate.opsForSet().add(KEY_PREFIX + userId, instanceId);
    }

    @Override
    public void unregister(String userId) {

        redisTemplate.opsForSet().remove(KEY_PREFIX + userId, instanceId);

        Long size = redisTemplate.opsForSet().size(KEY_PREFIX + userId);

        if (size != null && size == 0) {
            redisTemplate.delete(KEY_PREFIX + userId);
        }
    }

    @Override
    public Set<String> getInstances(String userId) {

        Set<String> instances = redisTemplate.opsForSet().members(KEY_PREFIX + userId);

        return instances != null ? instances : Collections.emptySet();
    }
}