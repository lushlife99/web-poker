package com.example.pokerv2.controller;

import com.example.pokerv2.dto.BoardDto;
import com.example.pokerv2.service.BoardServiceV1;
import com.example.pokerv2.service.handleService.GameHandleService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.messaging.handler.annotation.DestinationVariable;
import org.springframework.messaging.handler.annotation.MessageMapping;
import org.springframework.web.bind.annotation.*;

import java.security.Principal;
import java.util.List;

@RequiredArgsConstructor
@RestController
@RequestMapping("/api/board")
public class BoardController {

    private final BoardServiceV1 boardServiceV1;
    private final GameHandleService gameHandleService;

    @GetMapping("/context")
    public List<BoardDto> getContext(Principal principal) {
        return boardServiceV1.getContext(principal);
    }

    @PostMapping("/joinGame")
    public BoardDto joinGame(@RequestParam int bb, Principal principal) {

        return boardServiceV1.join(bb, principal);
    }

    @MessageMapping("/board/action/{option}")
    public void action(@RequestBody BoardDto boardDto, @DestinationVariable String option, Principal principal){

        gameHandleService.action(boardDto, option, principal.getName());
    }

    @GetMapping("/{boardId}")
    public BoardDto get(@PathVariable Long boardId, Principal principal) {
        return boardServiceV1.get(boardId, principal);
    }

    @MessageMapping("/board/exit")
    public void exitGame(@RequestBody BoardDto board, Principal principal) {
        gameHandleService.exitPlayer(board, principal.getName());
    }

    /**
     * startGame
     * endGame
     * 지금은 컨트롤러의 동작으로 게임을 실행하지만,
     * 나중에 게임로직이 다 완성되면 게임이 끝났을 때 알아서 재시작하게 만들거임.
     * 그렇게 되면 이 컨트롤러도 삭제.
     *
     * @param boardId
     * @return
     */

    @PostMapping("/start/{boardId}")
    public BoardDto startGame(@PathVariable Long boardId) {
       return gameHandleService.startGame(boardId);
    }

    @PostMapping("/end/{boardId}")
    public ResponseEntity endGame(@PathVariable Long boardId) {
        gameHandleService.endGameTest(boardId);
        return new ResponseEntity(HttpStatus.OK);
    }
}
