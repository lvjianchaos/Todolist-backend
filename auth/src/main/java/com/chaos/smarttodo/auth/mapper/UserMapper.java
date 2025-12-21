package com.chaos.smarttodo.auth.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.chaos.smarttodo.auth.entity.User;
import org.apache.ibatis.annotations.Select;

/**
 * 用户持久层
 */
public interface UserMapper extends BaseMapper<User> {

    /**
     * 登录时需要查询密码，手动定义 SQL 覆盖实体类上的 select=false
     */
    @Select("SELECT * FROM user WHERE username = #{username}")
    User selectByUsernameWithPassword(String username);
}