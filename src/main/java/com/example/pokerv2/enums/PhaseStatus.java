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

}
