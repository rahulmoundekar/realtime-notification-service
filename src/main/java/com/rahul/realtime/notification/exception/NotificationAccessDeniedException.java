package com.rahul.realtime.notification.exception;

public class NotificationAccessDeniedException
        extends RuntimeException {

    public NotificationAccessDeniedException(
            Long notificationId
    ) {
        super(
                "Access denied for notification: "
                        + notificationId
        );
    }
}