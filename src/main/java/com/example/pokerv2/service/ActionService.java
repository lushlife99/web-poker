package com.example.pokerv2.service;

import com.example.pokerv2.dto.BoardDto;
import com.example.pokerv2.error.CustomException;
import com.example.pokerv2.error.ErrorCode;
import com.example.pokerv2.model.Board;
import com.example.pokerv2.repository.ActionRepository;
import com.example.pokerv2.repository.BoardRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.security.Principal;

@Slf4j
@Service
@RequiredArgsConstructor
public class ActionService {

    private final ActionRepository actionRepository;
    private final BoardRepository boardRepository;
    public void saveAction(BoardDto boardDto, Principal principal) {
        Board board = boardRepository.findById(boardDto.getId()).orElseThrow(() -> new CustomException(ErrorCode.BAD_REQUEST));

    }
}
