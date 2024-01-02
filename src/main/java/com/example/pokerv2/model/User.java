package com.example.pokerv2.model;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;

/**
 * 2024/01/02 jungeun
 *
 * 1. money
 * 유저가 보유한 돈
 *
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