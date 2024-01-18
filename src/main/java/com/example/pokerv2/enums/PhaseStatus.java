package com.example.pokerv2.enums;

import lombok.AllArgsConstructor;
import lombok.Getter;

@AllArgsConstructor
@Getter
public enum PhaseStatus {

    WAITING(0),
    PRE_FLOP(1),
    FLOP(2),
    TURN(3),
    RIVER(4),
    SHOWDOWN(5);

    private final int phaseNum;

    public static PhaseStatus valueOf(int phaseNum) {
        for (PhaseStatus status : PhaseStatus.values()) {
            if (status.getPhaseNum() == phaseNum) {
                return status;
            }
        }
        throw new IllegalArgumentException("Invalid phaseNum: " + phaseNum);
    }
}