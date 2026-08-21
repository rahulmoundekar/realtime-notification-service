package com.rahul.realtime.notification.integration;

import com.rahul.realtime.notification.dto.request.CreateNotificationRequest;
import com.rahul.realtime.notification.dto.response.NotificationResponse;
import com.rahul.realtime.notification.enums.NotificationType;
import com.rahul.realtime.notification.repository.NotificationRepository;
import com.rahul.realtime.notification.service.NotificationService;
import com.rahul.realtime.notification.support.IntegrationTestBase;
import com.rahul.realtime.notification.support.TestJwtDecoderConfig;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.context.annotation.Import;

import static org.assertj.core.api.Assertions.assertThat;

@SpringBootTest
@Import(TestJwtDecoderConfig.class)
class NotificationServiceIntegrationTest {

    @Autowired
    NotificationService notificationService;

    @Autowired
    NotificationRepository notificationRepository;

    @Test
    void shouldPersistNotification() {

        CreateNotificationRequest request = new CreateNotificationRequest(NotificationType.ORDER_STATUS, "Order Shipped", "Your order was shipped");

        NotificationResponse response = notificationService.createNotification(request, "user-101");

        assertThat(response.id()).isNotNull();

        assertThat(response.userId()).isEqualTo("user-101");

        assertThat(notificationRepository.findById(response.id())).isPresent();
    }
}