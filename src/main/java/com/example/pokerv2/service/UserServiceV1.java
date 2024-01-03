package com.example.pokerv2.service;

import com.example.pokerv2.dto.UserDto;
import com.example.pokerv2.error.CustomException;
import com.example.pokerv2.error.ErrorCode;
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

    public void join(String userId, String userName, String password){
        Optional<User> findUser = userRepository.findByUserId(userId);
        if(findUser.isPresent())
            throw new CustomException(ErrorCode.DUPLICATE_USER);

        User joinUser = User.builder().userId(userId)
                .userName(userName)
                .password(password).money(10000000).build();

        userRepository.save(joinUser);

    }

    public UserDto login(String userId, String password, HttpServletResponse response) {
        Optional<User> findUser = userRepository.findByUserId(userId);
        if(findUser.isEmpty())
            throw new CustomException(ErrorCode.NOT_EXISTS_USER);

        User user = findUser.get();
        if(!user.getPassword().equals(password))
            throw new CustomException(ErrorCode.MISMATCH_PASSWORD);

        sessionManager.createSession(user, response);
        return new UserDto(user);
    }

}
