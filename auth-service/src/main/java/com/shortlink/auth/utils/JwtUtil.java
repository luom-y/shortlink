package com.shortlink.auth.utils;

import io.jsonwebtoken.Claims;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.security.Keys;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

import javax.crypto.SecretKey;
import java.nio.charset.StandardCharsets;
import java.util.Date;

@Slf4j
@Component
public class JwtUtil {

    @Value("${jwt.secret}")
    private String secretKey;

    @Value("${jwt.access-token-ttl:7200}")
    private Long accessTokenTtl;

    @Value("${jwt.refresh-token-ttl:604800}")
    private Long refreshTokenTtl;

    private SecretKey getSecretKey() {
        byte[] keyBytes = secretKey.getBytes(StandardCharsets.UTF_8);
        return Keys.hmacShaKeyFor(keyBytes);
    }

    // 生成 accessToken
    public String generateAccessToken(Long userId, String role) {
        return buildToken(userId, role, accessTokenTtl);
    }

    // 生成 refreshToken  TTl=90天  ， 但有这个token也不一定能刷新 需要查redis真实过期
    public String generateRefreshToken(Long userId, String role) {
        return buildToken(userId, role, refreshTokenTtl);
    }

    private String buildToken(Long userId, String role, long ttlSeconds) {
        Date now = new Date();
        return Jwts.builder()
                .claim("userId", userId)
                .claim("role", role)
                .issuedAt(now)
                .expiration(new Date(now.getTime() + ttlSeconds * 1000))
                .signWith(getSecretKey())
                .compact();
    }

    // 解析 token 返回 Claims
    public Claims parseToken(String token) {
        return Jwts.parser()
                .verifyWith(getSecretKey())
                .build()
                .parseSignedClaims(token)
                .getPayload();
    }

    // 从 token 中提取 userId
    public Long getUserId(String token) {
        return parseToken(token).get("userId", Long.class);
    }
}
