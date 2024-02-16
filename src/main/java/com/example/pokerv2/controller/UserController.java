package com.example.pokerv2.controller;

import com.example.pokerv2.dto.UserDto;
import com.example.pokerv2.service.UserServiceV1;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.security.Principal;

@RestController
@RequiredArgsConstructor
@RequestMapping("/api/user")
public class UserController {

    private final UserServiceV1 userServiceV1;

    @GetMapping("/profile")
    public UserDto getUserProfile(Principal principal) {
        return userServiceV1.getMyProfile(principal);
    }

    @GetMapping(value = "/image/{imagePath}", produces = MediaType.IMAGE_JPEG_VALUE)
    public ResponseEntity<byte[]> getImage(@PathVariable String imagePath) {

        byte[] img = userServiceV1.getUserImage(imagePath);
        return new ResponseEntity<>(img, HttpStatus.OK);
    }

    @PostMapping("/image")
    public UserDto updateImage(@RequestParam MultipartFile file, Principal principal) {
        return userServiceV1.updateUserImage(file, principal);
    }
}

