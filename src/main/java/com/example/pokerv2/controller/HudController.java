package com.example.pokerv2.controller;

import com.example.pokerv2.dto.HudDto;
import com.example.pokerv2.service.HudService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.security.Principal;

@RestController
@RequiredArgsConstructor
@RequestMapping("/api/hud")
@Tag(name = "Hud")
public class HudController {

    private final HudService hudService;

    @GetMapping("/{userId}")
    @Operation(summary = "Hud 조회")
    public HudDto get(@PathVariable Long userId) {
        return hudService.get(userId);
    }

    @GetMapping
    @Operation(summary = "자신의 Hud 조회")
    public HudDto get(Principal principal) {
        return hudService.get(principal.getName());
    }
}
