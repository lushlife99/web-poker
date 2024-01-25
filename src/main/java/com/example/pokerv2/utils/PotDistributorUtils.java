package com.example.pokerv2.utils;

import com.example.pokerv2.dto.BoardDto;
import com.example.pokerv2.dto.GameResultDto;
import com.example.pokerv2.dto.PlayerDto;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

import java.util.*;
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

    private static final Comparator<PlayerDto> HAND_VALUE_COMPARATOR = Comparator
            .comparingLong(p -> p.getGameResult().getHandValue());

    private static final Comparator<PlayerDto> TOTAL_CALL_SIZE_COMPARATOR = Comparator
            .comparingInt(p -> p.getTotalCallSize());

    private PotDistributorUtils() {}

    public static void distribute(BoardDto boardDto) {
        List<PlayerDto> players = boardDto.getPlayers();
        players.sort(HAND_VALUE_COMPARATOR.reversed());

        Map<Long, Integer> handStrongCount = new HashMap<>();

        for(int i = 0; i < players.size(); i++) {
            GameResultDto gameResult = players.get(i).getGameResult();
            if(handStrongCount.containsKey(gameResult.getHandValue())){
                handStrongCount.replace(gameResult.getHandValue(), handStrongCount.get(gameResult.getHandValue()) + 1);
            } else {
                handStrongCount.put(gameResult.getHandValue(), 1);
            }
        }

        for(int i = 0; i < boardDto.getTotalPlayer()-1; i++) {
            PlayerDto winPlayer = players.get(i);
            Integer sameHandPlayerSize = handStrongCount.get(winPlayer.getGameResult().getHandValue());
            if(sameHandPlayerSize == 1) {
                distributeOnePlayer(boardDto, i);
            } else {
                distributeDrawPlayers(boardDto, i, winPlayer.getGameResult().getHandValue());
                i = i + sameHandPlayerSize - 1;
            }

            if(boardDto.getPot() < boardDto.getTotalPlayer()) {
                break;
            }

        }
    }

    private static void distributeOnePlayer(BoardDto boardDto, int winPlayerIdx) {
        List<PlayerDto> players = boardDto.getPlayers();
        PlayerDto winPlayer = players.get(winPlayerIdx);
        int wpTotalCallSize = winPlayer.getTotalCallSize();
        for(int j = winPlayerIdx +1; j < boardDto.getTotalPlayer(); j++) {
            PlayerDto losePlayer = players.get(j);
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

        if (winPlayer.getGameResult().getEarnedMoney() != 0){
            winPlayer.getGameResult().setWinner(true);
        }
    }

    private static void distributeDrawPlayers(BoardDto boardDto, int firstPlayerIdx, long handValue) {
        List<PlayerDto> winPlayerList = new ArrayList<>();

        List<PlayerDto> players = boardDto.getPlayers();
        for(int i = firstPlayerIdx; i < players.size(); i++){
            PlayerDto playerDto = players.get(i);
            if(playerDto.getGameResult().getHandValue() == handValue){
                winPlayerList.add(playerDto);
            }
        }
        System.out.println(firstPlayerIdx);
        System.out.println(winPlayerList.size());
        winPlayerList.sort(TOTAL_CALL_SIZE_COMPARATOR);
        for(int i = firstPlayerIdx + winPlayerList.size(); i < players.size(); i++) {
            PlayerDto losePlayer = players.get(i);
            System.out.println("losePlayer = " + losePlayer);
            for(int j = 0; j < winPlayerList.size(); j++) {
                PlayerDto winPlayer = winPlayerList.get(j);;
                GameResultDto wpGameResult = winPlayer.getGameResult();
                int takePotAmount;

                if(winPlayer.getTotalCallSize() <= losePlayer.getTotalCallSize() / (winPlayerList.size() - j)) {
                    takePotAmount = winPlayer.getTotalCallSize();
                    System.out.println(losePlayer.getUserId() + " to " + winPlayer.getUserId() + " " + takePotAmount);
                    winPlayer.setMoney(winPlayer.getMoney() + takePotAmount);
                    losePlayer.setTotalCallSize(losePlayer.getTotalCallSize() - takePotAmount);
                    boardDto.setPot(boardDto.getPot() - takePotAmount);
                    wpGameResult.setEarnedMoney(wpGameResult.getEarnedMoney() + takePotAmount);
                }

                else {
                    takePotAmount = losePlayer.getTotalCallSize() / (winPlayerList.size() - j);
                    for(int k = j; k < winPlayerList.size(); k++) {
                        PlayerDto drawPlayer = winPlayerList.get(k);
                        System.out.println(losePlayer.getUserId() + "to " + drawPlayer.getUserId() + " " + takePotAmount);
                        drawPlayer.setMoney(drawPlayer.getMoney() + takePotAmount);
                        boardDto.setPot(boardDto.getPot() - takePotAmount);
                        drawPlayer.getGameResult().setEarnedMoney(drawPlayer.getGameResult().getEarnedMoney() + takePotAmount);
                    }
                    break;
                }

            }
        }

        for(int i = 0; i < winPlayerList.size(); i++){
            PlayerDto winPlayer = winPlayerList.get(i);
            GameResultDto wpGameResult = winPlayer.getGameResult();
            if(wpGameResult.getEarnedMoney() > 0) {
                wpGameResult.setWinner(true);
            }
        }
    }
}
