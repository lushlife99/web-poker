package com.example.pokerv2.model;

import com.example.pokerv2.dto.BoardDto;
import com.example.pokerv2.enums.PhaseStatus;
import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;
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
    private Long gameSeq;
    private int btn;
    private int totalPlayer;
    private int blind;
    private int pot;
    private int bettingPos;
    private int actionPos;

    @Enumerated(EnumType.ORDINAL)
    @Builder.Default
    private PhaseStatus phaseStatus = PhaseStatus.WAITING;
    private int bettingSize;

    private int communityCard1;
    private int communityCard2;
    private int communityCard3;
    private int communityCard4;
    private int communityCard5;

    private LocalDateTime lastActionTime;

    @OneToMany(mappedBy = "board", fetch = FetchType.LAZY, cascade = CascadeType.ALL)
    @OrderBy("position asc")
    @Builder.Default
    private List<Player> players = new ArrayList<>();

    @ElementCollection(fetch = FetchType.LAZY)
    @Builder.Default
    private List<Integer> totalCallSize = new ArrayList<>(List.of(0, 0, 0, 0, 0, 0));

}