package com.example.pokerv2.stomp;

import com.example.pokerv2.model.User;
import com.example.pokerv2.service.SessionManager;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.messaging.Message;
import org.springframework.messaging.MessageChannel;
import org.springframework.messaging.MessageDeliveryException;
import org.springframework.messaging.simp.stomp.StompCommand;
import org.springframework.messaging.simp.stomp.StompHeaderAccessor;
import org.springframework.messaging.support.ChannelInterceptor;
import org.springframework.stereotype.Component;

import java.util.Optional;

@RequiredArgsConstructor
@Component
@Slf4j
public class StompHandler implements ChannelInterceptor {

    private final SessionManager sessionManager;

    @Override
    public Message<?> preSend(Message<?> message, MessageChannel channel) {
        StompHeaderAccessor accessor = StompHeaderAccessor.wrap(message);

        if (StompCommand.CONNECT.equals(accessor.getCommand())) {
            String sessionId = accessor.getFirstNativeHeader(SessionManager.SESSION_COOKIE_NAME);
            if (sessionId != null) {
                Optional<User> user = sessionManager.getSession(sessionId);
                if(user.isEmpty())
                    throw new MessageDeliveryException("UNAUTHORIZED");

            } else throw new MessageDeliveryException("UNAUTHORIZED");
        }

        return message;
    }
}
