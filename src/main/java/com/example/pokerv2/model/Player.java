package com.example.pokerv2.model;

import com.example.pokerv2.dto.PlayerDto;
import com.example.pokerv2.enums.PlayerStatus;
import com.example.pokerv2.enums.Position;
import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Builder
@AllArgsConstructor
@NoArgsConstructor
@Data
@Entity
public class Player {

    @Id @GeneratedValue
    private Long id;

    @ManyToOne
    @JoinColumn(name = "user_id")
    private User user;

    @ManyToOne
    @JoinColumn(name = "board_id")
    private Board board;

    private Position position;

    private double bb;
    private int card1;
    private int card2;
    private PlayerStatus status;
    private int totalCallSize;
    private int phaseCallSize;

    public void changePlayerStatus(PlayerDto playerDto) {
        this.bb = playerDto.getBb();
        this.status = PlayerStatus.valueOf(playerDto.getStatus());
        this.phaseCallSize = playerDto.getPhaseCallSize();
    }
}
