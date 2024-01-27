package com.example.pokerv2.controller;

import com.example.pokerv2.dto.BoardDto;
import com.example.pokerv2.model.Board;
import com.example.pokerv2.repository.BoardRepository;
import com.example.pokerv2.service.BoardServiceV1;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.messaging.handler.annotation.DestinationVariable;
import org.springframework.messaging.handler.annotation.MessageMapping;
import org.springframework.transaction.annotation.Isolation;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.bind.annotation.*;

import java.security.Principal;
import java.util.List;

@RequiredArgsConstructor
@RestController
@RequestMapping("/api/board")
public class BoardController {

    private final BoardServiceV1 boardServiceV1;
    private final BoardRepository boardRepository;

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
        boardServiceV1.action(boardDto, option, principal.getName());
    }


    @GetMapping("/{boardId}")
    public BoardDto get(@PathVariable Long boardId, Principal principal) {
        return boardServiceV1.get(boardId, principal);
    }

    @PutMapping("/exit")
    public ResponseEntity exitGame(@RequestBody BoardDto board, Principal principal) {
        System.out.println(principal.getName());
        System.out.println(board);
        boardServiceV1.sitOut(board, principal.getName());
        return new ResponseEntity(HttpStatus.OK);
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
       return new BoardDto(boardServiceV1.startGame(boardId));
    }

    @PostMapping("/end/{boardId}")
    public ResponseEntity endGame(@PathVariable Long boardId) {
        boardServiceV1.endGameTest(boardId);
        return new ResponseEntity(HttpStatus.OK);
    }


}
