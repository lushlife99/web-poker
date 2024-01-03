package com.example.pokerv2.service;

import com.example.pokerv2.model.User;
import jakarta.servlet.http.Cookie;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.stereotype.Component;

import java.util.Arrays;
import java.util.Map;
import java.util.Optional;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;

@Component
public class SessionManager {

    public static final String SESSION_COOKIE_NAME = "mySessionId";
    private Map<String, User> sessionStore = new ConcurrentHashMap<>();

    public void createSession(User value, HttpServletResponse response){
        String sessionId = UUID.randomUUID().toString();
        sessionStore.put(sessionId, value);

        Cookie cookie = new Cookie(SESSION_COOKIE_NAME, sessionId);
        cookie.setMaxAge(3600);
        cookie.setPath("/");
        response.addCookie(cookie);
    }

    public Optional<User> getSession(HttpServletRequest request){
        Cookie cookie = findCookie(request, SESSION_COOKIE_NAME);
        if(cookie == null){
            return Optional.empty();
        } else if(!sessionStore.containsKey(cookie.getValue())) return Optional.empty();
        return Optional.of(sessionStore.get(cookie.getValue()));
    }

    public void expire(HttpServletRequest request){
        Cookie cookie = findCookie(request, SESSION_COOKIE_NAME);
        if(cookie != null){
            sessionStore.remove(cookie.getValue());
        }
    }

    private Cookie findCookie(HttpServletRequest request, String cookieName) {
        if (request.getCookies() == null) {
            return null;
        }
        return Arrays.stream(request.getCookies())
                .filter(c -> c.getName().equals(cookieName))
                .findAny()
                .orElse(null);
    }
}
