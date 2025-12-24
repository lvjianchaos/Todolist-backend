package com.chaos.smarttodo.auth.dto.resp;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class UserLoginRespDTO {
    private String userId;
    private String username;
    private String nickname;
    private String avatar;
    private String token;
}
