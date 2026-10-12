package com.automation.config;

import org.springframework.context.annotation.Configuration;
import org.springframework.web.socket.config.annotation.EnableWebSocket;
import org.springframework.web.socket.config.annotation.WebSocketConfigurer;
import org.springframework.web.socket.config.annotation.WebSocketHandlerRegistry;

import com.automation.controller.GlobalWebSocketHandler;

@Configuration
@EnableWebSocket
public class GlobalWebSocketConfig implements WebSocketConfigurer {

    private final GlobalWebSocketHandler globalWebSocketHandler;

    public GlobalWebSocketConfig(GlobalWebSocketHandler globalWebSocketHandler) {
        this.globalWebSocketHandler = globalWebSocketHandler;
    }

    @Override
    public void registerWebSocketHandlers(WebSocketHandlerRegistry registry) {
        registry.addHandler(globalWebSocketHandler, "/ws/app")
                .setAllowedOrigins("*");
    }
}
