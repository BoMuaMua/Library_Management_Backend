package com.library.auth.service;

import jakarta.servlet.http.HttpServletRequest;

import java.time.LocalDateTime;

/**
* @author BoMuaMua
* @createDate 2026-03-26 08:46:24
*/
public interface AuthService {

     LocalDateTime setLastLoginTime();


    String login(Long studentNum, String password);

    String logout(HttpServletRequest request);
}
