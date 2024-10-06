package com.qiaopi.utils;

import io.jsonwebtoken.*;
import io.jsonwebtoken.security.Keys;
import lombok.extern.slf4j.Slf4j;

import javax.crypto.SecretKey;
import java.nio.charset.StandardCharsets;
import java.util.Date;
import java.util.Map;

/**
 * @ClassName JwtUtil工具类
 * @project Qiaopi_common
 * @Description
 * @Version 1.0
 */
@Slf4j
public class JwtUtil {

    /**
     * 生成jwt
     * 使用Hs256算法，私钥使用固定密钥
     * @param secretKey  jwt密钥
     * @param ttlMillis  jwt过期时间，单位毫秒
     * @param claims     设置的信息
     * @return
     */
    public static String createJWT(String secretKey, long ttlMillis, Map<String, Object> claims){

        // 指定签名的时候使用的签名算法，也就是header那部分
        SignatureAlgorithm signatureAlgorithm = SignatureAlgorithm.HS256;

        // 生成JWT的时间
        long expMillis = System.currentTimeMillis() + ttlMillis;
        Date exp = new Date(expMillis);

        // 设置jwt的body
        JwtBuilder builder = Jwts.builder()
                // 如果有私有声明，一定要先设置这个自己创建的私有的声明，这个是给builder的claim赋值，一旦写在标准的声明赋值之后，就是覆盖了那些标准的声明的
                .setClaims(claims)
                // 设置签名使用的签名算法和签名使用的秘钥
                .signWith(signatureAlgorithm, secretKey.getBytes(StandardCharsets.UTF_8))
                // 设置过期时间
                .setExpiration(exp);

        return builder.compact();
    }


    /**
     * 解析jwt
     * @param token
     * @param secretKey
     * @return
     */
    public static Jws<Claims> parseJWT(String token, String secretKey){
        //密钥实例
        SecretKey key = Keys.hmacShaKeyFor(secretKey.getBytes(StandardCharsets.UTF_8));

        Jws<Claims> claimsJws = Jwts.parser()
                .verifyWith(key)  //设置签名的密钥
                .build()
                .parseSignedClaims(token); //设置要解析的jwt

        return claimsJws;
    }

}