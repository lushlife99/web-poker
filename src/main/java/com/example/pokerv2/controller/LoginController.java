package com.example.pokerv2.controller;


import com.example.pokerv2.model.User;
import com.example.pokerv2.service.UserService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

@RequiredArgsConstructor
@RestController
public class LoginController {
    private final UserService userService;
    @PostMapping("/join")
    public ResponseEntity join(@RequestBody User user) {
        System.out.println("AuthController.join");
        userService.join(user);
        return new ResponseEntity(HttpStatus.OK);
    }
}
