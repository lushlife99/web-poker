package com.example.pokerv2.enums;

import lombok.AllArgsConstructor;
import lombok.Getter;

@AllArgsConstructor
@Getter
public enum PlayerAction {
    FOLD("fold"),
    CHECK("check"),
    CALL("call"),
    ALL_IN_CALL("allInCall"),
    RAISE("raise"),
    ALL_IN_RAISE("allInRaise");
    private final String actionDetail;

//    public static PlayerAction valueOf(String actionDetail) {
//        for (PlayerAction action : PlayerAction.values()) {
//            if (action.getActionDetail() == actionDetail) {
//                return action;
//            }
//        }
//        throw new IllegalArgumentException("Invalid actionDetail: " + actionDetail);
//    }

}
