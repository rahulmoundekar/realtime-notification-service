package com.rahul.realtime.notification.integration;

import com.rahul.realtime.notification.support.MultiInstanceApplicationLauncher;
import com.rahul.realtime.notification.support.MultiInstanceContext;
import org.junit.jupiter.api.Test;
import org.springframework.context.ConfigurableApplicationContext;

import static org.assertj.core.api.Assertions.assertThat;

class MultiInstanceApplicationLauncherTest {

    @Test
    void shouldStartTwoApplicationInstances() {

        MultiInstanceContext contexts = null;

        try {
            contexts =
                    MultiInstanceApplicationLauncher.startBoth();

            ConfigurableApplicationContext instance1 =
                    contexts.instance1();

            ConfigurableApplicationContext instance2 =
                    contexts.instance2();

            assertThat(instance1)
                    .isNotNull();

            assertThat(instance2)
                    .isNotNull();

            assertThat(instance1.isActive())
                    .isTrue();

            assertThat(instance2.isActive())
                    .isTrue();

        } finally {

            if (contexts != null) {
                contexts.close();
            }
        }
    }
}