package com.chaos.smarttodo.gateway.filter;

import com.chaos.smarttodo.common.utils.JwtUtils;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
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

import java.util.List;

/**
 * 全局 JWT 鉴权过滤器
 */
@Component
@Slf4j
public class JwtAuthFilter implements GlobalFilter, Ordered {

    @Autowired
    private JwtUtils jwtUtils; // 注入 Bean

    @Value("${auth.skip-urls}")
    private List<String> skipUrls;

    private final AntPathMatcher pathMatcher = new AntPathMatcher();

    @Override
    public Mono<Void> filter(ServerWebExchange exchange, GatewayFilterChain chain) {
        ServerHttpRequest request = exchange.getRequest();
        String path = request.getURI().getPath();

        log.info("网关收到请求: {}", path);

        // 1. 白名单校验
        for (String skipUrl : skipUrls) {
            if (pathMatcher.match(skipUrl, path)) {
                return chain.filter(exchange);
            }
        }

        // 2. 获取并校验 Token
        String authHeader = request.getHeaders().getFirst("Authorization");
        if (authHeader == null || !authHeader.startsWith("Bearer ")) {
            return unauthorizedResponse(exchange);
        }

        String token = authHeader.substring(7);
        if (!JwtUtils.validateToken(token)) {
            return unauthorizedResponse(exchange);
        }

        // 3. 解析用户ID并传递给下游服务
        String userId = jwtUtils.getUserId(token);
        log.info("用户 {} 校验通过", userId);

        // 将 userId 放入 Header，下游服务可直接获取
        ServerHttpRequest newRequest = request.mutate()
                .header("X-User-Id", userId)
                .build();

        return chain.filter(exchange.mutate().request(newRequest).build());
    }

    private Mono<Void> unauthorizedResponse(ServerWebExchange exchange) {
        ServerHttpResponse response = exchange.getResponse();
        response.setStatusCode(HttpStatus.UNAUTHORIZED);
        return response.setComplete();
    }

    @Override
    public int getOrder() {
        return -100; // 优先级尽量高
    }
}