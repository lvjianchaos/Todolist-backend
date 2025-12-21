package com.chaos.smarttodo.auth.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.chaos.smarttodo.auth.common.enums.UserErrorCodeEnum;
import com.chaos.smarttodo.auth.common.exception.ServiceException;
import com.chaos.smarttodo.auth.dto.req.UserLoginReqDTO;
import com.chaos.smarttodo.auth.dto.req.UserRegisterReqDTO;
import com.chaos.smarttodo.auth.dto.resp.UserLoginRespDTO;
import com.chaos.smarttodo.auth.entity.User;
import com.chaos.smarttodo.auth.mapper.UserMapper;
import com.chaos.smarttodo.auth.service.AuthService;
import com.chaos.smarttodo.auth.util.JwtUtil;
import lombok.RequiredArgsConstructor;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

/**
 * 认证服务实现类
 *
 * @author chaos
 */
@Service
@RequiredArgsConstructor
public class AuthServiceImpl implements AuthService {
    private final UserMapper userMapper;
    private final PasswordEncoder passwordEncoder;
    private final JwtUtil jwtUtil;

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void register(UserRegisterReqDTO dto) {
        // 1. 判断用户是否存在
        Long count = userMapper.selectCount(new LambdaQueryWrapper<User>().eq(User::getUsername, dto.getUsername()));
        if (count > 0) {
            throw new ServiceException(UserErrorCodeEnum.USER_NAME_EXIST);
        }

        // 2. 构造用户对象并加密密码
        User user = new User();
        user.setUsername(dto.getUsername());
        user.setNickname(dto.getNickname() != null ? dto.getNickname() : dto.getUsername());
        user.setPassword(passwordEncoder.encode(dto.getPassword()));

        userMapper.insert(user);
    }

    @Override
    public UserLoginRespDTO login(UserLoginReqDTO dto) {
        // 1. 查询用户（需要带密码）
        User user = userMapper.selectByUsernameWithPassword(dto.getUsername());

        // 2. 校验用户
        if (user == null) {
            throw new ServiceException(UserErrorCodeEnum.USER_NULL);
        }
        // 3. 校验密码
        if (!passwordEncoder.matches(dto.getPassword(), user.getPassword())) {
            throw new ServiceException(UserErrorCodeEnum.USER_PASSWORD_ERROR);
        }

        // 4. 签发 Token
        String token = jwtUtil.createToken(user.getId().toString(), user.getUsername());

        // 5. 构造响应对象
        return new UserLoginRespDTO(user.getId().toString(), user.getUsername(), user.getNickname(), token);
    }

    @Override
    public User getUserById(Long userId) {
        User user = userMapper.selectById(userId);
        if (user == null) {
            throw new RuntimeException("用户不存在");
        }
        return user;
    }
}
