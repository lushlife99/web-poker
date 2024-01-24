package com.example.pokerv2.controller;

import com.example.pokerv2.model.User;
import com.example.pokerv2.service.UserServiceV1;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

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

}

