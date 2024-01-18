package com.example.pokerv2.enums;

import lombok.AllArgsConstructor;
import lombok.Getter;

@AllArgsConstructor
@Getter
public enum PlayerStatus {

    FOLD(0),
    ALL_IN(1),
    PLAY(2);

    private final int statusNum;

    public static PlayerStatus valueOf(int statusNum) {
        for (PlayerStatus status : PlayerStatus.values()) {
            if (status.getStatusNum() == statusNum) {
                return status;
            }
        }
        throw new IllegalArgumentException("Invalid statusNum: " + statusNum);
    }
}
