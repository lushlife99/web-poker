package com.example.pokerv2.controller;

import com.example.pokerv2.model.User;
import com.example.pokerv2.service.UserService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequiredArgsConstructor
@Tag(name = "Auth")
public class AuthController {

    private final UserService userService;

    @PostMapping("/join")
    @Operation(summary = "회원가입", description = "id, name, password로 회원가입 한다.")
    public ResponseEntity join(@RequestBody User user) {
        userService.join(user);
        return new ResponseEntity(HttpStatus.OK);
    }

}

