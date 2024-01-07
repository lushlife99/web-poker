package com.example.pokerv2.dto;

import com.example.pokerv2.model.Player;
import lombok.Data;

@Data
public class PlayerDto {
    private Long id;
    private Long userId;
    private Long boardId;
    private int position;
    private double bb;
    private int card1;
    private int card2;
    private int status;
    private int totalCallSize;
    private int phaseCallSize;

    public PlayerDto(Player player) {
        this.id = player.getId();
        this.userId = player.getUser().getId();
        this.boardId = player.getBoard().getId();
        this.position = player.getPosition().ordinal();
        this.bb = player.getBb();
        this.card1 = player.getCard1();
        this.card2 = player.getCard2();
        this.status = player.getStatus().ordinal();
        this.totalCallSize = player.getTotalCallSize();
        this.phaseCallSize = player.getPhaseCallSize();

    }
}
