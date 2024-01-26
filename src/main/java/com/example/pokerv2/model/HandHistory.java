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
public class HandHistory {

    @Id @GeneratedValue
    private Long id;
    @OneToMany(mappedBy = "handHistory", cascade = CascadeType.ALL) @Builder.Default @OrderBy("actionNo asc")
    private List<Action> actionList = new ArrayList<>();
    private int actionCount;
    private int potAmountPf;
    private int potAmountFlop;
    private int potAmountTurn;
    private int potAmountRiver;

}
