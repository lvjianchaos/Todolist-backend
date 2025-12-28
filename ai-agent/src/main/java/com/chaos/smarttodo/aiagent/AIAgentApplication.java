package com.chaos.smarttodo.aiagent;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.cloud.client.discovery.EnableDiscoveryClient;
import org.springframework.cloud.openfeign.EnableFeignClients;

@SpringBootApplication
@EnableDiscoveryClient
@EnableFeignClients
public class AIAgentApplication {
    public static void main(String[] args) {
        SpringApplication.run(AIAgentApplication.class, args);
        System.out.println("====== Smart-Todo AI Agent 启动成功 ======");
    }
}
