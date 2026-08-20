package com.rahul.realtime.notification.redis;

import java.util.Set;

public interface RedisConnectionRegistry {

    void register(String userId);

    void unregister(String userId);

    Set<String> getInstances(String userId);
}