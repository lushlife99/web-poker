//package com.example.pokerv2.service;
//
//import com.example.pokerv2.dto.BoardDto;
//import com.example.pokerv2.repository.ActionRepository;
//import com.example.pokerv2.repository.BoardRepository;
//import com.example.pokerv2.repository.PlayerRepository;
//import com.example.pokerv2.repository.UserRepository;
//import jakarta.transaction.Transactional;
//import lombok.RequiredArgsConstructor;
//import org.springframework.messaging.simp.SimpMessagingTemplate;
//import org.springframework.stereotype.Service;
//
//import java.security.Principal;
//
//@Service
//@RequiredArgsConstructor
//public class BoardService {
//
//    private static final int MAX_PLAYER = 6;
//    private final BoardRepository boardRepository;
//    private final UserRepository userRepository;
//    private final PlayerRepository playerRepository;
//    private final SimpMessagingTemplate simpMessagingTemplate;
//    private final ActionRepository actionRepository;
//
//    /**
//     * 게임 입장 서비스
//     *
//     * 1. 방 입장
//     *  1) 게임을 대기중인 방으로 입장
//     *  2) 최대 6명
//     *  3)
//     */
//
//    @Transactional
//    public BoardDto join(int requestBb, Principal principal){
//
//    }
//}
