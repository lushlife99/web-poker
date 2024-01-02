package com.example.pokerv2.model;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.ArrayList;
import java.util.List;

@Data
@Builder
@AllArgsConstructor
@NoArgsConstructor
@Entity
public class Board {

    @Id @GeneratedValue
    private Long id;
    private int totalPlayer;
    @Builder.Default
    private int btn = 0;
    private int blind;
    private int pot;
    private int bettingPos;
    private int actionPos;
    private int phaseNum;
    private int bettingSize;

    private int communityCard1;
    private int communityCard2;
    private int communityCard3;
    private int communityCard4;
    private int communityCard5;

    @OneToMany(mappedBy = "board")
    @OrderBy("position asc")
    @Builder.Default
    private List<Player> players = new ArrayList<>();

}