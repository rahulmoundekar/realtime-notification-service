package com.rahul.realtime.notification.integration;

import com.rahul.realtime.notification.dto.event.NotificationEvent;
import com.rahul.realtime.notification.entity.NotificationOutboxEvent;
import com.rahul.realtime.notification.repository.NotificationOutboxRepository;
import com.rahul.realtime.notification.service.NotificationPublisher;
import com.rahul.realtime.notification.service.impl.NotificationOutboxPublisher;
import com.rahul.realtime.notification.support.TestJwtDecoderConfig;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.context.TestConfiguration;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Import;
import org.springframework.context.annotation.Primary;

import static org.assertj.core.api.Assertions.assertThat;

@SpringBootTest(properties = {"spring.datasource.url=jdbc:postgresql://localhost:5432/realtime_notification", "spring.datasource.username=postgres", "spring.datasource.password=root", "spring.datasource.driver-class-name=org.postgresql.Driver",

        "spring.data.redis.host=localhost", "spring.data.redis.port=6379",

        "app.redis.notification-channel=notification-events", "app.instance.id=outbox-test",

        "spring.jpa.hibernate.ddl-auto=update"})
@Import({NotificationOutboxRecoveryIntegrationTest.TestConfig.class, TestJwtDecoderConfig.class})
class NotificationOutboxRecoveryIntegrationTest {

    @Autowired
    private NotificationOutboxRepository outboxRepository;

    @Autowired
    private NotificationOutboxPublisher outboxPublisher;

    @Autowired
    private TestNotificationPublisher testPublisher;

    @BeforeEach
    void setUp() {
        outboxRepository.deleteAll();
        testPublisher.reset();
    }

    @Test
    void shouldKeepOutboxUnpublishedWhenPublishingFails() {

        NotificationOutboxEvent event = createOutboxEvent(2001L);

        NotificationOutboxEvent saved = outboxRepository.saveAndFlush(event);

        testPublisher.setFailPublishing(true);

        outboxPublisher.publishPendingEvents();

        NotificationOutboxEvent result = outboxRepository.findById(saved.getId()).orElseThrow();

        assertThat(result.isPublished()).isFalse();

        assertThat(testPublisher.getAttempts()).isEqualTo(1);
    }

    @Test
    void shouldPublishPendingEventAfterRecovery() {

        NotificationOutboxEvent event = createOutboxEvent(2002L);

        NotificationOutboxEvent saved = outboxRepository.saveAndFlush(event);

        // First attempt fails
        testPublisher.setFailPublishing(true);

        outboxPublisher.publishPendingEvents();

        NotificationOutboxEvent afterFailure = outboxRepository.findById(saved.getId()).orElseThrow();

        assertThat(afterFailure.isPublished()).isFalse();

        // Publisher recovers
        testPublisher.setFailPublishing(false);

        outboxPublisher.publishPendingEvents();

        NotificationOutboxEvent afterRecovery = outboxRepository.findById(saved.getId()).orElseThrow();

        assertThat(afterRecovery.isPublished()).isTrue();

        assertThat(afterRecovery.getPublishedAt()).isNotNull();

        assertThat(testPublisher.getAttempts()).isEqualTo(2);
    }

    private NotificationOutboxEvent createOutboxEvent(Long notificationId) {

        NotificationOutboxEvent event = new NotificationOutboxEvent();

        event.setNotificationId(notificationId);

        event.setPayload("""
                {
                  "notificationId": %d,
                  "userId": "user-101",
                  "type": "ORDER_STATUS",
                  "title": "Order Shipped",
                  "message": "Your order has been shipped.",
                  "status": "UNREAD"
                }
                """.formatted(notificationId));

        return event;
    }

    @TestConfiguration(proxyBeanMethods = false)
    static class TestConfig {

        @Bean
        @Primary
        TestNotificationPublisher testNotificationPublisher() {
            return new TestNotificationPublisher();
        }

        @Bean
        NotificationOutboxPublisher notificationOutboxPublisher(NotificationOutboxRepository outboxRepository, NotificationPublisher notificationPublisher) {

            return new NotificationOutboxPublisher(outboxRepository, notificationPublisher);
        }
    }

    static class TestNotificationPublisher implements NotificationPublisher {

        private boolean failPublishing;
        private int attempts;

        @Override
        public void publishPayload(String payload) {

            attempts++;

            if (failPublishing) {
                throw new RuntimeException("Simulated Redis outage");
            }
        }

        @Override
        public void publish(NotificationEvent event) {
            throw new UnsupportedOperationException("Not used by outbox recovery test");
        }

        void setFailPublishing(boolean failPublishing) {
            this.failPublishing = failPublishing;
        }

        int getAttempts() {
            return attempts;
        }

        void reset() {
            failPublishing = false;
            attempts = 0;
        }
    }
}