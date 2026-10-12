package com.automation.controller;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;
import org.springframework.web.socket.CloseStatus;
import org.springframework.web.socket.TextMessage;
import org.springframework.web.socket.WebSocketSession;
import org.springframework.web.socket.handler.TextWebSocketHandler;

import com.automation.service.GlobalUiWebSocketChannel;

@Component
public class GlobalWebSocketHandler extends TextWebSocketHandler {

    private static final Logger log = LoggerFactory.getLogger(GlobalWebSocketHandler.class);

    private final GlobalUiWebSocketChannel globalUiWebSocketChannel;

    public GlobalWebSocketHandler(GlobalUiWebSocketChannel globalUiWebSocketChannel) {
        this.globalUiWebSocketChannel = globalUiWebSocketChannel;
    }

    @Override
    public void afterConnectionEstablished(WebSocketSession session) throws Exception {
        log.info("Browser client connected to Global WebSocket (/ws/app): {}", session.getId());
        globalUiWebSocketChannel.registerUiSession(session);
    }

    @Override
    protected void handleTextMessage(WebSocketSession session, TextMessage message) throws Exception {
        String payload = message.getPayload();
        log.trace("Received payload from browser UI session {}: {}", session.getId(), payload);
        globalUiWebSocketChannel.handleUiMessage(session, payload);
    }

    @Override
    public void afterConnectionClosed(WebSocketSession session, CloseStatus status) throws Exception {
        log.info("Browser client disconnected from Global WebSocket: {} (status={})", session.getId(), status);
        globalUiWebSocketChannel.unregisterUiSession(session);
    }

    @Override
    public void handleTransportError(WebSocketSession session, Throwable exception) throws Exception {
        log.warn("Transport error for UI WebSocket session {}: {}", session.getId(), exception.getMessage());
    }
}
