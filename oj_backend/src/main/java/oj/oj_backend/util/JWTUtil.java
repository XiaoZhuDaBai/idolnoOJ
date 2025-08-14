package oj.oj_backend.util;

/**
 * @author XiaoZhuDaBai
 * @version 1.0
 * @date 2025/7/15 23:03
 */

import io.jsonwebtoken.Claims;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.SignatureAlgorithm;
import io.jsonwebtoken.security.Keys;

import java.security.Key;
import java.util.Date;
import java.util.Map;

public class JWTUtil {

    private static final Key SIGN_KEY = Keys.secretKeyFor(SignatureAlgorithm.HS256);

    // 5天过期时间（单位：毫秒）
    private static final long EXPIRATION = 5 * 24 * 60 * 60 * 1000; // 5天

    public static String generateJWT(Map<String, Object> claims) {
        return Jwts.builder()
                .setClaims(claims)
                .setExpiration(new Date(System.currentTimeMillis() + EXPIRATION))
                .signWith(SIGN_KEY, SignatureAlgorithm.HS256)
                .compact();
    }


}
