package com.chaos.smarttodo.aiAgent;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.cloud.client.discovery.EnableDiscoveryClient;
@SpringBootApplication
@EnableDiscoveryClient // 开启服务发现功能
public class AIAgentApplication{
    public static void main(String[] args) {
        SpringApplication.run(AIAgentApplication.class, args);
    }
}