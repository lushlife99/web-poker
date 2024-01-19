package com.example.pokerv2.enums;


import lombok.AllArgsConstructor;
import lombok.Getter;

@AllArgsConstructor
@Getter
public enum MessageType {

    PLAYER_JOIN("PLAYER_JOIN"),
    GAME_START("GAME_START"),
    ERROR("ERROR"),
    NEXT_PHASE_START("NEXT_PHASE_START"),
    NEXT_ACTION("NEXT_ACTION"),
    GAME_RESULT("GAME_RESULT"),
    PLAYER_EXIT("PLAYER_EXIT"),

    ;

    private final String detail;
}