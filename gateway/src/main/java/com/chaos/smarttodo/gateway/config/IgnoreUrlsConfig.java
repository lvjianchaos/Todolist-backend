package com.chaos.smarttodo.gateway.config;

import lombok.Data;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.stereotype.Component;
import java.util.List;

@Data
@Component
@ConfigurationProperties(prefix = "secure")
public class IgnoreUrlsConfig {
    private List<String> ignoreUrls;
}