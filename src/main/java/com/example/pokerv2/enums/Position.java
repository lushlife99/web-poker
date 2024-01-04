package com.example.pokerv2.enums;

import lombok.AllArgsConstructor;
import lombok.Getter;

@Getter
@AllArgsConstructor
public enum Position {
    UTG(0),
    MP(1),
    CO(2),
    BTN(3),
    SB(4),
    BB(5);

    private final int posNum;

    public static Position getPositionByNumber(int number) {
        for (Position position : Position.values()) {
            if (position.posNum == number) {
                return position;
            }
        }
        throw new IllegalArgumentException("Invalid Position number: " + number);
    }
}
