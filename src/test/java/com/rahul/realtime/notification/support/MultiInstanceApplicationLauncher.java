package com.rahul.realtime.notification.support;

import com.rahul.realtime.notification.RealtimeNotificationApplication;
import org.springframework.boot.SpringApplication;
import org.springframework.context.ConfigurableApplicationContext;
import com.rahul.realtime.notification.support.TestJwtDecoderConfig;
import java.util.HashMap;
import java.util.Map;

public final class MultiInstanceApplicationLauncher {

    private static final String POSTGRES_URL = "jdbc:postgresql://localhost:5432/realtime_notification";

    private static final String POSTGRES_USERNAME = "postgres";

    private static final String POSTGRES_PASSWORD = "root";

    private static final String REDIS_HOST = "localhost";

    private static final int REDIS_PORT = 6379;

    private static final String REDIS_CHANNEL = "notification-events";

    private MultiInstanceApplicationLauncher() {
    }

    public static ConfigurableApplicationContext start(int port, String instanceId) {

        SpringApplication application =
                new SpringApplication(
                        RealtimeNotificationApplication.class,
                        TestJwtDecoderConfig.class
                );

        Map<String, Object> properties = new HashMap<>();

        properties.put("server.port", port);

        properties.put("app.instance.id", instanceId);

        // PostgreSQL
        properties.put("spring.datasource.url", POSTGRES_URL);

        properties.put("spring.datasource.username", POSTGRES_USERNAME);

        properties.put("spring.datasource.password", POSTGRES_PASSWORD);

        properties.put("spring.datasource.driver-class-name", "org.postgresql.Driver");

        // Redis
        properties.put("spring.data.redis.host", REDIS_HOST);

        properties.put("spring.data.redis.port", REDIS_PORT);

        // Application
        properties.put("app.redis.notification-channel", REDIS_CHANNEL);

        properties.put("app.websocket.allowed-origins", "http://localhost:3000,http://localhost:5173");

        properties.put("app.sse.timeout", "1800000");

        properties.put("app.sse.heartbeat-interval", "25000");

        application.setDefaultProperties(properties);

        return application.run();
    }

    public static MultiInstanceContext startBoth() {

        ConfigurableApplicationContext instance1 = start(18080, "test-instance-1");

        try {

            ConfigurableApplicationContext instance2 = start(18081, "test-instance-2");

            return new MultiInstanceContext(instance1, instance2);

        } catch (RuntimeException exception) {

            instance1.close();

            throw exception;
        }
    }
}