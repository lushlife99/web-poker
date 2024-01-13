package com.example.pokerv2.stomp;

import com.example.pokerv2.Stomp.WebSocketAuthenticatorService;
import jakarta.security.auth.message.AuthException;
import lombok.RequiredArgsConstructor;
import lombok.SneakyThrows;
import org.springframework.messaging.Message;
import org.springframework.messaging.MessageChannel;
import org.springframework.messaging.simp.stomp.StompCommand;
import org.springframework.messaging.simp.stomp.StompHeaderAccessor;
import org.springframework.messaging.support.ChannelInterceptor;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.stereotype.Component;

@RequiredArgsConstructor
@Component
/**
 *  2024/1/13
 *
 *  preSend 메소드에서 클라이언트가 CONNECT할 때 헤더로 보낸 Authorization에 담긴 jwt Token을 검증하도록함
 *  근데 우리는 jwt를 사용안하니까 다른 방식을 생각
 *
 */
public class StompHandler implements ChannelInterceptor {
    private final WebSocketAuthenticatorService webSocketAuthenticatorService;
    @SneakyThrows
    @Override
    public Message<?> preSend(Message<?> message, MessageChannel channel){
        StompHeaderAccessor accessor = StompHeaderAccessor.wrap(message);
        if(accessor.getCommand() == StompCommand.CONNECT){
            String username = accessor.getFirstNativeHeader("userId");
            String password = accessor.getFirstNativeHeader("password");
            if(username == null || password == null){
                throw new AuthException("Authentication failed");
            }
            UsernamePasswordAuthenticationToken user = webSocketAuthenticatorService.getAuthenticatedOrFail(username, password);
            accessor.setUser(user);
            // 블로그에서는 토큰을 발급
        }
        return message;
    }
}