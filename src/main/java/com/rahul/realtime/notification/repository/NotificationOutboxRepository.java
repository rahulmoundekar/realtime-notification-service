package com.rahul.realtime.notification.repository;

import com.rahul.realtime.notification.entity.NotificationOutboxEvent;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface NotificationOutboxRepository extends JpaRepository<NotificationOutboxEvent, Long> {

    List<NotificationOutboxEvent> findTop100ByPublishedFalseOrderByCreatedAtAsc();
}