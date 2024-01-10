package com.example.pokerv2.error;


import com.example.pokerv2.dto.MessageDto;
import com.example.pokerv2.enums.MessageType;
import com.example.pokerv2.model.User;
import com.example.pokerv2.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.messaging.handler.annotation.MessageExceptionHandler;
import org.springframework.messaging.simp.SimpMessagingTemplate;
import org.springframework.web.bind.annotation.RestControllerAdvice;

import java.util.Optional;

@Slf4j
@RestControllerAdvice
@RequiredArgsConstructor
public class GlobalWebsocketExHandler {

    private final SimpMessagingTemplate simpMessagingTemplate;
    private final UserRepository userRepository;

    @MessageExceptionHandler(CustomException.class)
    public void handleCustomException(CustomException ex) {

        userRepository.findByUserId(ex.getPrincipal().getName())
                .ifPresent(u -> simpMessagingTemplate.convertAndSend("/queue/error/" + u.getId(), new MessageDto(MessageType.ERROR.toString(), ex)));

    }

}
