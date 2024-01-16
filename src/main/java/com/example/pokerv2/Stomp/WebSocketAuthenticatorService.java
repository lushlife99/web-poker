package com.example.pokerv2.Stomp;

import com.example.pokerv2.model.User;
import com.example.pokerv2.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.security.authentication.AuthenticationCredentialsNotFoundException;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.AuthenticationException;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.stereotype.Component;

import java.util.Optional;

@Component
@RequiredArgsConstructor
public class WebSocketAuthenticatorService {

    private final UserRepository userRepository;

    // Authentication 인터페이스의 구현 객체
    public UsernamePasswordAuthenticationToken getAuthenticatedOrFail(final String username, final String password) throws AuthenticationException{
        if(username == null){
            throw new AuthenticationCredentialsNotFoundException("username is null");
        }
        if(password == null){
            throw new AuthenticationCredentialsNotFoundException("password is null");
        }
        Optional<User> user = userRepository.findByUserId(username);


        return new UsernamePasswordAuthenticationToken(username, null, user.get().getAuthorities());
        // 인증된 사용자에게 토큰을 생성하려면 principal, credentials, authorities 3가지 리턴
    }

}
