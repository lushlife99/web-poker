package com.example.pokerv2.utils;

import com.example.pokerv2.dto.BoardDto;
import com.example.pokerv2.dto.GameResultDto;
import com.example.pokerv2.dto.PlayerDto;
import com.example.pokerv2.enums.PhaseStatus;
import com.example.pokerv2.enums.PlayerStatus;
import com.example.pokerv2.enums.Position;
import com.example.pokerv2.model.Board;
import com.example.pokerv2.model.Player;
import com.example.pokerv2.model.User;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.ArrayList;
import java.util.List;

import static org.assertj.core.api.Assertions.*;


class PotDistributorUtilsTest {

    private BoardDto boardDto;

    @BeforeEach
    void init() {

        Board board = Board.builder().blind(1000).phaseStatus(PhaseStatus.SHOWDOWN).id(1L).totalPlayer(6).players(new ArrayList<>()).build();

        this.boardDto = new BoardDto(board);

        PlayerDto player1 = new PlayerDto(Player.builder().id(1L).user(User.builder().userId("1").id(1L).build()).board(board).position(Position.BTN).status(PlayerStatus.PLAY).build());
        PlayerDto player2 = new PlayerDto(Player.builder().id(2L).user(User.builder().userId("2").id(2L).build()).board(board).position(Position.CO).status(PlayerStatus.PLAY).build());
        PlayerDto player3 = new PlayerDto(Player.builder().id(3L).user(User.builder().userId("3").id(3L).build()).board(board).position(Position.MP).status(PlayerStatus.PLAY).build());
        PlayerDto player4 = new PlayerDto(Player.builder().id(4L).user(User.builder().userId("4").id(4L).build()).board(board).position(Position.UTG).status(PlayerStatus.PLAY).build());
        PlayerDto player5 = new PlayerDto(Player.builder().id(5L).user(User.builder().userId("5").id(5L).build()).board(board).position(Position.BB).status(PlayerStatus.PLAY).build());
        PlayerDto player6 = new PlayerDto(Player.builder().id(6L).user(User.builder().userId("6").id(6L).build()).board(board).position(Position.SB).status(PlayerStatus.PLAY).build());

        List<PlayerDto> players = this.boardDto.getPlayers();
        players.addAll(List.of(player1, player2, player3, player4, player5, player6));
        player1.setGameResult(GameResultDto.builder().handValue(6).build());
        player2.setGameResult(GameResultDto.builder().handValue(5).build());
        player3.setGameResult(GameResultDto.builder().handValue(4).build());
        player4.setGameResult(GameResultDto.builder().handValue(3).build());
        player5.setGameResult(GameResultDto.builder().handValue(2).build());
        player6.setGameResult(GameResultDto.builder().handValue(1).build());
    }

    @Test
    @DisplayName("한명의 승리자에게 모든 팟이 분배되는지 테스트")
    void distributeToOneWinner() {

        //given
        PlayerDto firstStrongestPlayer = boardDto.getPlayers().get(0);
        PlayerDto secondStrongestPlayer = boardDto.getPlayers().get(1);
        PlayerDto thirdStrongestPlayer = boardDto.getPlayers().get(2);
        PlayerDto fourthStrongestPlayer = boardDto.getPlayers().get(3);
        PlayerDto fifthStrongestPlayer = boardDto.getPlayers().get(4);
        PlayerDto sixthStrongestPlayer = boardDto.getPlayers().get(5);

        int totalCallSize = 1000;

        firstStrongestPlayer.setTotalCallSize(totalCallSize);
        secondStrongestPlayer.setTotalCallSize(totalCallSize);
        thirdStrongestPlayer.setTotalCallSize(totalCallSize);
        fourthStrongestPlayer.setTotalCallSize(totalCallSize);
        fifthStrongestPlayer.setTotalCallSize(totalCallSize);
        sixthStrongestPlayer.setTotalCallSize(totalCallSize);
        boardDto.setPot(totalCallSize * 6);

        //when
        PotDistributorUtils.distribute(boardDto);

        //then
        PlayerDto winPlayer = boardDto.getPlayers().get(0);
        assertThat(winPlayer.getGameResult().isWinner()).isTrue();
        assertThat(winPlayer.getMoney()).isEqualTo(totalCallSize * 5);
    }

    @Test
    @DisplayName("폴드한 플레이어들의 데드머니가 잘 분배되는지 테스트")
    void distributeDeadMoney() {
        //given
        PlayerDto firstStrongestPlayer = boardDto.getPlayers().get(0);
        PlayerDto foldPlayer1 = boardDto.getPlayers().get(1);
        PlayerDto foldPlayer2 = boardDto.getPlayers().get(2);
        PlayerDto foldPlayer3 = boardDto.getPlayers().get(3);
        PlayerDto foldPlayer4 = boardDto.getPlayers().get(4);
        PlayerDto foldPlayer5 = boardDto.getPlayers().get(5);

        int totalCallSize = 1000;

        firstStrongestPlayer.setTotalCallSize(totalCallSize);

        foldPlayer1.setTotalCallSize(totalCallSize);
        foldPlayer1.setStatus(PlayerStatus.FOLD.ordinal());
        foldPlayer1.getGameResult().setHandValue(0);

        foldPlayer2.setTotalCallSize(totalCallSize);
        foldPlayer2.setStatus(PlayerStatus.FOLD.ordinal());
        foldPlayer2.getGameResult().setHandValue(0);

        foldPlayer3.setTotalCallSize(totalCallSize);
        foldPlayer3.setStatus(PlayerStatus.FOLD.ordinal());
        foldPlayer3.getGameResult().setHandValue(0);

        foldPlayer4.setTotalCallSize(totalCallSize);
        foldPlayer4.setStatus(PlayerStatus.FOLD.ordinal());
        foldPlayer4.getGameResult().setHandValue(0);

        foldPlayer5.setTotalCallSize(totalCallSize);
        foldPlayer5.setStatus(PlayerStatus.FOLD.ordinal());
        foldPlayer5.getGameResult().setHandValue(0);

        boardDto.setPot(totalCallSize * 6);

        //when
        PotDistributorUtils.distribute(boardDto);

        //then
        PlayerDto winPlayer = boardDto.getPlayers().get(0);
        assertThat(winPlayer.getGameResult().isWinner()).isTrue();
        assertThat(winPlayer.getMoney()).isEqualTo(totalCallSize * 5);
    }

    @Test
    @DisplayName("다수의 승자가 존재할 경우 사이드팟이 알맞게 분배되는지 테스트")
    void sidePot() {

    }
}