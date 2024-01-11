package com.example.pokerv2.controller;

import com.example.pokerv2.dto.BoardDto;
import com.example.pokerv2.error.CustomException;
import com.example.pokerv2.error.ErrorCode;
import com.example.pokerv2.model.Board;
import com.example.pokerv2.service.BoardServiceV1;
import jakarta.servlet.http.HttpServletRequest;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.messaging.handler.annotation.Header;
import org.springframework.messaging.handler.annotation.MessageMapping;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.ModelAndView;

import java.security.Principal;

@RequiredArgsConstructor
@RestController
@RequestMapping("/api/board")
public class BoardController {

    private final BoardServiceV1 boardServiceV1;
    private static final String PlayerId = "PlayerId";

    @PostMapping("/joinGame")
    public BoardDto joinGame(@RequestParam int bb, Principal principal) {

        System.out.println("BoardController.joinGame");
        return boardServiceV1.join(bb, principal);
    }

    @MessageMapping("/board/action")
    public void action(@RequestBody BoardDto boardDto, @Header(PlayerId) Long playerId, Principal principal){
        boardServiceV1.action(boardDto, playerId, principal);
    }

    @MessageMapping("/board/test")
    public void test(Principal principal){
        System.out.println(principal.getName());
    }

    @PostMapping("/test")
    public ResponseEntity httpTest(HttpServletRequest request){
        return new ResponseEntity(HttpStatus.OK);
    }

    /**
     * startGame
     *
     * 지금은 컨트롤러의 동작으로 게임을 실행하지만,
     * 나중에 게임로직이 다 완성되면 게임이 끝났을 때 알아서 재시작하게 만들거임.
     * 그렇게 되면 이 컨트롤러도 삭제.
     * @param boardDto
     * @param request
     * @return
     */

//    @PostMapping("/startGame")
//    public BoardDto startGame(@RequestBody BoardDto boardDto, HttpServletRequest request) {
//        return boardServiceV1.startGame(boardDto);
//    }
}
