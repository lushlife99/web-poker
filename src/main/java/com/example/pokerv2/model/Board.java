package com.example.pokerv2.model;

import com.example.pokerv2.dto.BoardDto;
import com.example.pokerv2.enums.PhaseStatus;
import com.example.pokerv2.enums.Position;
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
    @Builder.Default
    private int btn = Position.BTN.getPosNum();
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

    @OneToMany(mappedBy = "board", cascade = CascadeType.ALL)
    @OrderBy("position asc")
    @Builder.Default
    private List<Player> players = new ArrayList<>();

    public void changeBoardStatus(BoardDto boardDto){
        this.totalPlayer = boardDto.getTotalPlayer();
        this.blind = boardDto.getBlind();
        this.pot = boardDto.getPot();
        this.bettingPos = boardDto.getBettingPos();
        this.actionPos = boardDto.getActionPos();
        this.phaseStatus = PhaseStatus.valueOf(boardDto.getPhaseStatus());
        this.bettingSize = boardDto.getBettingSize();
        this.communityCard1 = boardDto.getCommunityCard1();
        this.communityCard2 = boardDto.getCommunityCard2();
        this.communityCard3 = boardDto.getCommunityCard3();
        this.communityCard4 = boardDto.getCommunityCard4();
        this.communityCard5 = boardDto.getCommunityCard5();

    }

}