package com.example.pokerv2.service;

import com.example.pokerv2.dto.UserDto;
import com.example.pokerv2.error.CustomException;
import com.example.pokerv2.error.ErrorCode;
import com.example.pokerv2.model.Hud;
import com.example.pokerv2.model.User;
import com.example.pokerv2.repository.HudRepository;
import com.example.pokerv2.repository.UserRepository;
import jakarta.servlet.http.HttpServletResponse;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.Optional;

@Service
@RequiredArgsConstructor
public class UserServiceV1 {

    private final UserRepository userRepository;
    private final SessionManager sessionManager;
    private final HudRepository hudRepository;

    public User join(User user){
        Optional<User> findUser = userRepository.findByUserId(user.getUserId());
        if(findUser.isPresent())
            throw new CustomException(ErrorCode.DUPLICATE_USER);

        User savedUser = userRepository.save(user);

        Hud hud = new Hud();
        hud.setUser(user);
        hudRepository.save(hud);
        savedUser.setHud(hud);
        savedUser.setMoney(10000000);
        userRepository.save(savedUser);
        return user;
    }

    @Transactional
    public UserDto login(User loginUser, HttpServletResponse response) {
        Optional<User> findUser = userRepository.findByUserId(loginUser.getUserId());
        if(findUser.isEmpty())
            throw new CustomException(ErrorCode.NOT_EXISTS_USER);

        User user = findUser.get();
        if(!user.getPassword().equals(loginUser.getPassword()))
            throw new CustomException(ErrorCode.MISMATCH_PASSWORD);

        sessionManager.createSession(user, response);
        return new UserDto(user);
    }


}
