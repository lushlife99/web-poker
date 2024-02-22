package com.example.pokerv2.controller;

import com.example.pokerv2.dto.HandHistoryDto;
import com.example.pokerv2.service.HandHistoryService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.security.Principal;
import java.util.List;

@RequestMapping("/api/handHistory")
@RestController
@RequiredArgsConstructor
@Tag(name = "HandHistory")
public class HandHistoryController {

    private final HandHistoryService handHistoryService;

    @GetMapping
    @Operation(summary = "핸드 히스토리 조회")
    public List<HandHistoryDto> get(Principal principal) {
        return handHistoryService.get(principal.getName());
    }
}
