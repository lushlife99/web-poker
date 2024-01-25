package com.example.pokerv2.controller;

import com.example.pokerv2.dto.BoardDto;
import com.example.pokerv2.service.PlayerLifeCycleService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.security.Principal;


@RestController
@RequiredArgsConstructor
@RequestMapping("/api/player")
public class PlayerController {

    private final PlayerLifeCycleService playerLifeCycleService;

    @PostMapping("/connect/{playerId}")
    public ResponseEntity getPlayingBoardContext(@PathVariable Long playerId, Principal principal) {
        playerLifeCycleService.setConnect(playerId, principal);

        return new ResponseEntity(HttpStatus.OK);
    }
}
