package com.chaos.smarttodo.gateway.config;

import lombok.Data;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.cloud.context.config.annotation.RefreshScope;
import org.springframework.stereotype.Component;

@Data
@Component
@RefreshScope // 开启此注解后，Nacos 配置修改时，该对象属性会同步更新
@ConfigurationProperties(prefix = "secure.jwt")
public class JwtProperties {
    /**
     * JWT 签名密钥
     */
    private String secretKey;
}
