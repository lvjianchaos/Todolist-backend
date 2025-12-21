package com.chaos.smarttodo.gateway.filter;

import com.chaos.smarttodo.gateway.config.IgnoreUrlsConfig;
import com.chaos.smarttodo.gateway.config.JwtProperties;
import io.jsonwebtoken.Claims;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.security.Keys;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.cloud.gateway.filter.GatewayFilterChain;
import org.springframework.cloud.gateway.filter.GlobalFilter;
import org.springframework.core.Ordered;
import org.springframework.http.HttpStatus;
import org.springframework.http.server.reactive.ServerHttpRequest;
import org.springframework.http.server.reactive.ServerHttpResponse;
import org.springframework.stereotype.Component;
import org.springframework.util.AntPathMatcher;
import org.springframework.web.server.ServerWebExchange;
import reactor.core.publisher.Mono;

import java.nio.charset.StandardCharsets;

@Slf4j
@Component
public class JwtAuthFilter implements GlobalFilter, Ordered {

    @Autowired
    private IgnoreUrlsConfig ignoreUrlsConfig;

    @Autowired
    private JwtProperties jwtProperties;

    private final AntPathMatcher pathMatcher = new AntPathMatcher();

    @Override
    public Mono<Void> filter(ServerWebExchange exchange, GatewayFilterChain chain) {
        ServerHttpRequest request = exchange.getRequest();
        String path = request.getURI().getPath();

        // 1. 白名单检查
        if (ignoreUrlsConfig.getIgnoreUrls() != null) {
            for (String url : ignoreUrlsConfig.getIgnoreUrls()) {
                if (pathMatcher.match(url, path)) {
                    return chain.filter(exchange);
                }
            }
        }

        // 2. 获取 Token
        String token = request.getHeaders().getFirst("Authorization");
        if (token == null || !token.startsWith("Bearer ")) {
            return handleUnAuthorized(exchange, "Missing or invalid Authorization header");
        }
        token = token.substring(7);

        // 3. 校验 Token
        try {
            // 从动态配置类中获取最新的 secretKey
            String currentSecret = jwtProperties.getSecretKey();

            Claims claims = Jwts.parserBuilder()
                    .setSigningKey(Keys.hmacShaKeyFor(currentSecret.getBytes(StandardCharsets.UTF_8)))
                    .build()
                    .parseClaimsJws(token)
                    .getBody();

            // 4. 传递用户信息
            String userId = claims.getSubject();
            log.info("Token verified. User: {}, Path: {}", userId, path);

            ServerHttpRequest mutableRequest = request.mutate()
                    .header("X-User-Id", userId)
                    .build();

            return chain.filter(exchange.mutate().request(mutableRequest).build());

        } catch (Exception e) {
            log.error("JWT validation failed: {}", e.getMessage());
            return handleUnAuthorized(exchange, "Token expired or invalid");
        }
    }

    private Mono<Void> handleUnAuthorized(ServerWebExchange exchange, String msg) {
        log.warn("Unauthorized request: {}", msg);
        ServerHttpResponse response = exchange.getResponse();
        response.setStatusCode(HttpStatus.UNAUTHORIZED);
        return response.setComplete();
    }

    @Override
    public int getOrder() {
        return -100;
    }
}