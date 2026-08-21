package com.rahul.realtime.notification.integration;

import com.rahul.realtime.notification.support.MultiInstanceApplicationLauncher;
import com.rahul.realtime.notification.support.MultiInstanceContext;
import org.junit.jupiter.api.Test;
import org.springframework.web.socket.CloseStatus;
import org.springframework.web.socket.TextMessage;
import org.springframework.web.socket.WebSocketHandler;
import org.springframework.web.socket.WebSocketHttpHeaders;
import org.springframework.web.socket.WebSocketSession;
import org.springframework.web.socket.client.WebSocketClient;
import org.springframework.web.socket.client.standard.StandardWebSocketClient;
import org.springframework.web.socket.handler.TextWebSocketHandler;

import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.time.Duration;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicReference;

import static org.assertj.core.api.Assertions.assertThat;

class MultiInstanceNotificationIntegrationTest {

    private static final String TEST_TOKEN = "test-jwt-token";

    private static final String USER_ID = "user-101";

    @Test
    void shouldDeliverNotificationAcrossInstances() throws Exception {

        MultiInstanceContext contexts = null;

        WebSocketSession webSocketSession = null;

        try {

            // -------------------------------------------------
            // 1. Start Instance 1 + Instance 2
            // -------------------------------------------------

            contexts = MultiInstanceApplicationLauncher.startBoth();

            // -------------------------------------------------
            // 2. Connect WebSocket to Instance 1
            // -------------------------------------------------

            TestWebSocketHandler webSocketHandler = new TestWebSocketHandler();

            WebSocketClient webSocketClient = new StandardWebSocketClient();

            WebSocketHttpHeaders headers = new WebSocketHttpHeaders();

            headers.setBearerAuth(TEST_TOKEN);

            CompletableFuture<WebSocketSession> webSocketFuture = webSocketClient.execute(webSocketHandler, headers, URI.create("ws://localhost:18080/ws/notifications"));

            webSocketSession = webSocketFuture.get(10, TimeUnit.SECONDS);

            assertThat(webSocketSession).isNotNull();

            assertThat(webSocketSession.isOpen()).isTrue();

            // -------------------------------------------------
            // 3. Create notification through Instance 2
            // -------------------------------------------------

            HttpClient httpClient = HttpClient.newHttpClient();

            String requestBody = """
                    {
                      "type": "ORDER_STATUS",
                      "title": "Order Shipped",
                      "message": "Your order #ORD-5001 has been shipped."
                    }
                    """;

            HttpRequest request = HttpRequest.newBuilder().uri(URI.create("http://localhost:18081/api/v1/notifications")).header("Authorization", "Bearer " + TEST_TOKEN).header("Content-Type", "application/json").POST(HttpRequest.BodyPublishers.ofString(requestBody)).build();

            HttpResponse<String> response = httpClient.send(request, HttpResponse.BodyHandlers.ofString());

            assertThat(response.statusCode()).isIn(200, 201);

            // -------------------------------------------------
            // 4. Wait for WebSocket notification
            // -------------------------------------------------

            boolean received = webSocketHandler.awaitMessage(Duration.ofSeconds(10));

            assertThat(received).as("WebSocket connected to instance 1 " + "should receive event published by instance 2").isTrue();

            // -------------------------------------------------
            // 5. Validate message
            // -------------------------------------------------

            String message = webSocketHandler.getMessage();

            assertThat(message).isNotBlank();

            assertThat(message).contains("\"userId\":\"" + USER_ID + "\"");

            assertThat(message).contains("\"type\":\"ORDER_STATUS\"");

            assertThat(message).contains("\"title\":\"Order Shipped\"");

            assertThat(message).contains("\"message\":\"Your order #ORD-5001 has been shipped.\"");

        } finally {

            if (webSocketSession != null && webSocketSession.isOpen()) {

                webSocketSession.close(CloseStatus.NORMAL);
            }

            if (contexts != null) {
                contexts.close();
            }
        }
    }

    private static class TestWebSocketHandler extends TextWebSocketHandler {

        private final AtomicReference<String> receivedMessage = new AtomicReference<>();

        private final CompletableFuture<Void> messageReceived = new CompletableFuture<>();

        @Override
        public void afterConnectionEstablished(WebSocketSession session) {

            System.out.println("Test WebSocket connected. sessionId=" + session.getId());
        }

        @Override
        protected void handleTextMessage(WebSocketSession session, TextMessage message) {

            System.out.println("Test WebSocket received: " + message.getPayload());

            receivedMessage.set(message.getPayload());

            messageReceived.complete(null);
        }

        @Override
        public void handleTransportError(WebSocketSession session, Throwable exception) {

            messageReceived.completeExceptionally(exception);
        }

        boolean awaitMessage(Duration timeout) {

            try {

                messageReceived.get(timeout.toMillis(), TimeUnit.MILLISECONDS);

                return true;

            } catch (Exception exception) {

                return false;
            }
        }

        String getMessage() {

            return receivedMessage.get();
        }
    }
}