package com.example.pokerv2.service.handleService;

import com.example.pokerv2.dto.BoardDto;
import com.example.pokerv2.enums.PhaseStatus;
import com.example.pokerv2.enums.PlayerStatus;
import com.example.pokerv2.error.CustomException;
import com.example.pokerv2.error.ErrorCode;
import com.example.pokerv2.model.Board;
import com.example.pokerv2.model.Player;
import com.example.pokerv2.repository.BoardRepository;
import com.example.pokerv2.service.ActionService;
import com.example.pokerv2.service.BoardServiceV1;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Isolation;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
public class GameHandleService {

    private final BoardServiceV1 boardServiceV1;
    private final ActionService actionService;
    private final BoardRepository boardRepository;

    @Transactional(isolation = Isolation.READ_COMMITTED)
    public void action(BoardDto boardDto, String option, String userId) {

        Board board = boardServiceV1.saveBoardChanges(boardDto, option, userId);

        while(true) {
            System.out.println("GameHandleService.action");
            boardServiceV1.action(board.getId());

            if(boardServiceV1.isGameEnd(board.getId()) || board.getPhaseStatus() == PhaseStatus.SHOWDOWN) {
                endGame(board.getId());
                break;
            }

            if(boardServiceV1.isActionPlayerConnect(board.getId())) {
                break;
            }

            BoardServiceV1.waitDisconnectPlayer();

            if(boardServiceV1.isActionPlayerConnect(board.getId())) {
                break;
            } else {
                boardServiceV1.setPlayerDisconnectFold(board.getId());
            }
        }
    }

    public void exitPlayer(BoardDto boardDto, String userId) {

        boardServiceV1.sitOut(boardDto, userId);
        Board board = boardServiceV1.getRecentBoard(boardDto.getId());
        if (boardServiceV1.isGameEnd(board.getId())) {
           endGame(board.getId());
        }
    }

    public void endGame(Long boardId) {

        Board board = boardRepository.findById(boardId).orElseThrow(() -> new CustomException(ErrorCode.BAD_REQUEST));
        boardServiceV1.initPhase(board.getId());

        if (boardServiceV1.isGameEnd(board.getId())) {
            boardServiceV1.winOnePlayer(board.getId());
        } else {
            boardServiceV1.showDown(board.getId());
        }

        boardServiceV1.initBoard(board.getId());

        try {
            Thread.sleep(1000);
        } catch (InterruptedException e) {
            e.getStackTrace();
        }

        board = boardServiceV1.getRecentBoard(board.getId());

        if (board.getTotalPlayer() >= 2) {
            boardServiceV1.startGame(board.getId());
        }
    }


    /**
     * endGameTest
     * test 끝나면 삭제
     * @param boardId
     */
    @Transactional
    public void endGameTest(Long boardId) {
        Board board = boardRepository.findById(boardId).orElseThrow(() -> new CustomException(ErrorCode.BAD_REQUEST));
        board.setPhaseStatus(PhaseStatus.SHOWDOWN);
        endGame(boardId);
    }


}
