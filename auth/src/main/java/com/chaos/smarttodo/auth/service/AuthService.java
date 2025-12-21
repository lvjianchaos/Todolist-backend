package com.chaos.smarttodo.auth.service;

import com.chaos.smarttodo.auth.dto.req.UserLoginReqDTO;
import com.chaos.smarttodo.auth.dto.req.UserRegisterReqDTO;
import com.chaos.smarttodo.auth.dto.resp.UserLoginRespDTO;
import com.chaos.smarttodo.auth.entity.User;

public interface AuthService {
    void register(UserRegisterReqDTO dto);

    UserLoginRespDTO login(UserLoginReqDTO dto);

    User getUserById(Long userId);
}
