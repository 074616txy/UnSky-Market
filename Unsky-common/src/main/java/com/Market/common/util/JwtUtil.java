package com.Market.common.util;

import io.jsonwebtoken.Claims;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.SignatureAlgorithm;
import io.jsonwebtoken.security.Keys;

import java.util.Date;

public class JwtUtil {

    /**
     * SECRET_KEY：JWT 签名密钥
     * EXPIRE_TIME：过期时间，这里先写成 1 天
     */
    private static final String SECRET_KEY = "unsky-market-secret-key-unsky-market";
    private static final long EXPIRE_TIME = 1000 * 60 * 60 * 24;

    /**
     * 接收 userId
     * 生成 token
     * 返回 token 字符串
     * 根据用户 id，生成一个“带身份信息 + 带过期时间 + 带签名”的 token。
     * @param userId
     * @return
     */
    public static String generateToken(Long userId){
    //表示当时时间
    Date now = new Date();
    //把用户id包装成一个有签名、带过期时间的token字符串
    Date expireDate = new Date(now.getTime() + EXPIRE_TIME);

    return Jwts.builder()
            .setSubject(String.valueOf(userId))
            .setIssuedAt(now)
            .setExpiration(expireDate)
            .signWith(Keys.hmacShaKeyFor(SECRET_KEY.getBytes()), SignatureAlgorithm.HS256)
            .compact();
    }

    /**
     * 根据token解析当前用户id
     * 登录时是把 userId 放进 Token
     * 现在是把 Token 中的 userId 再取出来
     * 这一步是后续接入受保护接口身份识别的基础
     * @param token
     * @return
     */
    public static Long getUserIdFromToken(String token) {
        Claims claims = Jwts.parserBuilder()
                .setSigningKey(Keys.hmacShaKeyFor(SECRET_KEY.getBytes()))
                .build()
                .parseClaimsJws(token)//parserBuilder解析并校验这张 Token
                .getBody();

        return Long.valueOf(claims.getSubject());
    }
}
