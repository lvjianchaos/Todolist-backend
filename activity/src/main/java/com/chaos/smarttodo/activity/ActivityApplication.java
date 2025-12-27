package com.chaos.smarttodo.activity;

import org.mybatis.spring.annotation.MapperScan;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.cloud.client.discovery.EnableDiscoveryClient;

@EnableDiscoveryClient
@SpringBootApplication
@MapperScan("com.chaos.smarttodo.activity.mapper")

public class ActivityApplication {
    public static void main(String[] args) {
        SpringApplication.run(ActivityApplication.class, args);
        System.out.println("====== Smart-Todo 活动模块启动成功 ======");
    }
}

