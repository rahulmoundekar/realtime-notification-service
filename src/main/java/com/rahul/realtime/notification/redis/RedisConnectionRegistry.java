package com.rahul.realtime.notification.redis;

public interface RedisConnectionRegistry {

    void register(String userId);

    void unregister(String userId);

    String getInstanceId(String userId);
}