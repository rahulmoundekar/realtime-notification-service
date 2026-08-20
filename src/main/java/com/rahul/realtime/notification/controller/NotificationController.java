package com.rahul.realtime.notification.controller;

import com.rahul.realtime.notification.dto.request.CreateNotificationRequest;
import com.rahul.realtime.notification.dto.response.NotificationPageResponse;
import com.rahul.realtime.notification.dto.response.NotificationResponse;
import com.rahul.realtime.notification.service.NotificationService;
import jakarta.validation.Valid;

import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/v1/notifications")
@RequiredArgsConstructor
@Validated
public class NotificationController {

    private final NotificationService notificationService;

    @PostMapping
    public ResponseEntity<NotificationResponse> createNotification(
            @Valid @RequestBody CreateNotificationRequest request
    ) {

        NotificationResponse response =
                notificationService.createNotification(request);

        return ResponseEntity
                .status(HttpStatus.CREATED)
                .body(response);
    }

    @GetMapping("/users/{userId}")
    public ResponseEntity<NotificationPageResponse> getUserNotifications(
            @PathVariable String userId,
            @RequestParam(defaultValue = "0")
            @Min(0)
            int page,

            @RequestParam(defaultValue = "20")
            @Min(1)
            @Max(100)
            int size
    ) {

        NotificationPageResponse response =
                notificationService.getUserNotifications(
                        userId,
                        page,
                        size
                );

        return ResponseEntity.ok(response);
    }

    @PatchMapping("/{notificationId}/read")
    public ResponseEntity<NotificationResponse> markAsRead(
            @PathVariable Long notificationId
    ) {

        NotificationResponse response =
                notificationService.markAsRead(notificationId);

        return ResponseEntity.ok(response);
    }

    @GetMapping("/users/{userId}/unread")
    public ResponseEntity<NotificationPageResponse> getUnreadNotifications(
            @PathVariable String userId,
            @RequestParam(defaultValue = "0")
            @Min(0)
            int page,

            @RequestParam(defaultValue = "20")
            @Min(1)
            @Max(100)
            int size
    ) {

        NotificationPageResponse response =
                notificationService.getUnreadNotifications(
                        userId,
                        page,
                        size
                );

        return ResponseEntity.ok(response);
    }
}