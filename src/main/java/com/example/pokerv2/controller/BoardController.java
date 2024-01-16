package com.example.pokerv2.controller;

import com.example.pokerv2.dto.BoardDto;
import com.example.pokerv2.service.BoardService;
import lombok.RequiredArgsConstructor;
import org.springframework.messaging.handler.annotation.MessageMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.security.Principal;

@RequiredArgsConstructor
@RestController
@RequestMapping("/api/board")
public class BoardController {

    private final BoardService boardService;
    @MessageMapping("/aaa")
    public void test(Principal principal){
        principal.getName();
    }

    @PostMapping("/joinGame")
    public BoardDto joinGame(@RequestParam int bb, Principal principal){
        System.out.println("principal = " + principal.getName());
        return boardService.join(bb, principal);
    }
}
