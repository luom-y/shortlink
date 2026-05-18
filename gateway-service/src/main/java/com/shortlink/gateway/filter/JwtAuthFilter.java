package com.shortlink.gateway.filter;

import com.shortlink.common.result.Result;
import com.shortlink.common.result.ResultCode;
import com.shortlink.gateway.utils.JwtUtil;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.cloud.gateway.filter.GatewayFilterChain;
import org.springframework.cloud.gateway.filter.GlobalFilter;
import org.springframework.core.Ordered;
import org.springframework.core.io.buffer.DataBuffer;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.server.reactive.ServerHttpRequest;
import org.springframework.http.server.reactive.ServerHttpResponse;
import org.springframework.stereotype.Component;
import org.springframework.web.server.ServerWebExchange;
import reactor.core.publisher.Mono;

import java.nio.charset.StandardCharsets;
import java.util.List;

/**
 * JWT鉴权全局过滤器。
 * 白名单路径（登录/注册/短链接跳转）无需Token，其他请求必须携带有效Token。
 * Token同时校验Redis中的有效性，支持主动登出/Token撤销。
 */
@Slf4j
@Component
@RequiredArgsConstructor
public class JwtAuthFilter implements GlobalFilter, Ordered {

    /** 白名单路径：无需鉴权 */
    private static final List<String> WHITELIST = List.of(
            "/api/v1/admin/users/login",
            "/api/v1/admin/users",
            "/api/v1/admin/users/refresh"
    );

    private static final String ACCESS_TOKEN_PREFIX = "access_token:";

    private final JwtUtil jwtUtil;
    private final StringRedisTemplate stringRedisTemplate;

    @Override
    public Mono<Void> filter(ServerWebExchange exchange, GatewayFilterChain chain) {
        String path = exchange.getRequest().getURI().getPath();

        // 白名单路径和短链接跳转路径不需要鉴权
        if (isWhitelist(path) || isShortLinkRedirect(path)) {
            return chain.filter(exchange);
        }

        String token = extractToken(exchange.getRequest());
        if (token == null) {
            return unauthorized(exchange, "未登录");
        }

        Long userId;
        try {
            userId = jwtUtil.getUserId(token);
        } catch (Exception e) {
            log.warn("JWT 解析失败: path={}", path, e);
            return unauthorized(exchange, "Token无效");
        }

        // 从Redis校验Token是否仍然有效（支持主动撤销）
        String redisKey = ACCESS_TOKEN_PREFIX + userId;
        String storedToken = stringRedisTemplate.opsForValue().get(redisKey);
        if (!token.equals(storedToken)) {
            log.warn("Token 不匹配: userId={}, path={}", userId, path);
            return unauthorized(exchange, "Token已失效");
        }

        // 将userId透传给下游服务
        ServerHttpRequest mutatedRequest = exchange.getRequest().mutate()
                .header("X-User-Id", userId.toString())
                .build();

        return chain.filter(exchange.mutate().request(mutatedRequest).build());
    }

    @Override
    public int getOrder() {
        return -100;
    }

    private boolean isWhitelist(String path) {
        return WHITELIST.stream().anyMatch(path::equals);
    }

    /**
     * 判断是否为短链接跳转路径：单段路径（如 /aBc123），非/api/开头，纯字母数字。
     */
    private boolean isShortLinkRedirect(String path) {
        if (path == null || path.isBlank()) {
            return false;
        }
        String[] segments = path.split("/");
        if (segments.length != 2) {
            return false;
        }
        String segment = segments[1];
        if (segment.isBlank() || segment.startsWith("api") || segment.startsWith("actuator")) {
            return false;
        }
        return segment.matches("[a-zA-Z0-9]+");
    }

    private String extractToken(ServerHttpRequest request) {
        String authHeader = request.getHeaders().getFirst(HttpHeaders.AUTHORIZATION);
        if (authHeader == null || !authHeader.startsWith("Bearer ")) {
            return null;
        }
        return authHeader.substring(7);
    }

    private Mono<Void> unauthorized(ServerWebExchange exchange, String message) {
        ServerHttpResponse response = exchange.getResponse();
        response.setStatusCode(HttpStatus.UNAUTHORIZED);
        response.getHeaders().setContentType(MediaType.APPLICATION_JSON);
        String body = "{\"code\":" + ResultCode.UNAUTHORIZED.getCode() + ",\"message\":\"" + message + "\"}";
        DataBuffer buffer = response.bufferFactory().wrap(body.getBytes(StandardCharsets.UTF_8));
        return response.writeWith(Mono.just(buffer));
    }
}
