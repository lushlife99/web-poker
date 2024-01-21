package com.example.pokerv2.utils;

import com.example.pokerv2.dto.BoardDto;
import com.example.pokerv2.dto.GameResultDto;
import com.example.pokerv2.dto.PlayerDto;
import com.example.pokerv2.error.CustomException;
import com.example.pokerv2.error.ErrorCode;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

import java.util.Comparator;
import java.util.List;
import java.util.stream.Collectors;

@Slf4j
@Component
public class PotDistributorUtils {

    /**
     * 24/01/21 chan
     *
     * 쇼다운 시 승자에 따라 팟을 분배하는 유틸 클래스
     *
     */
    private static final Comparator<PlayerDto> HAND_VALUE_COMPARATOR = Comparator.comparingLong(p -> p.getGameResult().getHandValue());

    private PotDistributorUtils() {
        throw new CustomException(ErrorCode.INTERNAL_SERVER_ERROR);
    }

    public static void distribute(BoardDto boardDto) {
        List<PlayerDto> players = boardDto.getPlayers();
        List<PlayerDto> sortedPlayers = players.stream()
                .sorted(HAND_VALUE_COMPARATOR)
                .collect(Collectors.toList());

        for(int i = 0; i < boardDto.getTotalPlayer(); i++) {
            PlayerDto winPlayer = sortedPlayers.get(i);
            int wpTotalCallSize = winPlayer.getTotalCallSize();
            for(int j = i+1; j < boardDto.getTotalPlayer(); j++) {
                PlayerDto losePlayer = sortedPlayers.get(j);
                int lpTotalCallSize = losePlayer.getTotalCallSize();
                int takePotAmount = 0;
                if(wpTotalCallSize <= lpTotalCallSize) {
                    takePotAmount = wpTotalCallSize;
                }
                else {
                    takePotAmount = lpTotalCallSize;
                }

                winPlayer.setMoney(winPlayer.getMoney() + takePotAmount);
                losePlayer.setTotalCallSize(losePlayer.getTotalCallSize() - takePotAmount);
                boardDto.setPot(boardDto.getPot() - takePotAmount);
                GameResultDto gameResult = winPlayer.getGameResult();
                gameResult.setEarnedMoney(gameResult.getEarnedMoney() + takePotAmount);
            }

            if(boardDto.getPot() == 0) {
                boardDto.setPlayers(sortedPlayers);
                break;
            }
        }
    }
}
