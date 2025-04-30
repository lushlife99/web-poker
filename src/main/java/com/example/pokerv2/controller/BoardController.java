package com.example.pokerv2.controller;

import com.example.pokerv2.dto.BoardDto;
import com.example.pokerv2.service.BoardService;
import com.example.pokerv2.handler.game.GameHandler;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.messaging.handler.annotation.DestinationVariable;
import org.springframework.messaging.handler.annotation.MessageMapping;
import org.springframework.web.bind.annotation.*;

import java.security.Principal;
import java.util.List;

@Slf4j
@RequiredArgsConstructor
@RestController
@Tag(name = "Board")
@RequestMapping("/api/board")
public class BoardController {

    private final BoardService boardService;
    private final GameHandler gameHandler;

    @GetMapping("/context")
    @Operation(summary = "게임 문맥데이터 조회", description = "게임 플레이 중에 연결이 끊겼을 경우, 재 로그인 시 문맥데이터 반환")
    public List<BoardDto> getContext(Principal principal) {
        return boardService.getContext(principal);
    }

    @PostMapping("/joinGame")
    @Operation(summary = "게임 입장", description = "빠른 게임 입장")
    public BoardDto joinGame(@RequestParam int blind, @RequestParam int bb, Principal principal) {
        return gameHandler.joinRandomBoard(blind, bb, principal);
    }

    @PostMapping("/joinGame/{boardId}")
    @Operation(summary = "게임 입장", description = "선택한 게임 입장")
    public BoardDto joinGame(@RequestParam Long boardId, @RequestParam int bb, Principal principal) {
        return gameHandler.join(boardId, bb, principal);
    }

    @GetMapping("/search/{blind}")
    @Operation(summary = "Blind로 나눠진 게임 리스트 조회")
    public List<BoardDto> getBoardList(@PathVariable int blind) {
        return boardService.getBoardList(blind);
    }

    @GetMapping("/{boardId}")
    @Operation(summary = "단건 게임 조회")
    public BoardDto get(@PathVariable Long boardId, Principal principal) {
        return boardService.get(boardId, principal);
    }

    @MessageMapping("/board/exit")
    public void exitGame(@RequestBody BoardDto board, Principal principal) {
        gameHandler.exitPlayer(board, principal.getName());
    }

    @MessageMapping("/board/action/{option}")
    public void action(@RequestBody BoardDto boardDto, @DestinationVariable String option, Principal principal){
        gameHandler.action(boardDto, option, principal.getName());
    }


}
