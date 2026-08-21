package com.rahul.realtime.notification.integration;

import com.rahul.realtime.notification.sse.SseConnectionManager;
import org.junit.jupiter.api.Test;
import org.springframework.web.servlet.mvc.method.annotation.SseEmitter;

import static org.assertj.core.api.Assertions.assertThat;

class SseConnectionManagerTest {

    @Test
    void shouldRegisterAndRemoveSseEmitter() {

        SseConnectionManager manager =
                new SseConnectionManager();

        SseEmitter emitter =
                manager.addEmitter("user-101");

        assertThat(
                manager.getEmitterCount("user-101")
        )
                .isEqualTo(1);

        manager.removeEmitter(
                "user-101",
                emitter
        );

        assertThat(
                manager.getEmitterCount("user-101")
        )
                .isZero();
    }
}