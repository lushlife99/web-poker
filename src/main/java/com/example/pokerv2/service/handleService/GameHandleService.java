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
import com.example.pokerv2.service.ActionService;
import com.example.pokerv2.service.BoardServiceV1;
import com.example.pokerv2.service.HandHistoryService;
import com.example.pokerv2.service.HudService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.messaging.simp.SimpMessagingTemplate;
import org.springframework.stereotype.Service;

import java.security.Principal;

@Slf4j
@Service
@RequiredArgsConstructor
public class GameHandleService {

    private final BoardServiceV1 boardServiceV1;
    private final ActionService actionService;
    private final HandHistoryService handHistoryService;
    private final HudService hudService;
    private final SimpMessagingTemplate simpMessagingTemplate;

    private final static String TOPIC_PREFIX = "/topic/board/";

    private final static int ACTION_TIME = 10;
    private final static int RESULT_ANIMATION_TIME = 5;

    public BoardDto join(int requestBb, Principal principal) {

        BoardDto boardDto = boardServiceV1.join(requestBb, principal);
        sendUpdateBoardToPlayers(boardDto, MessageType.PLAYER_JOIN);

        if (boardDto.getPhaseStatus() == PhaseStatus.WAITING.ordinal() && boardDto.getTotalPlayer() >= 2) {
            boardDto = startGame(boardDto.getId());
        }

        return boardDto;
    }

    public void action(BoardDto boardDto, String action, String userId) {
        hudService.addCountBeforeSaveAction(boardDto, action);
        actionService.saveAction(boardDto, action, userId);
        Board board = boardServiceV1.saveBoardChanges(boardDto, action, userId);
        while (true) {
            if (boardServiceV1.isGameEnd(board.getId())) {
                endGame(board.getId());
                break;
            }
            board = boardServiceV1.setNextAction(board.getId());

            if (board.getActionPos() == -1) {
                hudService.addCountBeforePhaseChange(board.getId());
                handHistoryService.savePhaseHistory(board.getId());
                boardDto = boardServiceV1.nextPhase(board.getId());
                hudService.addCountAfterPhaseChange(board.getId());
                if (boardDto.getPhaseStatus() != PhaseStatus.SHOWDOWN.ordinal()) {
                    sendUpdateBoardToPlayers(board.getId(), MessageType.NEXT_PHASE_START);
                } else {
                    endGame(board.getId());
                }
            } else {
                sendUpdateBoardToPlayers(board.getId(), MessageType.NEXT_ACTION);
            }

            if (boardServiceV1.isGameEnd(board.getId()) || board.getPhaseStatus() == PhaseStatus.SHOWDOWN) {
                endGame(board.getId());
                break;
            }

            if (boardServiceV1.isActionPlayerConnect(board.getId())) {
                break;
            }

            waitDisconnectPlayer();

            if (boardServiceV1.isActionPlayerConnect(board.getId())) {
                break;
            } else {
                timeOutDisconnectPlayer(board);

                if (boardServiceV1.isGameEnd(board.getId())) {
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

    public BoardDto startGame(Long boardId) {
        boardServiceV1.dropDisconnectPlayers(boardId);
        BoardDto board = boardServiceV1.getBoard(boardId);
        if (board.getTotalPlayer() >= 2) {
            boardServiceV1.startGame(boardId);
            sendUpdateBoardToPlayers(boardId, MessageType.GAME_START);
            handHistoryService.createHandHistory(boardId);
            actionService.saveAnteAction(boardId);
            hudService.addCountAfterPhaseChange(boardId);
        }

        return boardServiceV1.getBoard(boardId);
    }

    public void endGame(Long boardId) {

        boardServiceV1.initPhase(boardId);
        int resultAnimationCount = 0;
        BoardDto boardDto;

        if (boardServiceV1.isGameEnd(boardId)) {
            boardDto = boardServiceV1.winOnePlayer(boardId);
            sendUpdateBoardToPlayers(boardDto, MessageType.GAME_END);
            resultAnimationCount = 1;
        } else {
            boardDto = boardServiceV1.showDown(boardId);
            sendUpdateBoardToPlayers(boardDto, MessageType.SHOW_DOWN);
            hudService.addCountAfterShowDown(boardDto);
            for (PlayerDto player : boardDto.getPlayers()) {
                GameResultDto gameResult = player.getGameResult();
                if (gameResult.isWinner()) {
                    resultAnimationCount++;
                }
            }
        }

        handHistoryService.end(boardDto);

        try {
            Thread.sleep(resultAnimationCount * RESULT_ANIMATION_TIME * 1000);
        } catch (InterruptedException e) {
            e.getStackTrace();
        }

        if (boardDto.getGameSeq() != boardServiceV1.getBoard(boardId).getGameSeq()) {
            return;
        }

        boardServiceV1.initBoard(boardId);
        sendUpdateBoardToPlayers(boardId, MessageType.INIT_BOARD);

        try {
            Thread.sleep(1000);
        } catch (InterruptedException e) {
            e.getStackTrace();
        }

        if (boardDto.getGameSeq() != boardServiceV1.getBoard(boardId).getGameSeq()) {
            return;
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

    private void sendUpdateBoardToPlayers(Long boardId, MessageType messageType) {
        simpMessagingTemplate.convertAndSend(TOPIC_PREFIX + boardId, new MessageDto(messageType.getDetail(), boardServiceV1.getBoard(boardId)));
    }

    private void sendUpdateBoardToPlayers(BoardDto boardDto, MessageType messageType) {
        simpMessagingTemplate.convertAndSend(TOPIC_PREFIX + boardDto.getId(), new MessageDto(messageType.getDetail(), boardDto));
    }
}
