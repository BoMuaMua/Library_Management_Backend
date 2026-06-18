package com.library.auth.controller;


import com.library.auth.annotation.AutoLog;
import com.library.auth.common.Result;
import com.library.auth.service.AuthService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.servlet.http.HttpServletRequest;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

@Tag(name = "认证管理", description = "负责用户登录、退出")
@RestController
public class AuthController {
    @Autowired
    private AuthService authService;



    @Operation(summary = "登录", description = "生成 AccessToken 和 RefreshToken 并存入Redis中")
    @AutoLog(value = "登录")
    @GetMapping ("/login")
    public Result login(@RequestParam Long studentNum, @RequestParam String password){
        return Result.success(authService.login(studentNum,password));
    }

    @Operation(summary = "退出登录", description = "从 Redis 中作废当前的 AccessToken 和 RefreshToken")
    @AutoLog(value = "退出登录")
    @PostMapping("/logout")
    public Result logout(HttpServletRequest request) {
        return Result.success(authService.logout(request));
    }
}

