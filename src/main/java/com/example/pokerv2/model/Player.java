package com.example.pokerv2.model;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * 2024/01/02 jungeun
 *
 * 1. user
 * 유저 정보 가져옴
 *
 * 2. bb
 * ????????????
 *
 * 3. card1, 2
 * 플레이어가 가지고 있는 카드 정보
 *
 * 4. status
 * *************
 * 여기 혹시 PlayStatus를 사용 안하고 int를 쓰나요?
 *
 * 5. totalCallSize
 * ????????????
 *
 */

@Builder
@AllArgsConstructor
@NoArgsConstructor
@Data
@Entity
public class Player {

    @Id @GeneratedValue
    private Long id;

    @ManyToOne
    @JoinColumn(name = "user_id")
    private User user;

    @ManyToOne
    @JoinColumn(name = "board_id")
    private Board board;

    private int position;

    private double bb;
    private int card1;
    private int card2;
    private int status;
    private int totalCallSize;


}