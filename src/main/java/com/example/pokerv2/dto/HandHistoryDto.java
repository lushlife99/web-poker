package com.example.pokerv2.dto;

import com.example.pokerv2.enums.Position;
import com.example.pokerv2.model.Action;
import com.example.pokerv2.model.HandHistory;
import com.example.pokerv2.model.UserHandHistory;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.ArrayList;
import java.util.List;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class HandHistoryDto {

    private Long id;
    private List<ActionDto> actionList;
    private int potAmountPf;
    private int potAmountFlop;
    private int potAmountTurn;
    private int potAmountRiver;
    private Position btnPosition;
    private int communityCard1;
    private int communityCard2;
    private int communityCard3;
    private int communityCard4;
    private int communityCard5;
    private List<UserDto> userList = new ArrayList<>();
    private List<Integer> posList = new ArrayList<>();
    private List<Integer> cardList = new ArrayList<>();

    public HandHistoryDto(HandHistory handHistory) {
        try {
            this.id = handHistory.getId();
            this.actionList = new ArrayList<>();
            for (Action action : handHistory.getActionList()) {
                actionList.add(new ActionDto(action));
            }
            this.potAmountPf = handHistory.getPotAmountPf();
            this.potAmountFlop = handHistory.getPotAmountFlop();
            this.potAmountTurn = handHistory.getPotAmountTurn();
            this.potAmountRiver = handHistory.getPotAmountRiver();
            this.communityCard1 = handHistory.getCommunityCard1();
            this.communityCard2 = handHistory.getCommunityCard2();
            this.communityCard3 = handHistory.getCommunityCard3();
            this.communityCard4 = handHistory.getCommunityCard4();
            this.communityCard5 = handHistory.getCommunityCard5();
            userList = new ArrayList<>();
            for (UserHandHistory userHandHistory : handHistory.getUserList()) {
                userList.add(new UserDto(userHandHistory.getUser()));
            }
            cardList = handHistory.getCardList();
        } catch (Exception e) {
            System.out.println("HandHistoryDto.HandHistoryDto");
            System.out.println(e.getMessage());
        }
    }

}
