package com.rahul.realtime.notification.integration;

import com.rahul.realtime.notification.support.MultiInstanceApplicationLauncher;
import com.rahul.realtime.notification.support.MultiInstanceContext;
import org.junit.jupiter.api.Test;

import java.io.BufferedReader;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.nio.charset.StandardCharsets;
import java.time.Duration;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicReference;

import static org.assertj.core.api.Assertions.assertThat;

class MultiInstanceSseIntegrationTest {

    private static final String TOKEN = "test-jwt-token";

    @Test
    void shouldDeliverNotificationAcrossInstancesViaSse() throws Exception {

        MultiInstanceContext contexts = null;

        SseTestClient sseClient = null;

        try {

            // ---------------------------------------------
            // 1. Start both application instances
            // ---------------------------------------------

            contexts = MultiInstanceApplicationLauncher.startBoth();

            // ---------------------------------------------
            // 2. Connect SSE to Instance 1
            // ---------------------------------------------

            sseClient = new SseTestClient("http://localhost:18080/api/v1/notifications/stream", TOKEN);

            sseClient.connect();

            assertThat(sseClient.awaitConnection(Duration.ofSeconds(10))).isTrue();

            // ---------------------------------------------
            // 3. Create notification through Instance 2
            // ---------------------------------------------

            HttpClient httpClient = HttpClient.newHttpClient();

            String requestBody = """
                    {
                      "type": "ORDER_STATUS",
                      "title": "Order Shipped",
                      "message": "Your order #ORD-6001 has been shipped."
                    }
                    """;

            HttpRequest request = HttpRequest.newBuilder().uri(URI.create("http://localhost:18081/api/v1/notifications")).header("Authorization", "Bearer " + TOKEN).header("Content-Type", "application/json").POST(HttpRequest.BodyPublishers.ofString(requestBody)).build();

            HttpResponse<String> response =
                    httpClient.send(
                            request,
                            HttpResponse.BodyHandlers.ofString()
                    );

            System.out.println(
                    "POST notification status = "
                            + response.statusCode()
            );

            System.out.println(
                    "POST notification response = "
                            + response.body()
            );

            assertThat(response.statusCode())
                    .isIn(200, 201);

            // ---------------------------------------------
            // 4. Wait for SSE notification
            // ---------------------------------------------

            assertThat(sseClient.awaitNotification(Duration.ofSeconds(10))).as("SSE client connected to instance 1 " + "should receive event published by instance 2").isTrue();

            // ---------------------------------------------
            // 5. Verify payload
            // ---------------------------------------------

            String payload = sseClient.getNotificationPayload();

            assertThat(payload).isNotBlank();

            assertThat(payload).contains("\"userId\":\"user-101\"");

            assertThat(payload).contains("\"type\":\"ORDER_STATUS\"");

            assertThat(payload).contains("\"title\":\"Order Shipped\"");

            assertThat(payload).contains("\"message\":\"Your order #ORD-6001 has been shipped.\"");

        } finally {

            if (sseClient != null) {
                sseClient.close();
            }

            if (contexts != null) {
                contexts.close();
            }
        }
    }

    private static final class SseTestClient {

        private final String url;

        private final String token;

        private final HttpClient client = HttpClient.newBuilder().connectTimeout(Duration.ofSeconds(10)).build();

        private final CompletableFuture<Void> connected = new CompletableFuture<>();

        private final CompletableFuture<Void> notificationReceived = new CompletableFuture<>();

        private final AtomicReference<String> notificationPayload = new AtomicReference<>();

        private final AtomicReference<Throwable> connectionError = new AtomicReference<>();

        private volatile InputStream inputStream;

        SseTestClient(String url, String token) {
            this.url = url;
            this.token = token;
        }

        void connect() {

            CompletableFuture.runAsync(() -> {

                try {

                    System.out.println("====================================");

                    System.out.println("Connecting SSE");

                    System.out.println("URL = " + url);

                    System.out.println("====================================");

                    HttpRequest request = HttpRequest.newBuilder().uri(URI.create(url)).timeout(Duration.ofSeconds(30)).header("Authorization", "Bearer " + token).header("Accept", "text/event-stream").header("Cache-Control", "no-cache").GET().build();

                    HttpResponse<InputStream> response = client.send(request, HttpResponse.BodyHandlers.ofInputStream());

                    System.out.println("SSE HTTP status = " + response.statusCode());

                    System.out.println("SSE headers = " + response.headers().map());

                    if (response.statusCode() != 200) {

                        throw new IllegalStateException("SSE connection failed. " + "HTTP status=" + response.statusCode());
                    }

                    inputStream = response.body();

                    connected.complete(null);

                    try (BufferedReader reader = new BufferedReader(new InputStreamReader(inputStream, StandardCharsets.UTF_8))) {

                        String line;

                        StringBuilder data = new StringBuilder();

                        while ((line = reader.readLine()) != null) {

                            System.out.println("SSE line = [" + line + "]");

                            if (line.startsWith("data:")) {

                                data.append(line.substring(5).trim());
                            }

                            if (line.isEmpty() && !data.isEmpty()) {

                                String event = data.toString();

                                System.out.println("SSE event = " + event);

                                if (event.contains("\"notificationId\"")) {

                                    notificationPayload.set(event);

                                    notificationReceived.complete(null);

                                    return;
                                }

                                data.setLength(0);
                            }
                        }

                    }

                } catch (Exception exception) {

                    connectionError.set(exception);

                    exception.printStackTrace();

                    connected.completeExceptionally(exception);

                    notificationReceived.completeExceptionally(exception);
                }
            });
        }

        boolean awaitConnection(Duration timeout) {

            try {

                connected.get(timeout.toMillis(), TimeUnit.MILLISECONDS);

                return true;

            } catch (Exception exception) {

                Throwable actual = connectionError.get();

                System.err.println("SSE connection failed:");

                if (actual != null) {
                    actual.printStackTrace();
                } else {
                    exception.printStackTrace();
                }

                return false;
            }
        }

        boolean awaitNotification(Duration timeout) {

            try {

                notificationReceived.get(timeout.toMillis(), TimeUnit.MILLISECONDS);

                return true;

            } catch (Exception exception) {

                Throwable actual = connectionError.get();

                System.err.println("SSE notification failed:");

                if (actual != null) {
                    actual.printStackTrace();
                } else {
                    exception.printStackTrace();
                }

                return false;
            }
        }

        String getNotificationPayload() {
            return notificationPayload.get();
        }

        void close() {

            try {

                if (inputStream != null) {
                    inputStream.close();
                }

            } catch (Exception exception) {

                System.err.println("SSE cleanup failed: " + exception.getMessage());
            }
        }
    }
}