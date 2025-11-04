package com.tiffino.config;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.socket.config.annotation.*;

@Configuration
@EnableWebSocket
public class WebSocketConfig implements WebSocketConfigurer {

    private final ChatWebSocketHandler chatWebSocketHandler;
    private final JwtService jwtService;

    public WebSocketConfig(ChatWebSocketHandler chatWebSocketHandler, JwtService jwtService) {
        this.chatWebSocketHandler = chatWebSocketHandler;
        this.jwtService = jwtService;
    }

    @Override
    public void registerWebSocketHandlers(WebSocketHandlerRegistry registry) {
        registry.addHandler(chatWebSocketHandler, "/ws/chat")
                .addInterceptors(new JwtHandshakeInterceptor(jwtService))
                .setAllowedOrigins("http://localhost:4200"); // ✅ tighten for prod
    }
}

