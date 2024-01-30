package com.example.pokerv2.service;

import com.example.pokerv2.enums.Position;
import com.example.pokerv2.error.CustomException;
import com.example.pokerv2.error.ErrorCode;
import com.example.pokerv2.model.*;
import com.example.pokerv2.repository.*;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

@Service
@RequiredArgsConstructor
public class HandHistoryService {

    private final HandHistoryRepository handHistoryRepository;
    private final ActionRepository actionRepository;
    private final BoardRepository boardRepository;
    private final UserRepository userRepository;
    private final UserHandHistoryRepository userHandHistoryRepository;

    @Transactional(propagation = Propagation.REQUIRES_NEW)
    public HandHistory createHandHistory(Long boardId) {
        Board board = boardRepository.findById(boardId).orElseThrow(() -> new CustomException(ErrorCode.BAD_REQUEST));
        HandHistory handHistory = new HandHistory();
        List<UserHandHistory> connectionList = new ArrayList<>();

        for (Player player : board.getPlayers()) {
            UserHandHistory connection = UserHandHistory.builder().user(player.getUser()).handHistory(handHistory)
                    .card1(player.getCard1()).card2(player.getCard2()).build();
            connectionList.add(connection);
        }

        handHistory = HandHistory.builder().board(board).userList(connectionList).btnPosition(Position.getPositionByNumber(board.getBtn()))
                .communityCard1(board.getCommunityCard1()).communityCard2(board.getCommunityCard2()).communityCard3(board.getCommunityCard3()).communityCard4(board.getCommunityCard4()).communityCard5(board.getCommunityCard5()).build();
        handHistoryRepository.save(handHistory);
        userHandHistoryRepository.saveAll(connectionList);
        board.setHandHistory(handHistory);
        return handHistory;
    }

}
