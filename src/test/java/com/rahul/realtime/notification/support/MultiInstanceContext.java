package com.rahul.realtime.notification.support;

import org.springframework.context.ConfigurableApplicationContext;

public final class MultiInstanceContext implements AutoCloseable {

    private final ConfigurableApplicationContext instance1;

    private final ConfigurableApplicationContext instance2;

    public MultiInstanceContext(ConfigurableApplicationContext instance1, ConfigurableApplicationContext instance2) {
        this.instance1 = instance1;
        this.instance2 = instance2;
    }

    public ConfigurableApplicationContext instance1() {
        return instance1;
    }

    public ConfigurableApplicationContext instance2() {
        return instance2;
    }

    @Override
    public void close() {

        closeContext(instance1);
        closeContext(instance2);
    }

    private void closeContext(ConfigurableApplicationContext context) {

        if (context != null && context.isActive()) {
            context.close();
        }
    }
}