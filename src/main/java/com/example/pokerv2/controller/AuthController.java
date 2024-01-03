package com.example.pokerv2.controller;

import com.example.pokerv2.dto.UserDto;
import com.example.pokerv2.service.UserServiceV1;
import jakarta.servlet.http.HttpServletResponse;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequiredArgsConstructor
public class AuthController {

    private final UserServiceV1 userServiceV1;

    @PostMapping("/join")
    public ResponseEntity join(@RequestParam String userId, @RequestParam String password, @RequestParam String userName) {

        userServiceV1.join(userId, userName, password);
        return new ResponseEntity(HttpStatus.OK);
    }

    @PostMapping("/login")
    public UserDto login(@RequestParam String userId, @RequestParam String password, HttpServletResponse response) {

        return userServiceV1.login(userId, password, response);
    }


}