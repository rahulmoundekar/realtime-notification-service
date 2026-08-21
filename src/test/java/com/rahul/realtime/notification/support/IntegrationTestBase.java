package com.rahul.realtime.notification.support;

import org.springframework.test.context.DynamicPropertyRegistry;
import org.springframework.test.context.DynamicPropertySource;

public abstract class IntegrationTestBase {

    @DynamicPropertySource
    static void registerProperties(
            DynamicPropertyRegistry registry
    ) {

        registry.add(
                "spring.datasource.url",
                () -> "jdbc:postgresql://localhost:5432/realtime_notification"
        );

        registry.add(
                "spring.datasource.username",
                () -> "postgres"
        );

        registry.add(
                "spring.datasource.password",
                () -> "postgres"
        );

        registry.add(
                "spring.datasource.driver-class-name",
                () -> "org.postgresql.Driver"
        );

        registry.add(
                "spring.jpa.hibernate.ddl-auto",
                () -> "create-drop"
        );

        registry.add(
                "spring.data.redis.host",
                () -> "localhost"
        );

        registry.add(
                "spring.data.redis.port",
                () -> 6379
        );

        registry.add(
                "app.instance.id",
                () -> "integration-test"
        );
    }
}