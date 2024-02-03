package com.example.pokerv2.stomp;


import com.example.pokerv2.service.PlayerService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.messaging.Message;
import org.springframework.messaging.MessageChannel;
import org.springframework.messaging.MessageDeliveryException;
import org.springframework.messaging.simp.stomp.StompCommand;
import org.springframework.messaging.simp.stomp.StompHeaderAccessor;
import org.springframework.messaging.support.ChannelInterceptor;
import org.springframework.messaging.support.MessageHeaderAccessor;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.AuthenticationException;
import org.springframework.stereotype.Component;


@RequiredArgsConstructor
@Component
@Slf4j
public class StompHandler implements ChannelInterceptor {

    private final WebSocketAuthenticatorService webSocketAuthenticatorService;
    private final PlayerService playerLifeCycleService;
    private static final String USERNAME_HEADER = "userId";
    private static final String PASSWORD_HEADER = "password";
    private static final String DISCONNECT_OPTION = "disconnect-option";
    private static final String disconnectValue = "exit";

    @Override
    public Message<?> preSend(final Message<?> message, final MessageChannel channel) throws AuthenticationException {
        final StompHeaderAccessor accessor = MessageHeaderAccessor.getAccessor(message, StompHeaderAccessor.class);
        if (StompCommand.CONNECT == accessor.getCommand()) {
            final String username = accessor.getFirstNativeHeader(USERNAME_HEADER);
            final String password = accessor.getFirstNativeHeader(PASSWORD_HEADER);
            if(username != null && password != null) {

                final UsernamePasswordAuthenticationToken user = webSocketAuthenticatorService.getAuthenticatedOrFail(username, password);
                accessor.setUser(user);
                playerLifeCycleService.setConnect(user);
            } else {
                throw new MessageDeliveryException("UNAUTHORIZED");
            }
        }
        else if (StompCommand.DISCONNECT == accessor.getCommand()) {
            final String disconnect_option = accessor.getFirstNativeHeader(DISCONNECT_OPTION);
            if(accessor.getUser() != null && disconnect_option.equals(disconnectValue)) {
                playerLifeCycleService.setDisconnect(accessor.getUser());
            }
        }
        return message;
    }

}