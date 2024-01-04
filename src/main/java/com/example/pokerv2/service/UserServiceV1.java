package com.example.pokerv2.service;

import com.example.pokerv2.dto.UserDto;
import com.example.pokerv2.error.CustomException;
import com.example.pokerv2.error.ErrorCode;
import com.example.pokerv2.model.Hud;
import com.example.pokerv2.model.User;
import com.example.pokerv2.repository.UserRepository;
import jakarta.servlet.http.HttpServletResponse;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.Optional;

@Service
@RequiredArgsConstructor
public class UserServiceV1 {

    private final UserRepository userRepository;
    private final SessionManager sessionManager;

    public void join(User user){
        Optional<User> findUser = userRepository.findByUserId(user.getUserId());
        if(findUser.isPresent())
            throw new CustomException(ErrorCode.DUPLICATE_USER);

        user.setMoney(10000000);
        user.setHud(new Hud());

        userRepository.save(user);

    }


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
