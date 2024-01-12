package com.example.pokerv2.dto;

import com.example.pokerv2.model.User;
import lombok.Data;

@Data
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
