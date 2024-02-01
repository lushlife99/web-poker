package com.example.pokerv2.controller;

import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;


@RestController
@RequiredArgsConstructor
@RequestMapping("/api/player")
public class PlayerController {

//    @PostMapping("/connect/{playerId}")
//    public ResponseEntity getPlayingBoardContext(@PathVariable Long playerId, Principal principal) {
//        playerLifeCycleService.setConnect(playerId, principal);
//
//        return new ResponseEntity(HttpStatus.OK);
//    }

}
