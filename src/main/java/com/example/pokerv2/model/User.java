package com.example.pokerv2.model;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;

/**
 * 2024/01/02 chan
 *
 * 1. user player 차이점
 *
 * 유저는 한 Board 에서만 플레이 할 수 있는게 아니다.
 * 여러 Board 에서 플레이 할 수 있다.
 * 한 Board 에서 플레이하는 유저를 Player 라고 구분한다.
 * 그러므로 한 user는 여러 Board 에 속하는 하나의 플레이어가 될 수 있다.
 *
 * 2. money
 *
 * money 는 유저가 가진 돈이 저장된다.
 * 게임을 시작할 때 buy-in 하여 player가 되면, money가 차감되고 그만큼의 돈을 player의 bb 필드에 추가된다.
 *
 */
@Builder
@AllArgsConstructor
@NoArgsConstructor
@Data
@Entity
public class User {

    @Id @GeneratedValue
    private Long id;
    private String userId;
    private String userName;
    private String password;
    private int money;
    @OneToOne
    private Hud hud;
    @OneToMany(mappedBy = "user")
    private List<Player> playerList;


}
