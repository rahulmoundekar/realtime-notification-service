package com.rahul.realtime.notification.integration;

import com.rahul.realtime.notification.support.IntegrationTestBase;
import com.rahul.realtime.notification.support.TestJwtDecoderConfig;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Import;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.boot.test.context.SpringBootTest;

import java.time.Duration;

import static org.assertj.core.api.Assertions.assertThat;

@SpringBootTest
@Import(TestJwtDecoderConfig.class)
class RedisPubSubIntegrationTest {

    @Autowired
    private RedisTemplate<String, String>
            redisTemplate;

    @Test
    void shouldPublishToRedisChannel() {

        Long subscribers =
                redisTemplate.convertAndSend(
                        "notification-events",
                        "{\"test\":true}"
                );

        assertThat(subscribers)
                .isGreaterThanOrEqualTo(0);
    }
}