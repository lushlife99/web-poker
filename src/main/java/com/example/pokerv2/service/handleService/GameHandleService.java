package com.example.pokerv2.service.handleService;

import com.example.pokerv2.dto.BoardDto;
import com.example.pokerv2.dto.GameResultDto;
import com.example.pokerv2.dto.MessageDto;
import com.example.pokerv2.dto.PlayerDto;
import com.example.pokerv2.enums.MessageType;
import com.example.pokerv2.enums.PhaseStatus;
import com.example.pokerv2.enums.PlayerAction;
import com.example.pokerv2.error.CustomException;
import com.example.pokerv2.error.ErrorCode;
import com.example.pokerv2.model.Board;
import com.example.pokerv2.repository.BoardRepository;
import com.example.pokerv2.service.ActionService;
import com.example.pokerv2.service.BoardServiceV1;
import com.example.pokerv2.service.HandHistoryService;
import lombok.RequiredArgsConstructor;
import org.springframework.messaging.simp.SimpMessagingTemplate;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.security.Principal;

@Service
@RequiredArgsConstructor
public class GameHandleService {

    private final BoardServiceV1 boardServiceV1;
    private final ActionService actionService;
    private final BoardRepository boardRepository;
    private final HandHistoryService handHistoryService;
    private final SimpMessagingTemplate simpMessagingTemplate;
    private final static String TOPIC_PREFIX = "/topic/board/";

    private final static int ACTION_TIME = 10;
    private final static int RESULT_ANIMATION_TIME = 5;


    public BoardDto join(int requestBb, Principal principal) {
        BoardDto boardDto = boardServiceV1.join(requestBb, principal);
        sendUpdateBoardToPlayers(boardDto.getId(), MessageType.PLAYER_JOIN);

        if(boardDto.getPhaseStatus() == PhaseStatus.WAITING.ordinal()) {
            startGame(boardDto.getId());
        }
        return boardServiceV1.getBoard(boardDto.getId());
    }

    public void action(BoardDto boardDto, String option, String userId) {
        actionService.saveAction(boardDto, option, userId);
        Board board = boardServiceV1.saveBoardChanges(boardDto, option, userId);
        while(true) {
            if(boardServiceV1.isGameEnd(board.getId())) {
                endGame(board.getId());
                break;
            }
            board = boardServiceV1.setNextAction(board.getId());

            if (board.getActionPos() == -1) {
                boardDto = boardServiceV1.nextPhase(board.getId());
                if (boardDto.getPhaseStatus() != PhaseStatus.SHOWDOWN.ordinal()) {
                    sendUpdateBoardToPlayers(board.getId(), MessageType.NEXT_PHASE_START);
                } else {
                    endGame(board.getId());
                }
            } else {
                sendUpdateBoardToPlayers(board.getId(), MessageType.NEXT_ACTION);
            }

            if(boardServiceV1.isGameEnd(board.getId()) || board.getPhaseStatus() == PhaseStatus.SHOWDOWN) {
                endGame(board.getId());
                break;
            }

            if(boardServiceV1.isActionPlayerConnect(board.getId())) {
                break;
            }

            waitDisconnectPlayer();

            if(boardServiceV1.isActionPlayerConnect(board.getId())) {
                break;
            } else {
                timeOutDisconnectPlayer(board);

                if(boardServiceV1.isGameEnd(board.getId())) {
                    endGame(board.getId());
                    break;
                } else {
                    boardServiceV1.sitOut(boardDto, boardServiceV1.getCurrentActionUserId(board.getId()));
                    sendUpdateBoardToPlayers(board.getId(), MessageType.PLAYER_EXIT);
                }
            }
        }
    }

    private void timeOutDisconnectPlayer(Board board) {
        BoardDto boardDto;
        boardDto = boardServiceV1.getBoard(board.getId());
        actionService.saveAction(boardDto, PlayerAction.FOLD.getActionDetail(), boardServiceV1.getCurrentActionUserId(board.getId()));
        board = boardServiceV1.saveBoardChanges(boardDto, PlayerAction.FOLD.getActionDetail(), boardServiceV1.getCurrentActionUserId(board.getId()));
    }

    public void exitPlayer(BoardDto boardDto, String userId) {
        boardServiceV1.sitOut(boardDto, userId);
        sendUpdateBoardToPlayers(boardDto.getId(), MessageType.PLAYER_EXIT);
        boardDto = boardServiceV1.getBoard(boardDto.getId());
        if (boardDto.getPhaseStatus() >= PhaseStatus.PRE_FLOP.ordinal() && boardDto.getPhaseStatus() <= PhaseStatus.RIVER.ordinal() &&
                boardServiceV1.isGameEnd(boardDto.getId())) {
           endGame(boardDto.getId());
        }
    }

    public void startGame(Long boardId) {
        boardServiceV1.dropDisconnectPlayers(boardId);
        if(boardServiceV1.getBoard(boardId).getTotalPlayer() >= 2) {
            boardServiceV1.startGame(boardId);
            handHistoryService.createHandHistory(boardId);
            sendUpdateBoardToPlayers(boardId, MessageType.GAME_START);
        }
    }
    public void endGame(Long boardId) {

        Board board = boardRepository.findById(boardId).orElseThrow(() -> new CustomException(ErrorCode.BAD_REQUEST));
        boardServiceV1.initPhase(board.getId());
        int resultAnimationCount = 0;

        if (boardServiceV1.isGameEnd(board.getId())) {
            BoardDto boardDto = boardServiceV1.winOnePlayer(board.getId());
            sendUpdateBoardToPlayers(boardDto, MessageType.GAME_END);
            resultAnimationCount = 1;
        } else {
            BoardDto boardDto = boardServiceV1.showDown(board.getId());
            sendUpdateBoardToPlayers(boardDto, MessageType.SHOW_DOWN);
            for (PlayerDto player : boardDto.getPlayers()) {
                GameResultDto gameResult = player.getGameResult();
                if (gameResult.isWinner()) {
                    resultAnimationCount++;
                }
            }
        }
        try {
            Thread.sleep(resultAnimationCount * RESULT_ANIMATION_TIME * 1000);
        } catch (InterruptedException e) {
            e.getStackTrace();
        }

        boardServiceV1.initBoard(board.getId());
        sendUpdateBoardToPlayers(boardId, MessageType.INIT_BOARD);

        try {
            Thread.sleep(1000);
        } catch (InterruptedException e) {
            e.getStackTrace();
        }

        startGame(boardId);
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

    private void sendUpdateBoardToPlayers(Long boardId, MessageType messageType) {
        simpMessagingTemplate.convertAndSend(TOPIC_PREFIX + boardId, new MessageDto(messageType.getDetail(), boardServiceV1.getBoard(boardId)));
    }

    private void sendUpdateBoardToPlayers(BoardDto boardDto, MessageType messageType) {
        simpMessagingTemplate.convertAndSend(TOPIC_PREFIX + boardDto.getId(), new MessageDto(messageType.getDetail(), boardDto));
    }
}
