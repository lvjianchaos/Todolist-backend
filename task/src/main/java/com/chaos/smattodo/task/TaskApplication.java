package com.chaos.smattodo.task;

import org.mybatis.spring.annotation.MapperScan;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.cloud.client.discovery.EnableDiscoveryClient;

/**
 * 任务模块启动类
 */
@SpringBootApplication
@EnableDiscoveryClient
@MapperScan("com.chaos.smattodo.task.mapper")
public class TaskApplication {
    public static void main(String[] args) {
        SpringApplication.run(TaskApplication.class, args);
        System.out.println("====== Smart-Todo 任务模块启动成功 ======");
    }
}
