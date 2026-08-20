package com.rahul.realtime.notification;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.scheduling.annotation.EnableScheduling;

@SpringBootApplication
@EnableScheduling
public class RealtimeNotificationApplication {

    public static void main(String[] args) {
        SpringApplication.run(RealtimeNotificationApplication.class, args);
    }
}
