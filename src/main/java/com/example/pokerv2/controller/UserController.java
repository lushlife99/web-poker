package com.example.pokerv2.controller;

import com.example.pokerv2.dto.UserDto;
import com.example.pokerv2.service.UserServiceV1;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
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
@Tag(name = "User")
public class UserController {

    private final UserServiceV1 userServiceV1;

    @GetMapping("/profile")
    @Operation(summary = "자기 정보 조회", description = "자기 프로필 조회")
    public UserDto getUserProfile(Principal principal) {
        return userServiceV1.getMyProfile(principal);
    }

    @GetMapping(value = "/image/{userId}", produces = MediaType.IMAGE_JPEG_VALUE)
    @Operation(summary = "이미지 조회")
    public ResponseEntity<byte[]> getImage(@PathVariable Long userId) {
        byte[] img = userServiceV1.getUserImage(userId);
        return new ResponseEntity<>(img, HttpStatus.OK);
    }
    
    @PostMapping("/image")
    @Operation(summary = "이미지 등록, 변경")
    public UserDto updateImage(@RequestParam MultipartFile file, Principal principal) {
        return userServiceV1.updateUserImage(file, principal);
    }

}

