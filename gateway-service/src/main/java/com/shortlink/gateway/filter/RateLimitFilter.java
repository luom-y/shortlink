package com.shortlink.gateway.filter;

import lombok.extern.slf4j.Slf4j;
import org.springframework.cloud.gateway.filter.GatewayFilterChain;
import org.springframework.cloud.gateway.filter.GlobalFilter;
import org.springframework.core.Ordered;
import org.springframework.core.io.buffer.DataBuffer;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.data.redis.core.script.DefaultRedisScript;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.server.reactive.ServerHttpResponse;
import org.springframework.stereotype.Component;
import org.springframework.web.server.ServerWebExchange;
import reactor.core.publisher.Mono;

import java.nio.charset.StandardCharsets;
import java.util.List;

/**
 * Redis + Lua 滑动窗口限流过滤器。
 * 默认限制：每IP每接口 100次/60秒。
 * 仅对 /api/ 开头的接口生效，短链接跳转路径不限流。
 */
@Slf4j
@Component
public class RateLimitFilter implements GlobalFilter, Ordered {

    private static final String RATE_LIMIT_KEY_PREFIX = "ratelimit:";
    private static final int DEFAULT_LIMIT = 100;
    private static final int DEFAULT_WINDOW_SECONDS = 60;

    private final StringRedisTemplate stringRedisTemplate;

    /** Lua脚本：基于ZSET的滑动窗口限流 */
    private static final String LUA_SCRIPT = """
            local key = KEYS[1]
            local limit = tonumber(ARGV[1])
            local window = tonumber(ARGV[2])
            local now = tonumber(ARGV[3])
            local member = ARGV[4]

            redis.call('ZREMRANGEBYSCORE', key, 0, now - window * 1000)

            local count = redis.call('ZCARD', key)
            if count >= limit then
                return 0
            end

            redis.call('ZADD', key, now, member)
            redis.call('EXPIRE', key, window + 1)
            return 1
            """;

    private final DefaultRedisScript<Long> rateLimitScript;

    public RateLimitFilter(StringRedisTemplate stringRedisTemplate) {
        this.stringRedisTemplate = stringRedisTemplate;
        this.rateLimitScript = new DefaultRedisScript<>(LUA_SCRIPT, Long.class);
    }

    @Override
    public Mono<Void> filter(ServerWebExchange exchange, GatewayFilterChain chain) {
        String path = exchange.getRequest().getURI().getPath();

        // 仅对API接口限流
        if (!path.startsWith("/api/")) {
            return chain.filter(exchange);
        }

        String clientIp = getClientIp(exchange);
        String key = RATE_LIMIT_KEY_PREFIX + clientIp + ":" + path;
        long now = System.currentTimeMillis();
        String member = now + ":" + System.nanoTime();

        Long allowed = stringRedisTemplate.execute(
                rateLimitScript,
                List.of(key),
                String.valueOf(DEFAULT_LIMIT),
                String.valueOf(DEFAULT_WINDOW_SECONDS),
                String.valueOf(now),
                member
        );

        if (allowed == null || allowed == 0) {
            log.warn("限流触发: ip={}, path={}", clientIp, path);
            return rateLimited(exchange);
        }

        return chain.filter(exchange);
    }

    @Override
    public int getOrder() {
        // 在JWT鉴权之后(-100)执行
        return -90;
    }

    /** 获取客户端真实IP（考虑反向代理） */
    private String getClientIp(ServerWebExchange exchange) {
        String ip = exchange.getRequest().getHeaders().getFirst("X-Forwarded-For");
        if (ip == null || ip.isBlank() || "unknown".equalsIgnoreCase(ip)) {
            ip = exchange.getRequest().getHeaders().getFirst("X-Real-IP");
        }
        if (ip == null || ip.isBlank() || "unknown".equalsIgnoreCase(ip)) {
            ip = exchange.getRequest().getRemoteAddress() != null
                    ? exchange.getRequest().getRemoteAddress().getAddress().getHostAddress()
                    : "unknown";
        }
        if (ip != null && ip.contains(",")) {
            ip = ip.split(",")[0].trim();
        }
        return ip;
    }

    private Mono<Void> rateLimited(ServerWebExchange exchange) {
        ServerHttpResponse response = exchange.getResponse();
        response.setStatusCode(HttpStatus.TOO_MANY_REQUESTS);
        response.getHeaders().setContentType(MediaType.APPLICATION_JSON);
        String body = "{\"code\":3001,\"message\":\"请求过于频繁，请稍后重试\"}";
        DataBuffer buffer = response.bufferFactory().wrap(body.getBytes(StandardCharsets.UTF_8));
        return response.writeWith(Mono.just(buffer));
    }
}
