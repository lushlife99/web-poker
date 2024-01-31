package com.example.pokerv2.service.handleService;

import com.example.pokerv2.dto.BoardDto;
import com.example.pokerv2.dto.MessageDto;
import com.example.pokerv2.enums.MessageType;
import com.example.pokerv2.enums.PhaseStatus;
import com.example.pokerv2.enums.PlayerAction;
import com.example.pokerv2.error.CustomException;
import com.example.pokerv2.error.ErrorCode;
import com.example.pokerv2.model.Board;
import com.example.pokerv2.model.HandHistory;
import com.example.pokerv2.repository.BoardRepository;
import com.example.pokerv2.service.ActionService;
import com.example.pokerv2.service.BoardServiceV1;
import com.example.pokerv2.service.HandHistoryService;
import lombok.RequiredArgsConstructor;
import org.springframework.messaging.simp.SimpMessagingTemplate;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Isolation;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
public class GameHandleService {

    private final BoardServiceV1 boardServiceV1;
//    private final ActionService actionService;
    private final BoardRepository boardRepository;
//    private final HandHistoryService handHistoryService;
    private final SimpMessagingTemplate simpMessagingTemplate;
    private final static String TOPIC_PREFIX = "/topic/board/";

    private final static int ACTION_TIME = 10;
    private final static int RESULT_ANIMATION_TIME = 5;


    @Transactional(isolation = Isolation.READ_COMMITTED)
    public void action(BoardDto boardDto, String option, String userId) {

//        actionService.saveAction(boardDto, option, userId);
        Board board = boardServiceV1.saveBoardChanges(boardDto, option, userId);

        while(true) {
            System.out.println(1);
            if(boardServiceV1.isGameEnd(board.getId())) {
                System.out.println(2);

                endGame(board.getId());
                break;
            }
            board = boardServiceV1.setNextAction(board.getId());

            if (board.getActionPos() == -1) {
                System.out.println(3);

                board = boardServiceV1.nextPhase(board);
                if (board.getPhaseStatus() != PhaseStatus.SHOWDOWN) {
                    System.out.println(4);

                    simpMessagingTemplate.convertAndSend(TOPIC_PREFIX + board.getId(), new MessageDto(MessageType.NEXT_PHASE_START.getDetail(), boardServiceV1.getRecentBoard(board.getId())));
                }
            } else {
                System.out.println(5);

                simpMessagingTemplate.convertAndSend(TOPIC_PREFIX + board.getId(), new MessageDto(MessageType.NEXT_ACTION.getDetail(), boardServiceV1.getRecentBoard(board.getId())));
            }

            if(boardServiceV1.isGameEnd(board.getId()) || board.getPhaseStatus() == PhaseStatus.SHOWDOWN) {
                System.out.println(6);

                endGame(board.getId());
                break;
            }

            if(boardServiceV1.isActionPlayerConnect(board.getId())) {
                System.out.println(11);
                break;
            }

            waitDisconnectPlayer();
            if(boardServiceV1.isActionPlayerConnect(board.getId())) {
                System.out.println(7);

                break;
            } else {
                System.out.println(8);

                boardDto = boardServiceV1.getRecentBoard(board.getId());
//                actionService.saveAction(boardDto, PlayerAction.FOLD.getActionDetail(), boardServiceV1.getCurrentActionUserId(board.getId()));
                board = boardServiceV1.saveBoardChanges(boardDto, PlayerAction.FOLD.getActionDetail(), boardServiceV1.getCurrentActionUserId(board.getId()));
                boardServiceV1.sitOut(boardDto, boardServiceV1.getCurrentActionUserId(board.getId()));
            }
        }
    }
    public void exitPlayer(BoardDto boardDto, String userId) {

        boardServiceV1.sitOut(boardDto, userId);
        boardDto = boardServiceV1.getRecentBoard(boardDto.getId());
        if (boardServiceV1.isGameEnd(boardDto.getId())) {
           endGame(boardDto.getId());
        }
    }

    public BoardDto startGame(Long boardId) {
        Board board = boardServiceV1.startGame(boardId);
        //HandHistory handHistory = handHistoryService.createHandHistory(boardId);
        return new BoardDto(board);
    }
    public void endGame(Long boardId) {

        Board board = boardRepository.findById(boardId).orElseThrow(() -> new CustomException(ErrorCode.BAD_REQUEST));
        boardServiceV1.initPhase(board.getId());
        int resultAnimationCount = 0;

        if (boardServiceV1.isGameEnd(board.getId())) {
            resultAnimationCount = boardServiceV1.winOnePlayer(board.getId());
        } else {
            resultAnimationCount = boardServiceV1.showDown(board.getId());
        }

        try {
            Thread.sleep(resultAnimationCount * RESULT_ANIMATION_TIME * 1000);
        } catch (InterruptedException e) {
            e.getStackTrace();
        }

        boardServiceV1.initBoard(board.getId());

        try {
            Thread.sleep(1000);
        } catch (InterruptedException e) {
            e.getStackTrace();
        }

        BoardDto boardDto = boardServiceV1.getRecentBoard(board.getId());
        System.out.println("GameHandleService.endGame");
        System.out.println(boardDto);
        if (boardDto.getTotalPlayer() >= 2) {
            startGame(board.getId());
        }
    }

    private static void waitDisconnectPlayer() {
        try {
            Thread.sleep(ACTION_TIME * 1000);
        } catch (InterruptedException e) {
            throw new CustomException(ErrorCode.INTERNAL_SERVER_ERROR);
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
