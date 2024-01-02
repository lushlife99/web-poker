package com.example.pokerv2.model;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;

@Builder
@AllArgsConstructor
@NoArgsConstructor
@Data
@Entity
public class User {

    @Id @GeneratedValue
    private Long id;
    private String userId;
    private String userName;
    private String password;
    private int money;
    @OneToOne
    private Hud hud;
    @OneToMany
    private List<Player> playerList;


}
