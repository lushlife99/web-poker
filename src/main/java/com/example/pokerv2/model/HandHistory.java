package com.example.pokerv2.model;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.ArrayList;
import java.util.List;

/**
 * 2024/01/02 jungeun
 *
 * 1. actionCount
 * ????????????
 * ------------------------
 * 수정
 * 액션의 갯수
 *
 * 2. potAmountPf
 * 프리 플랍에서의 베팅된 돈의 합계
 *
 * 3. potAmountFlop
 * 플랍에서의 베팅된 돈의 합계
 *
 * 4. potAmountTurn
 * 턴에서의 베팅된 돈의 합계
 *
 * 5. potAmountRiver
 * 리버에서의 베팅된 돈의 합계
 *
 */
@Data
@Builder
@AllArgsConstructor
@NoArgsConstructor
@Entity
public class HandHistory {

    @Id @GeneratedValue
    private Long id;
    @OneToMany(mappedBy = "handHistory") @Builder.Default @OrderBy("actionNo asc")
    private List<Action> actionList = new ArrayList<>();
    private int actionCount;
    private int potAmountPf;
    private int potAmountFlop;
    private int potAmountTurn;
    private int potAmountRiver;

}