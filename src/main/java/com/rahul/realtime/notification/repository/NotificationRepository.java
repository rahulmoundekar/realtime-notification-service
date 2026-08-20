package com.rahul.realtime.notification.repository;

import com.rahul.realtime.notification.entity.Notification;
import com.rahul.realtime.notification.enums.NotificationStatus;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface NotificationRepository extends JpaRepository<Notification, Long> {

    List<Notification> findByUserIdOrderByCreatedAtDesc(String userId);

    List<Notification> findByUserIdAndStatusOrderByCreatedAtDesc(
            String userId,
            NotificationStatus status
    );

    Page<Notification> findByUserIdOrderByCreatedAtDesc(
            String userId,
            Pageable pageable
    );

    Page<Notification> findByUserIdAndStatusOrderByCreatedAtDesc(
            String userId,
            NotificationStatus status,
            Pageable pageable
    );

}
