package com.example.pokerv2.dto;

import com.example.pokerv2.model.Hud;
import com.example.pokerv2.model.Player;
import com.example.pokerv2.model.User;
import lombok.Data;


import java.util.ArrayList;
import java.util.List;

@Data
public class UserDto {


    private Long id;
    private String userId;
    private String userName;
    private int money;
    private HudDto hud;
    private List<PlayerDto> playerList;

    public UserDto(User user){
        this.id = user.getId();
        this.userId = user.getUserId();
        this.userName = user.getUserName();
        this.money = user.getMoney();
        this.hud = new HudDto(user.getHud());
        this.playerList = new ArrayList<>();
        for (Player player : user.getPlayerList()) {
            this.playerList.add(new PlayerDto(player));
        }
    }
}
