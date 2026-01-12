package com.lingnan.fruitshop.security;

import io.jsonwebtoken.Claims;
import io.jsonwebtoken.Jws;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.SignatureAlgorithm;
import io.jsonwebtoken.security.Keys;
import org.springframework.stereotype.Component;

import javax.crypto.SecretKey;
import java.nio.charset.StandardCharsets;
import java.util.Date;

@Component
public class JwtTokenUtil {

    // ==================== 依赖注入 ====================
    private final JwtProperties jwtProperties;

    public JwtTokenUtil(JwtProperties jwtProperties) {
        this.jwtProperties = jwtProperties;
    }

    // ==================== 客户用户JWT令牌生成 ====================
    // 用于普通用户登录后的身份认证
    public String generateToken(long userId, String username) {
        long now = System.currentTimeMillis();
        Date issuedAt = new Date(now);
        Date expireAt = new Date(now + jwtProperties.getExpireSeconds() * 1000L);

        return Jwts.builder()
                .setSubject(String.valueOf(userId))           // 设置用户ID作为主题
                .claim("type", "customer")                    // 声明令牌类型：客户用户
                .claim("username", username)                  // 添加用户名声明
                .setIssuedAt(issuedAt)                        // 设置签发时间
                .setExpiration(expireAt)                      // 设置过期时间
                .signWith(getKey(), SignatureAlgorithm.HS256)  // 使用HS256算法签名
                .compact();
    }

    // ==================== 管理员JWT令牌生成 ====================
    // 用于管理员登录后的身份认证
    public String generateAdminToken(long adminId, String username) {
        long now = System.currentTimeMillis();
        Date issuedAt = new Date(now);
        Date expireAt = new Date(now + jwtProperties.getExpireSeconds() * 1000L);

        return Jwts.builder()
                .setSubject(String.valueOf(adminId))          // 设置管理员ID作为主题
                .claim("type", "admin")                       // 声明令牌类型：管理员
                .claim("username", username)                  // 添加用户名声明
                .setIssuedAt(issuedAt)                        // 设置签发时间
                .setExpiration(expireAt)                      // 设置过期时间
                .signWith(getKey(), SignatureAlgorithm.HS256)  // 使用HS256算法签名
                .compact();
    }

    // ==================== 商家JWT令牌生成 ====================
    // 用于商家登录后的身份认证
    public String generateMerchantToken(long merchantId, Long shopId, String account) {
        long now = System.currentTimeMillis();
        Date issuedAt = new Date(now);
        Date expireAt = new Date(now + jwtProperties.getExpireSeconds() * 1000L);

        return Jwts.builder()
                .setSubject(String.valueOf(merchantId))       // 设置商家ID作为主题
                .claim("type", "merchant")                    // 声明令牌类型：商家
                .claim("shopId", shopId)                       // 添加店铺ID声明
                .claim("account", account)                     // 添加账号声明
                .setIssuedAt(issuedAt)                        // 设置签发时间
                .setExpiration(expireAt)                      // 设置过期时间
                .signWith(getKey(), SignatureAlgorithm.HS256)  // 使用HS256算法签名
                .compact();
    }

    // ==================== JWT令牌解析 ====================
    // 验证并解析JWT令牌，提取其中的声明信息
    public Jws<Claims> parseToken(String token) {
        return Jwts.parserBuilder()
                .setSigningKey(getKey())                    // 设置签名密钥进行验证
                .build()
                .parseClaimsJws(token);                     // 解析令牌
    }

    // ==================== 签名密钥获取 ====================
    // 根据配置的密钥字符串生成HMAC-SHA密钥
    private SecretKey getKey() {
        String secret = jwtProperties.getSecret();
        return Keys.hmacShaKeyFor(secret.getBytes(StandardCharsets.UTF_8));
    }
}
