package com.example.pokerv2.dto;

import com.example.pokerv2.model.Player;
import lombok.Data;

@Data
public class PlayerDto {

    private Long id;
    private UserDto user;

    private Long boardId;

    private int position;

    private double bb;
    private int card1;
    private int card2;
    private int status;
    private int totalCallSize;

    public PlayerDto(Player player) {
        this.id = player.getId();
        this.user = new UserDto(player.getUser());
        this.boardId = player.getBoard().getId();
        this.position = player.getPosition().ordinal();
        this.bb = player.getBb();
        this.card1 = player.getCard1();
        this.card2 = player.getCard2();
        this.status = player.getStatus();
        this.totalCallSize = player.getTotalCallSize();
    }

}
