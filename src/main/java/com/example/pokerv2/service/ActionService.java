package com.example.pokerv2.service;

import com.example.pokerv2.dto.BoardDto;
import com.example.pokerv2.enums.PlayerAction;
import com.example.pokerv2.error.CustomException;
import com.example.pokerv2.error.ErrorCode;
import com.example.pokerv2.model.*;
import com.example.pokerv2.repository.ActionRepository;
import com.example.pokerv2.repository.BoardRepository;
import com.example.pokerv2.repository.HandHistoryRepository;
import com.example.pokerv2.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;

import java.security.Principal;
import java.util.List;

@Slf4j
@Service
@RequiredArgsConstructor
public class ActionService {

    private final ActionRepository actionRepository;
    private final BoardRepository boardRepository;
    private final UserRepository userRepository;
    private final HandHistoryRepository handHistoryRepository;
    private static final String BB = "bb";

    @Transactional(propagation = Propagation.REQUIRES_NEW)
    public void saveAction(BoardDto boardDto, String actOption, String userId) {
        Board board = boardRepository.findById(boardDto.getId()).orElseThrow(() -> new CustomException(ErrorCode.BAD_REQUEST));
        User actionUser = userRepository.findByUserId(userId).orElseThrow(() -> new CustomException(ErrorCode.BAD_REQUEST));
        HandHistory handHistory = board.getHandHistory();
        String actionDetail = "";
        Player actionPlayer = null;
        for (Player player : board.getPlayers()) {
            if(player.getUser().getId() == actionUser.getId()) {
                actionPlayer = player;
            }
        }

        System.out.println(actOption);

        if(actOption.equals(PlayerAction.FOLD.getActionDetail())) {
            actionDetail += PlayerAction.FOLD.getActionDetail();
        } else if(actOption.equals(PlayerAction.CALL.getActionDetail()) || actOption.equals(PlayerAction.ALL_IN_CALL.getActionDetail())) {
            actionDetail += PlayerAction.CALL.getActionDetail() + " " + actionPlayer.getPhaseCallSize() + BB;
        } else if(actOption.equals(PlayerAction.RAISE.getActionDetail()) || actOption.equals(PlayerAction.ALL_IN_RAISE.getActionDetail())) {
            actionDetail += PlayerAction.RAISE.getActionDetail() + " " + actionPlayer.getPhaseCallSize() + BB;
        }

        Action action = Action.builder()
                .actionNo(handHistory.getActionList().size()).handHistory(handHistory).userId(actionUser.getId())
                .position(actionPlayer.getPosition()).phaseStatus(board.getPhaseStatus()).detail(actionDetail).build();

        actionRepository.save(action);
    }

    private int getPlayerIdxByPos(Board board, int posNum) {
        List<Player> players = board.getPlayers();

        for (int i = 0; i < board.getTotalPlayer(); i++) {
            Player player = players.get(i);
            if (player.getPosition().getPosNum() == posNum)
                return i;
        }

        return -1;
    }
}
