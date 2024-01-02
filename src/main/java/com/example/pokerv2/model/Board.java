package com.example.pokerv2.model;

import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.Id;
import jakarta.persistence.ManyToMany;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.ArrayList;
import java.util.List;

@Builder
@AllArgsConstructor
@NoArgsConstructor
@Data
@Entity
public class Board {

    @Id @GeneratedValue
    private Long id;
    private int totalPlayer;
    private int btn = 0;
    private int blind;
    private int pot;
    private int[] communityCardList = new int[5];

    private int bettingPos;
    private int actionPos;
    private int phaseNum;
    private int bettingSize;

    @ManyToMany
    private List<Player> playerList = new ArrayList<>();

}
