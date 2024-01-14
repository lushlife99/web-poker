package com.example.pokerv2.config;

import com.example.pokerv2.model.User;
import com.example.pokerv2.service.UserServiceV1;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.context.event.ApplicationReadyEvent;
import org.springframework.context.event.EventListener;
import org.springframework.stereotype.Component;


@Slf4j
@Component
@RequiredArgsConstructor
public class InitProject {

    private final UserServiceV1 userServiceV1;

    @EventListener(ApplicationReadyEvent.class)
    public void initData() {

        try {
            userServiceV1.join(User.builder().userName("a1").password("1234").userId("a1").build());
            userServiceV1.join(User.builder().userName("a2").password("1234").userId("a2").build());
            log.info("Initial Data Init");
        } catch (Exception e) {
            log.info("Initial fail. already exist user");
        }
    }
}
