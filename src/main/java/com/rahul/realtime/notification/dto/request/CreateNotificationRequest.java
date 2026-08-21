package com.rahul.realtime.notification.dto.request;

import com.rahul.realtime.notification.enums.NotificationType;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;

public record CreateNotificationRequest(

        @NotNull(message = "notification type is required")
        NotificationType type,

        @NotBlank(message = "title is required")
        @Size(max = 255, message = "title must not exceed 255 characters")
        String title,

        @NotBlank(message = "message is required")
        String message
) {
}
