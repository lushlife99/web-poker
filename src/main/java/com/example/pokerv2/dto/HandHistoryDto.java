package com.example.pokerv2.dto;

import com.example.pokerv2.model.Action;
import com.example.pokerv2.model.HandHistory;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class HandHistoryDto {

    private Long id;
    private List<Action> actionList;
    private int actionCount;
    private int potAmountPf;
    private int potAmountFlop;
    private int potAmountTurn;
    private int potAmountRiver;

    public HandHistoryDto(HandHistory handHistory) {
        this.id = handHistory.getId();
        this.actionList = handHistory.getActionList();
        this.actionCount = handHistory.getActionCount();
        this.potAmountPf = handHistory.getPotAmountPf();
        this.potAmountFlop = handHistory.getPotAmountFlop();
        this.potAmountTurn = handHistory.getPotAmountTurn();
        this.potAmountRiver = handHistory.getPotAmountRiver();
    }

}
