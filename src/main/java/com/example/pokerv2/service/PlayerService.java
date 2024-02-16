package com.example.pokerv2.service;

import com.example.pokerv2.enums.PhaseStatus;
import com.example.pokerv2.enums.PlayerStatus;
import com.example.pokerv2.error.CustomException;
import com.example.pokerv2.error.ErrorCode;
import com.example.pokerv2.model.Board;
import com.example.pokerv2.model.Player;
import com.example.pokerv2.model.User;
import com.example.pokerv2.repository.PlayerRepository;
import com.example.pokerv2.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.security.Principal;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

@Slf4j
@Service
@RequiredArgsConstructor
public class PlayerService {

    private final UserRepository userRepository;
    private final PlayerRepository playerRepository;

    @Transactional
    public void setDisconnect(Principal principal) {
        Optional<User> disconnectedUser = userRepository.findByUserId(principal.getName());
        if (disconnectedUser.isPresent()) {
            User user = disconnectedUser.get();
            List<Player> playerList = user.getPlayerList();
            List<Player> exitList = new ArrayList<>();
            for (Player player : playerList) {
                Board board = player.getBoard();
                if(board.getTotalPlayer() == 1) {
                    exitList.add(player);
                }
                if (player.getStatus() == PlayerStatus.FOLD) {
                    player.setStatus(PlayerStatus.DISCONNECT_FOLD);
                } else if (player.getStatus() == PlayerStatus.PLAY) {
                    player.setStatus(PlayerStatus.DISCONNECT_PLAYED);
                } else if (player.getStatus() == PlayerStatus.ALL_IN) {
                    player.setStatus(PlayerStatus.DISCONNECT_ALL_IN);
                }
            }
            playerRepository.deleteAll(exitList);
        }
    }

    @Transactional
    public void setConnect(Principal principal) {
        User user = userRepository.findByUserId(principal.getName()).orElseThrow(() -> new CustomException(ErrorCode.BAD_REQUEST));
        List<Player> playerList = user.getPlayerList();

        for (Player player : playerList) {
            Board board = player.getBoard();
            if(board.getPhaseStatus().ordinal() >= PhaseStatus.PRE_FLOP.ordinal() && board.getPhaseStatus().ordinal() <= PhaseStatus.RIVER.ordinal()) {
                if (player.getStatus() == PlayerStatus.DISCONNECT_ALL_IN) {
                    player.setStatus(PlayerStatus.ALL_IN);
                } else if (player.getStatus() == PlayerStatus.DISCONNECT_PLAYED) {
                    player.setStatus(PlayerStatus.PLAY);
                } else if (player.getStatus() == PlayerStatus.DISCONNECT_FOLD) {
                    player.setStatus(PlayerStatus.FOLD);
                }
            }
        }

        playerRepository.saveAll(playerList);
    }

}
