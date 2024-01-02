package com.example.pokerv2.model;


import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.Id;
import jakarta.persistence.ManyToOne;
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
    private User user;
    @ManyToOne
    private Board board;

    private double bb;
    private int card1;
    private int card2;
    private int status;
    private int totalCallSize;

}
