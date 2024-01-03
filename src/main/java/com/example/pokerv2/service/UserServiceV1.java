package com.example.pokerv2.service;

import com.example.pokerv2.error.CustomException;
import com.example.pokerv2.error.ErrorCode;
import com.example.pokerv2.model.User;
import com.example.pokerv2.repository.UserRepository;
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


}
