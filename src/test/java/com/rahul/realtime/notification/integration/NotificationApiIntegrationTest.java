package com.rahul.realtime.notification.integration;

import com.rahul.realtime.notification.support.TestJwtDecoderConfig;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.webmvc.test.autoconfigure.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.context.annotation.Import;
import org.springframework.test.web.servlet.MockMvc;

import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.jwt;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@SpringBootTest
@AutoConfigureMockMvc
@Import(TestJwtDecoderConfig.class)
class NotificationApiIntegrationTest {

    @Autowired
    private MockMvc mockMvc;

    @Test
    void shouldRejectUnauthenticatedRequest() throws Exception {

        mockMvc.perform(get("/api/v1/notifications/me")).andExpect(status().isUnauthorized());
    }

    @Test
    void shouldAllowAuthenticatedUser() throws Exception {

        mockMvc.perform(get("/api/v1/notifications/me").with(jwt().jwt(jwt -> jwt.subject("user-101")))).andExpect(status().isOk());
    }
}