/*
 *  Copyright 2019-2025 Zheng Jie
 *
 *  Licensed under the Apache License, Version 2.0 (the "License");
 *  you may not use this file except in compliance with the License.
 *  You may obtain a copy of the License at
 *
 *  http://www.apache.org/licenses/LICENSE-2.0
 *
 *  Unless required by applicable law or agreed to in writing, software
 *  distributed under the License is distributed on an "AS IS" BASIS,
 *  WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 *  See the License for the specific language governing permissions and
 *  limitations under the License.
 */
package me.zhengjie.modules.security.security;

import cn.hutool.core.util.IdUtil;
import io.jsonwebtoken.*;
import io.jsonwebtoken.security.Keys;
import lombok.extern.slf4j.Slf4j;
import me.zhengjie.modules.security.config.SecurityProperties;
import me.zhengjie.modules.security.service.dto.JwtUserDto;
import org.springframework.beans.factory.InitializingBean;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.userdetails.User;
import org.springframework.stereotype.Component;
import org.springframework.util.StringUtils;
import javax.servlet.http.HttpServletRequest;
import java.security.Key;
import java.util.*;

/**
 * @author /
 */
@Slf4j
@Component
public class TokenProvider implements InitializingBean {

    private Key signingKey;
    private JwtParser jwtParser;
    private final SecurityProperties properties;
    public static final String AUTHORITIES_UUID_KEY = "uid";
    public static final String AUTHORITIES_UID_KEY = "userId";

    public TokenProvider(SecurityProperties properties) {
        this.properties = properties;
    }

    @Override
    public void afterPropertiesSet() {
        String secret = properties.getBase64Secret();
        if (!StringUtils.hasText(secret)) {
            throw new IllegalStateException("Configure JWT_BASE64_SECRET with a Base64-encoded random key of at least 64 bytes");
        }
        byte[] keyBytes;
        try {
            keyBytes = Base64.getDecoder().decode(secret);
        } catch (IllegalArgumentException e) {
            throw new IllegalStateException("JWT_BASE64_SECRET must be valid Base64", e);
        }
        if (keyBytes.length < 64) {
            throw new IllegalStateException("JWT_BASE64_SECRET must decode to at least 64 bytes for HS512");
        }
        this.signingKey = Keys.hmacShaKeyFor(keyBytes);
        // 初始化 JwtParser
        jwtParser = Jwts.parserBuilder()
                .setSigningKey(signingKey) // 使用预生成的签名密钥
                .build();
    }

    /**
     * 创建无到期时间的 Token，会话的撤销由 Redis 维护。
     *
     * @param user /
     * @return /
     */
    public String createToken(JwtUserDto user) {
        // 设置参数
        Map<String, Object> claims = new HashMap<>(6);
        // 设置用户ID
        claims.put(AUTHORITIES_UID_KEY, user.getUser().getId());
        // 设置UUID，确保每次Token不一样
        claims.put(AUTHORITIES_UUID_KEY, IdUtil.simpleUUID());
        // 直接调用 Jwts.builder() 创建新实例
        return Jwts.builder()
                // 设置自定义 Claims
                .setClaims(claims)
                // 设置主题
                .setSubject(user.getUsername())
                // 使用预生成的签名密钥和算法签名
                .signWith(signingKey, SignatureAlgorithm.HS512)
                .compact();
    }

    /**
     * 依据Token 获取鉴权信息
     *
     * @param token /
     * @return /
     */
    Authentication getAuthentication(String token) {
        Claims claims = getClaims(token);
        User principal = new User(claims.getSubject(), "******", new ArrayList<>());
        return new UsernamePasswordAuthenticationToken(principal, token, new ArrayList<>());
    }

    public Claims getClaims(String token) {
        return jwtParser
                .parseClaimsJws(token)
                .getBody();
    }

    public String getToken(HttpServletRequest request) {
        final String requestHeader = request.getHeader(properties.getHeader());
        if (requestHeader != null && requestHeader.startsWith(properties.getTokenStartWith())) {
            return requestHeader.substring(7);
        }
        return null;
    }

    /**
     * 获取登录用户RedisKey
     * @param token /
     * @return key
     */
    public String loginKey(String token) {
        Claims claims = getClaims(token);
        return properties.getOnlineKey() + claims.getSubject() + ":" + getId(token);
    }

    /**
     * 获取登录用户TokenKey
     * @param token /
     * @return /
     */
    public String getId(String token) {
        Claims claims = getClaims(token);
        return claims.get(AUTHORITIES_UUID_KEY).toString();
    }
}
