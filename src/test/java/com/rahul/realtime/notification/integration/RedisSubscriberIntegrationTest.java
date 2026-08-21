package com.rahul.realtime.notification.integration;

import com.rahul.realtime.notification.dto.event.NotificationEvent;
import com.rahul.realtime.notification.redis.RedisNotificationSubscriber;
import com.rahul.realtime.notification.service.NotificationDeliveryService;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.data.redis.connection.RedisConnectionFactory;
import org.springframework.data.redis.connection.lettuce.LettuceConnectionFactory;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.data.redis.listener.ChannelTopic;
import org.springframework.data.redis.listener.RedisMessageListenerContainer;
import org.springframework.data.redis.serializer.StringRedisSerializer;
import org.springframework.test.context.TestPropertySource;
import org.springframework.test.context.junit.jupiter.SpringJUnitConfig;
import tools.jackson.databind.ObjectMapper;

import java.time.Duration;
import java.time.Instant;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicReference;

import static org.assertj.core.api.Assertions.assertThat;

@SpringJUnitConfig(
        classes = RedisSubscriberIntegrationTest.TestConfig.class
)
@TestPropertySource(properties = {
        "spring.data.redis.host=localhost",
        "spring.data.redis.port=6379",
        "app.redis.notification-channel=notification-events"
})
class RedisSubscriberIntegrationTest {

    private static final String CHANNEL =
            "notification-events";

    @Autowired
    private RedisTemplate<String, String> redisTemplate;

    @Autowired
    private TestNotificationDeliveryService deliveryService;

    @Test
    void shouldReceiveNotificationFromRedis()
            throws Exception {

        String payload = """
                {
                  "notificationId": 1001,
                  "userId": "user-101",
                  "type": "ORDER_STATUS",
                  "title": "Order Shipped",
                  "message": "Your order has been shipped.",
                  "status": "UNREAD",
                  "createdAt": "2026-08-21T10:00:00Z"
                }
                """;

        Long subscriberCount =
                redisTemplate.convertAndSend(
                        CHANNEL,
                        payload
                );

        assertThat(subscriberCount)
                .isGreaterThanOrEqualTo(1);

        boolean received =
                deliveryService.awaitEvent(
                        Duration.ofSeconds(5)
                );

        assertThat(received)
                .as("Redis subscriber should receive event")
                .isTrue();

        NotificationEvent event =
                deliveryService.getEvent();

        assertThat(event).isNotNull();

        assertThat(event.notificationId())
                .isEqualTo(1001L);

        assertThat(event.userId())
                .isEqualTo("user-101");

        assertThat(event.type())
                .isEqualTo("ORDER_STATUS");

        assertThat(event.title())
                .isEqualTo("Order Shipped");

        assertThat(event.message())
                .isEqualTo("Your order has been shipped.");

        assertThat(event.status())
                .isEqualTo("UNREAD");

        assertThat(event.createdAt())
                .isEqualTo(
                        Instant.parse(
                                "2026-08-21T10:00:00Z"
                        )
                );
    }

    @Configuration
    static class TestConfig {

        @Bean
        RedisConnectionFactory redisConnectionFactory() {

            LettuceConnectionFactory factory =
                    new LettuceConnectionFactory(
                            "localhost",
                            6379
                    );

            factory.afterPropertiesSet();

            return factory;
        }

        @Bean
        RedisTemplate<String, String> redisTemplate(
                RedisConnectionFactory connectionFactory
        ) {

            RedisTemplate<String, String> template =
                    new RedisTemplate<>();

            template.setConnectionFactory(
                    connectionFactory
            );

            StringRedisSerializer serializer =
                    new StringRedisSerializer();

            template.setKeySerializer(serializer);
            template.setValueSerializer(serializer);
            template.setHashKeySerializer(serializer);
            template.setHashValueSerializer(serializer);

            template.afterPropertiesSet();

            return template;
        }

        @Bean
        RedisMessageListenerContainer redisMessageListenerContainer(
                RedisConnectionFactory connectionFactory,
                RedisNotificationSubscriber subscriber
        ) {

            RedisMessageListenerContainer container =
                    new RedisMessageListenerContainer();

            container.setConnectionFactory(
                    connectionFactory
            );

            container.addMessageListener(
                    subscriber,
                    new ChannelTopic(CHANNEL)
            );

            return container;
        }

        @Bean
        ObjectMapper objectMapper() {

            return new ObjectMapper();
        }

        @Bean
        TestNotificationDeliveryService
        notificationDeliveryService() {

            return new TestNotificationDeliveryService();
        }

        @Bean
        RedisNotificationSubscriber
        redisNotificationSubscriber(
                ObjectMapper objectMapper,
                NotificationDeliveryService deliveryService
        ) {

            return new RedisNotificationSubscriber(
                    objectMapper,
                    deliveryService
            );
        }
    }

    static class TestNotificationDeliveryService
            implements NotificationDeliveryService {

        private final CountDownLatch latch =
                new CountDownLatch(1);

        private final AtomicReference<NotificationEvent>
                receivedEvent =
                new AtomicReference<>();

        @Override
        public void deliver(
                NotificationEvent event
        ) {

            receivedEvent.set(event);

            latch.countDown();
        }

        boolean awaitEvent(
                Duration timeout
        ) throws InterruptedException {

            return latch.await(
                    timeout.toMillis(),
                    TimeUnit.MILLISECONDS
            );
        }

        NotificationEvent getEvent() {

            return receivedEvent.get();
        }
    }
}