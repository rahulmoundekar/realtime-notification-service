package com.rahul.realtime.notification.support;

import org.springframework.boot.test.context.TestConfiguration;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Primary;
import org.testcontainers.containers.GenericContainer;
import org.testcontainers.containers.PostgreSQLContainer;

@TestConfiguration(proxyBeanMethods = false)
public class IntegrationTestContainers {

    @Bean
    @Primary
    PostgreSQLContainer<?> postgresContainer() {

        PostgreSQLContainer<?> container =
                new PostgreSQLContainer<>("postgres:17")
                        .withDatabaseName(
                                "realtime_notification_test"
                        )
                        .withUsername("test")
                        .withPassword("test");

        container.start();

        return container;
    }

    @Bean
    @Primary
    GenericContainer<?> redisContainer() {

        GenericContainer<?> container =
                new GenericContainer<>(
                        "redis:7"
                )
                        .withExposedPorts(6379);

        container.start();

        return container;
    }
}