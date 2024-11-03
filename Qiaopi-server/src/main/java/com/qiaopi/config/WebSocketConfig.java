package com.qiaopi.config;

import com.qiaopi.handler.Letter.LetterSocketHandler;
import com.qiaopi.interceptor.UserHandshakeInterceptor;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.socket.config.annotation.EnableWebSocket;
import org.springframework.web.socket.config.annotation.WebSocketConfigurer;
import org.springframework.web.socket.config.annotation.WebSocketHandlerRegistry;

@Configuration
@EnableWebSocket
public class WebSocketConfig implements WebSocketConfigurer {

    @Autowired
    private UserHandshakeInterceptor userHandshakeInterceptor;

    @Autowired
    private  final LetterSocketHandler letterSocketHandler;

    public WebSocketConfig(LetterSocketHandler letterSocketHandler) {
        this.letterSocketHandler = letterSocketHandler;
    }

    @Override
    public void registerWebSocketHandlers(WebSocketHandlerRegistry registry) {

        // 注册 边写信边生成的WebSocket处理器
        registry.addHandler(letterSocketHandler, "/ws/letterGen")
                .setAllowedOrigins("*") // 允许跨域
                .addInterceptors(userHandshakeInterceptor);  // 注册自定义拦截器

    }



}
