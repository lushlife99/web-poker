package com.example.pokerv2.model;

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
public class HandHistory {

    @Id @GeneratedValue
    private Long id;

    @OneToOne
    private Board board;

    @OneToMany(mappedBy = "handHistory", fetch = FetchType.LAZY, cascade = CascadeType.ALL) @Builder.Default @OrderBy("actionNo asc")
    private List<Action> actionList = new ArrayList<>();
    @Enumerated(EnumType.ORDINAL)
    private Position btnPosition;
    private int potAmountPf;
    private int potAmountFlop;
    private int potAmountTurn;
    private int potAmountRiver;
    private int communityCard1;
    private int communityCard2;
    private int communityCard3;
    private int communityCard4;
    private int communityCard5;

    @OneToMany(mappedBy = "handHistory", fetch = FetchType.LAZY, cascade = CascadeType.ALL)
    @Builder.Default
    private List<UserHandHistory> userList = new ArrayList<>();

    @ElementCollection
    @OrderColumn(name = "card_order")
    private List<Integer> cardList = new ArrayList<>();
}
