package com.example.pokerv2.controller;

import com.example.pokerv2.model.Board;
import com.example.pokerv2.model.User;
import com.example.pokerv2.repository.BoardRepository;
import com.example.pokerv2.service.UserServiceV1;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.transaction.annotation.Isolation;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.bind.annotation.*;

@RestController
@RequiredArgsConstructor
public class AuthController {

    private final UserServiceV1 userService;
    private final BoardRepository boardRepository;

    @PostMapping("/join")
    public ResponseEntity join(@RequestBody User user) {
        userService.join(user);
        return new ResponseEntity(HttpStatus.OK);
    }

    @Transactional(isolation = Isolation.READ_COMMITTED)
    @PostMapping("/test/1")
    public String test1() throws InterruptedException {
        String s = "";

        Board board = boardRepository.findById(1L).get();
        s += "current : " + board.getTotalPlayer() + "\n";

        Thread.sleep(5000);

        Board board2 = boardRepository.findById(1L).get();
        s += "update : " + board2.getTotalPlayer();

        return s;
    }

    @PostMapping("/test/2")
    public int test2() {
        Board board = boardRepository.findById(1L).get();
        board.setTotalPlayer(board.getTotalPlayer() + 1);
        boardRepository.save(board);
        return board.getTotalPlayer();
    }



}

