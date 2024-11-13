package com.qiaopi.interceptor;

import com.qiaopi.constant.JwtClaimsConstant;
import com.qiaopi.properties.JwtProperties;
import com.qiaopi.utils.JwtUtil;
import io.jsonwebtoken.Claims;
import io.jsonwebtoken.Jws;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.server.ServerHttpRequest;
import org.springframework.http.server.ServerHttpResponse;
import org.springframework.http.server.ServletServerHttpRequest;
import org.springframework.http.server.ServletServerHttpResponse;
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
        String token = request.getHeaders().getFirst("Sec-WebSocket-Protocol");
        // 获取 Servlet 的 HttpServletRequest 和 HttpServletResponse 对象
        try {
            log.debug("jwt校验:{}", token);
            if (token == null) {
                response.setStatusCode(HttpStatus.UNAUTHORIZED);
                return false;
            }

            Jws<Claims> claims = JwtUtil.parseJWT(token, jwtProperties.getUserSecretKey());
            Long userId = Long.valueOf(claims.getPayload().get(JwtClaimsConstant.USER_ID).toString());
            log.debug("当前用户的id：{}", userId);
            // 将用户 ID 存入 WebSocket 会话属性
            attributes.put("userId", userId);

            // 3、通过，放行
            return true;
        } catch (NumberFormatException e) {
            // 4、不通过，响应401状态码
            response.setStatusCode(HttpStatus.UNAUTHORIZED);
            response.getHeaders().add("Sec-WebSocket-Protocol", "Unauthorized");
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

        // 握手完成后的操作
        // 获取 Servlet 的 HttpServletRequest 和 HttpServletResponse 对象
        // httpRequest 可以获取 HTTP协议升级前 请求报文的信息，如 header中的键值对等
        // httpResponse 可以设置 HTTP响应 的相关信息，如状态码、ContentType、header信息等
        HttpServletRequest httpRequest = ((ServletServerHttpRequest) request).getServletRequest();
        HttpServletResponse httpResponse = ((ServletServerHttpResponse) response).getServletResponse();
        if (httpRequest.getHeader("Sec-WebSocket-Protocol") != null) {
            httpResponse.addHeader("Sec-WebSocket-Protocol", httpRequest.getHeader("Sec-WebSocket-Protocol"));
        }

    }
}
