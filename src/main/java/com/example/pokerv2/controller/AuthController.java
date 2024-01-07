package com.example.pokerv2.controller;

import com.example.pokerv2.dto.UserDto;
import com.example.pokerv2.model.User;
import com.example.pokerv2.service.UserAuthenticationService;
import com.example.pokerv2.service.UserServiceV1;
import jakarta.servlet.http.HttpServletResponse;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequiredArgsConstructor
public class AuthController {

    private final UserServiceV1 userService;

    @PostMapping("/join")
    public ResponseEntity join(@RequestBody User user) {
        System.out.println("AuthController.join");
        userService.join(user);
        return new ResponseEntity(HttpStatus.OK);
    }

//    @PostMapping("/login")
//    public UserDto login(@RequestBody User user, HttpServletResponse response) {
//        System.out.println("AuthController.login");
//        return userService.login(user, response);
//    }



}

