package com.example.pokerv2.service;

import com.example.pokerv2.dto.HandHistoryDto;
import com.example.pokerv2.enums.Position;
import com.example.pokerv2.error.CustomException;
import com.example.pokerv2.error.ErrorCode;
import com.example.pokerv2.model.*;
import com.example.pokerv2.repository.*;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Isolation;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;

import java.security.Principal;
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

    @Transactional(propagation = Propagation.REQUIRES_NEW, isolation = Isolation.READ_COMMITTED)
    public HandHistory createHandHistory(Long boardId) {
        Board board = boardRepository.findById(boardId).orElseThrow(() -> new CustomException(ErrorCode.BAD_REQUEST));

        List<UserHandHistory> connectionList = new ArrayList<>();
        List<Integer> cardList = new ArrayList<>();


        HandHistory handHistory = HandHistory.builder().board(board).btnPosition(Position.getPositionByNumber(board.getBtn())).cardList(cardList)
                .communityCard1(board.getCommunityCard1()).communityCard2(board.getCommunityCard2()).communityCard3(board.getCommunityCard3()).communityCard4(board.getCommunityCard4()).communityCard5(board.getCommunityCard5()).build();

        for (Player player : board.getPlayers()) {
            UserHandHistory connection = UserHandHistory.builder().user(player.getUser()).handHistory(handHistory).build();
            cardList.add(player.getCard1());
            cardList.add(player.getCard2());
            connectionList.add(connection);
        }

        handHistory.setUserList(connectionList);

        handHistoryRepository.save(handHistory);
        userHandHistoryRepository.saveAll(connectionList);
        board.setHandHistory(handHistory);
        return handHistory;
    }

    @Transactional(propagation = Propagation.REQUIRES_NEW, isolation = Isolation.READ_COMMITTED)
    public void disconnect(Long boardId) {
        Board board = boardRepository.findById(boardId).orElseThrow(() -> new CustomException(ErrorCode.BAD_REQUEST));
        HandHistory handHistory = board.getHandHistory();
        handHistory.setBoard(null);
        board.setHandHistory(null);
    }

    public List<HandHistoryDto> get(String userId) {
        User user = userRepository.findByUserId(userId).orElseThrow(() -> new CustomException(ErrorCode.BAD_REQUEST));
        List<UserHandHistory> connectionList = user.getHandHistoryList();
        List<HandHistoryDto> handHistoryList = new ArrayList<>();

        for (UserHandHistory userHandHistory : connectionList) {
            HandHistory handHistory = userHandHistory.getHandHistory();
        }

        return handHistoryList;
    }

}
