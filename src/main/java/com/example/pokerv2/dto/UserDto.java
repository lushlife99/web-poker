package com.example.pokerv2.dto;

import com.example.pokerv2.model.Hud;
import com.example.pokerv2.model.Player;
import com.example.pokerv2.model.User;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;


import java.util.ArrayList;
import java.util.List;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class UserDto {


    private Long id;
    private String userId;
    private String userName;
    private int money;
    private HudDto hud;

    public UserDto(User user){
        this.id = user.getId();
        this.userId = user.getUserId();
        this.userName = user.getUsername();
        this.money = user.getMoney();
        this.hud = new HudDto(user.getHud());
    }
}
