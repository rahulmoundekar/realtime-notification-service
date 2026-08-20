package com.rahul.realtime.notification.service.impl;

import com.rahul.realtime.notification.dto.event.NotificationEvent;
import com.rahul.realtime.notification.dto.request.CreateNotificationRequest;
import com.rahul.realtime.notification.dto.response.NotificationPageResponse;
import com.rahul.realtime.notification.dto.response.NotificationResponse;
import com.rahul.realtime.notification.entity.Notification;
import com.rahul.realtime.notification.enums.NotificationStatus;
import com.rahul.realtime.notification.exception.NotificationNotFoundException;
import com.rahul.realtime.notification.repository.NotificationRepository;
import com.rahul.realtime.notification.service.NotificationPublisher;
import com.rahul.realtime.notification.service.NotificationService;

import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.Instant;
import java.util.List;

@Service
@RequiredArgsConstructor
public class NotificationServiceImpl implements NotificationService {

    private final NotificationRepository notificationRepository;
    private final NotificationPublisher notificationPublisher;

    @Override
    @Transactional
    public NotificationResponse createNotification(
            CreateNotificationRequest request
    ) {

        Notification notification = new Notification();

        notification.setUserId(request.userId());
        notification.setType(request.type());
        notification.setTitle(request.title());
        notification.setMessage(request.message());
        notification.setStatus(NotificationStatus.UNREAD);

        Notification savedNotification =
                notificationRepository.save(notification);

        NotificationResponse response =
                NotificationResponse.from(savedNotification);

        NotificationEvent event =
                NotificationEvent.from(response);

        System.out.println("===== BEFORE REDIS PUBLISH =====");
        System.out.println("Event = " + event);

        notificationPublisher.publish(event);

        System.out.println("===== AFTER REDIS PUBLISH =====");

        return response;
    }

    @Override
    @Transactional(readOnly = true)
    public NotificationPageResponse getUserNotifications(
            String userId,
            int page,
            int size
    ) {

        Pageable pageable = PageRequest.of(
                page,
                size,
                Sort.by(Sort.Direction.DESC, "createdAt")
        );

        Page<Notification> notificationPage =
                notificationRepository.findByUserIdOrderByCreatedAtDesc(
                        userId,
                        pageable
                );

        List<NotificationResponse> content =
                notificationPage.getContent()
                        .stream()
                        .map(NotificationResponse::from)
                        .toList();

        return new NotificationPageResponse(
                content,
                notificationPage.getNumber(),
                notificationPage.getSize(),
                notificationPage.getTotalElements(),
                notificationPage.getTotalPages(),
                notificationPage.isFirst(),
                notificationPage.isLast()
        );
    }

    @Override
    @Transactional
    public NotificationResponse markAsRead(Long notificationId) {

        Notification notification =
                notificationRepository.findById(notificationId)
                        .orElseThrow(
                                () -> new NotificationNotFoundException(
                                        notificationId
                                )
                        );

        if (notification.getStatus() != NotificationStatus.READ) {
            notification.setStatus(NotificationStatus.READ);
            notification.setReadAt(Instant.now());

            notification = notificationRepository.save(notification);
        }

        return NotificationResponse.from(notification);
    }

    @Override
    @Transactional(readOnly = true)
    public NotificationPageResponse getUnreadNotifications(
            String userId,
            int page,
            int size
    ) {

        Pageable pageable = PageRequest.of(
                page,
                size,
                Sort.by(Sort.Direction.DESC, "createdAt")
        );

        Page<Notification> notificationPage =
                notificationRepository
                        .findByUserIdAndStatusOrderByCreatedAtDesc(
                                userId,
                                NotificationStatus.UNREAD,
                                pageable
                        );

        List<NotificationResponse> content =
                notificationPage.getContent()
                        .stream()
                        .map(NotificationResponse::from)
                        .toList();

        return new NotificationPageResponse(
                content,
                notificationPage.getNumber(),
                notificationPage.getSize(),
                notificationPage.getTotalElements(),
                notificationPage.getTotalPages(),
                notificationPage.isFirst(),
                notificationPage.isLast()
        );
    }
}