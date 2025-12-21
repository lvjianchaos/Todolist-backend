package com.chaos.smarttodo.auth;

import org.mybatis.spring.annotation.MapperScan;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.cloud.client.discovery.EnableDiscoveryClient;

/**
 * 认证中心启动类
 */
@SpringBootApplication
@EnableDiscoveryClient
@MapperScan("com.chaos.smarttodo.auth.mapper")
public class AuthApplication {
    public static void main(String[] args) {
        SpringApplication.run(AuthApplication.class, args);
        System.out.println("====== Smart-Todo 认证模块启动成功 ======");
    }
}