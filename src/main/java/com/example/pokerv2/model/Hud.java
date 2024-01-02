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

    @Id @GeneratedValue
    private Long id;
    @OneToOne
    private User user;
    private int totalHands;
    private int vpip;
    private int pfr;
    private int cBet;
    private int threeBet;
    private int wtsd;
    private int wsd;


}
