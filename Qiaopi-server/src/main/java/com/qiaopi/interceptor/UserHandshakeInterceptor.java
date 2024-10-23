package com.qiaopi.interceptor;

import com.qiaopi.constant.JwtClaimsConstant;
import com.qiaopi.properties.JwtProperties;
import com.qiaopi.utils.JwtUtil;
import io.jsonwebtoken.Claims;
import io.jsonwebtoken.Jws;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.server.ServerHttpRequest;
import org.springframework.http.server.ServerHttpResponse;
import org.springframework.stereotype.Component;
import org.springframework.web.socket.WebSocketHandler;
import org.springframework.web.socket.server.HandshakeInterceptor;
import java.util.Map;

@Component
@Slf4j
public class UserHandshakeInterceptor implements HandshakeInterceptor {

    @Autowired
    private JwtProperties jwtProperties;

    @Override
    public boolean beforeHandshake(ServerHttpRequest request,
                                   ServerHttpResponse response,
                                   WebSocketHandler wsHandler,
                                   Map<String, Object> attributes) throws Exception {

        // 从请求头中获取 token
        String token = request.getHeaders().getFirst("Authorization");

        try {
            log.info("jwt校验:{}", token);
            if (token == null) {
                response.setStatusCode(HttpStatus.UNAUTHORIZED);
                return false;
            }

            Jws<Claims> claims = JwtUtil.parseJWT(token,jwtProperties.getUserSecretKey());
            Long userId = Long.valueOf(claims.getPayload().get(JwtClaimsConstant.USER_ID).toString());
            log.info("当前用户的id：{}", userId);
            // 将用户 ID 存入 WebSocket 会话属性
            attributes.put("userId", userId);

            // 3、通过，放行
            return true;
        } catch (NumberFormatException e) {
            // 4、不通过，响应401状态码
            response.setStatusCode(HttpStatus.UNAUTHORIZED);
            //writeErrorResponse(response, message("user.please.relogin"), UNAUTHORIZED);
            return false;
        }

    }

    @Override
    public void afterHandshake(ServerHttpRequest request,
                               ServerHttpResponse response,
                               WebSocketHandler wsHandler,
                               Exception exception) {
        // 可以在这里处理握手成功或失败后的逻辑
    }
}
   