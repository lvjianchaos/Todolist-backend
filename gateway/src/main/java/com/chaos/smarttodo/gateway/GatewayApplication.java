package com.chaos.smarttodo.gateway;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.cloud.client.discovery.EnableDiscoveryClient;

/**
 * 网关启动类
 * 负责路由转发与鉴权处理
 */
@SpringBootApplication
@EnableDiscoveryClient // 开启服务发现功能
public class GatewayApplication {
    public static void main(String[] args) {
        SpringApplication.run(GatewayApplication.class, args);
        System.out.println(">>> Smart-Todo Gateway 启动成功！");
    }
}
