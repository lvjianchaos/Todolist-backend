package com.chaos.smarttodo.auth.controller;

import com.chaos.smarttodo.auth.common.result.Result;
import com.chaos.smarttodo.auth.common.result.Results;
import com.chaos.smarttodo.auth.dto.req.UserLoginReqDTO;
import com.chaos.smarttodo.auth.dto.req.UserRegisterReqDTO;
import com.chaos.smarttodo.auth.dto.resp.UserLoginRespDTO;
import com.chaos.smarttodo.auth.entity.User;
import com.chaos.smarttodo.auth.service.AuthService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/auth")
@RequiredArgsConstructor
public class AuthController {

    private final AuthService authService;

    @PostMapping("/register")
    public Result<Void> register(@Valid @RequestBody UserRegisterReqDTO dto) {
        authService.register(dto);
        return Results.success();
    }

    @PostMapping("/login")
    public Result<UserLoginRespDTO> login(@Valid @RequestBody UserLoginReqDTO dto) {
        return Results.success(authService.login(dto));
    }

    /**
     * 获取当前登录用户信息
     * 用户 ID 由网关解析 JWT 后通过 Header 传递
     *
     * @param userId 自动从请求头中获取 X-User-Id
     * @return 用户基本信息（不含密码）
     */
    @GetMapping("/info")
    public Result<User> getInfo(@RequestHeader("X-User-Id") Long userId) {
        return Results.success(authService.getUserById(userId));
    }
}
