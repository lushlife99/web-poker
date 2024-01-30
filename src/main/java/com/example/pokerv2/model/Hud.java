package com.example.pokerv2.model;

import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.Id;
import jakarta.persistence.OneToOne;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Builder
@AllArgsConstructor
@NoArgsConstructor
@Data
@Entity
public class Hud {
    /**
     *
     * vpip : Voluntarily Puts Money In Pot
     * 자발적으로 팟에 돈을 넣은 횟수를 표시하는 프리플랍 통계
     *
     * pfr : Pre Flop Raise
     * 프리플랍에서 레이즈 한 횟수를 표시
     *
     * cBet : Continuation Bet
     * 프리플랍 레이저가 플랍에서 벳하는 정도
     *
     * threeBet : 첫번째 레이즈에 한번 더 레이즈를 하는게 3Bet, 3Bet에 한번 더 레이즈를 하는게 4Bet.
     * 3bet 이상 하는 정도
     * 3bet-count / Total Opportunities to Call or Raise After an Initial Bet
     *
     * wtsd : Went To Show Down
     * 쇼다운까지 가는 정도
     * wtsd / wtf
     *
     * wsd : Won at Show Down
     * 쇼다운에서 이기는 정도
     * (wsd / wtsd)

     */

    @Id @GeneratedValue
    private Long id;
    @OneToOne
    private User user;
    private int vpip;
    private int pfr;
    private int cBet;
    private int threeBet;
    private int wtsd;
    private int wsd;
    private int totalHands;
    private int pfAggressiveCnt;
    private int wtf;
}
