package com.example.pokerv2.enums;

import lombok.AllArgsConstructor;
import lombok.Getter;

@AllArgsConstructor
@Getter
public enum PlayerStatus {

    FOLD(0),
    DISCONNECT_PLAYED(1),
    DISCONNECT_ALL_IN(2),
    ALL_IN(3),
    PLAY(4);

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
