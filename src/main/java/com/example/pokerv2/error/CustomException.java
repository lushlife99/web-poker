package com.example.pokerv2.error;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.security.Principal;

@Getter
@AllArgsConstructor
public class CustomException extends RuntimeException{
    private final ErrorCode errorCode;

}
