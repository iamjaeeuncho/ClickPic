package com.phoenix.clickpic.config;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.socket.config.annotation.EnableWebSocket;
import org.springframework.web.socket.config.annotation.WebSocketConfigurer;
import org.springframework.web.socket.config.annotation.WebSocketHandlerRegistry;
import org.springframework.web.socket.handler.TextWebSocketHandler;

import com.phoenix.clickpic.websocket.controller.TurnWebSocketHandler;

/**
 * packageName    : 
 * fileName       : 
 * author         : 강희원
 * date           : 2024-09-03
 * description    :
 * ===========================================================
 * DATE              AUTHOR             NOTE
 * -----------------------------------------------------------
 * 2024-09-03        강희원             최초 생성
 */

@Configuration
@EnableWebSocket
public class WebSocketConfig implements WebSocketConfigurer {

    @Override
    public void registerWebSocketHandlers(WebSocketHandlerRegistry registry) {
        registry.addHandler(turnWebSocketHandler(), "/ws/turn").setAllowedOrigins("*");
    }

    @Bean
    public TextWebSocketHandler turnWebSocketHandler() {
        return new TurnWebSocketHandler();
    }
}
