package com.automation.simulator;

import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.WebSocket;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.CompletionStage;
import java.util.concurrent.TimeUnit;

public class RealtimeFlowTest {

    public static void main(String[] args) {
        String[] targetPorts = {"9011", "8089", "8080"};
        boolean testExecuted = false;

        for (String port : targetPorts) {
            String wsUrl = "ws://localhost:" + port + "/ws/app";
            System.out.println("==================================================");
            System.out.println(" Attempting connection to running SpringBoot at: " + wsUrl);
            System.out.println("==================================================");

            try {
                CompletableFuture<String> responseFuture = new CompletableFuture<>();
                HttpClient client = HttpClient.newHttpClient();

                WebSocket webSocket = client.newWebSocketBuilder()
                        .connectTimeout(java.time.Duration.ofSeconds(3))
                        .buildAsync(URI.create(wsUrl), new WebSocket.Listener() {
                            private final StringBuilder buffer = new StringBuilder();

                            @Override
                            public void onOpen(WebSocket ws) {
                                System.out.println("[REALTIME-TEST] Connected to SpringBoot UI WebSocket at " + wsUrl);
                                ws.request(1);
                            }

                            @Override
                            public CompletionStage<?> onText(WebSocket ws, CharSequence data, boolean last) {
                                buffer.append(data);
                                if (last) {
                                    String msg = buffer.toString();
                                    buffer.setLength(0);
                                    System.out.println("[REALTIME-TEST] Incoming message from SpringBoot: " + msg);
                                    if (msg.contains("AI_TASK_RESULT") || msg.contains("AI_HOST_RESOLVE_RESP") || msg.contains("AI_REMEDIATE_RESP")) {
                                        responseFuture.complete(msg);
                                    }
                                }
                                ws.request(1);
                                return null;
                            }

                            @Override
                            public void onError(WebSocket ws, Throwable error) {
                                System.err.println("[REALTIME-TEST] WebSocket Error on " + wsUrl + ": " + error.getMessage());
                                responseFuture.completeExceptionally(error);
                            }

                            @Override
                            public CompletionStage<?> onClose(WebSocket ws, int statusCode, String reason) {
                                System.out.println("[REALTIME-TEST] Connection closed on " + wsUrl + " (status: " + statusCode + ", reason: " + reason + ")");
                                return null;
                            }
                        })
                        .get(5, TimeUnit.SECONDS);

                testExecuted = true;
                String taskId = "realtime_task_" + System.currentTimeMillis();
                String jsonPayload = String.format(
                        "{\"event-type\":\"AI_EXECUTE_TASK\",\"taskId\":\"%s\",\"conversationId\":null,\"prompt\":\"You are an AI Server Infrastructure & Target Resolution Agent. User Input Query: \\\"find me the latest resume inside document folder.\\\" Server Inventory: []\",\"filesPath\":[]}",
                        taskId
                );

                System.out.println("[REALTIME-TEST] Sending AI_EXECUTE_TASK payload to SpringBoot...");
                System.out.println(" Payload: " + jsonPayload);
                webSocket.sendText(jsonPayload, true);

                System.out.println("[REALTIME-TEST] Awaiting response from SpringBoot (timeout 30s)...");
                String response = responseFuture.get(30, TimeUnit.SECONDS);
                System.out.println("==================================================");
                System.out.println(" REALTIME TEST RESULT:");
                System.out.println(" " + response);
                System.out.println("==================================================");
                webSocket.sendClose(WebSocket.NORMAL_CLOSURE, "Test Finished").get();
                break;

            } catch (Exception e) {
                System.err.println("[REALTIME-TEST] Could not test port " + port + ": " + e.getMessage());
            }
        }

        if (!testExecuted) {
            System.err.println("[REALTIME-TEST] FAILED: Could not connect to SpringBoot on ports 9011, 8089, or 8080.");
        }
    }
}
