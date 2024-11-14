package com.qiaopi.interceptor;

import cn.hutool.json.JSONObject;
import com.qiaopi.constant.JwtClaimsConstant;
import com.qiaopi.context.UserContext;
import com.qiaopi.properties.JwtProperties;
import com.qiaopi.utils.JwtUtil;
import io.jsonwebtoken.Claims;
import io.jsonwebtoken.Jws;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import org.springframework.web.method.HandlerMethod;
import org.springframework.web.servlet.HandlerInterceptor;

import java.io.IOException;
import java.util.HashSet;
import java.util.Set;

import static com.qiaopi.constant.HttpStatus.UNAUTHORIZED;
import static com.qiaopi.utils.MessageUtils.message;

//import javax.servlet.http.HttpServletRequest;
//import javax.servlet.http.HttpServletResponse;

/**
 * jwt令牌校验的拦截器
 */
@Component
@Slf4j
public class JwtTokenUserInterceptor implements HandlerInterceptor {

    @Autowired
    private JwtProperties jwtProperties;

    // 白名单集合
    private final Set<String> whiteList = new HashSet<>();

    @Autowired
    public JwtTokenUserInterceptor(JwtProperties jwtProperties) {
        this.jwtProperties = jwtProperties;
        // 初始化白名单
        whiteList.add("/card/list");
        whiteList.add("/font/list");
        whiteList.add("/font/listColor");
        whiteList.add("/paper/list");
        whiteList.add("/marketing/list");
    }

    /**
     * 校验jwt
     *
     * @param request
     * @param response
     * @param handler
     * @return
     * @throws Exception
     */
    public boolean preHandle(HttpServletRequest request, HttpServletResponse response, Object handler){
        //判断当前拦截到的是Controller的方法还是其他资源
        if (!(handler instanceof HandlerMethod)) {
            //当前拦截到的不是动态方法，直接放行
            return true;
        }

        // 检查请求路径是否在白名单中
        String requestURI = request.getRequestURI();
        if (whiteList.contains(requestURI)) {
            // 如果在白名单中，尝试获取用户ID，如果获取不到也不影响放行
            String token = request.getHeader(jwtProperties.getUserTokenName());
            if (token != null) {
                try {
                    Jws<Claims> claims = JwtUtil.parseJWT(token, jwtProperties.getUserSecretKey());
                    Long userId = Long.valueOf(claims.getPayload().get(JwtClaimsConstant.USER_ID).toString());
                    log.info("当前用户的id：{}", userId);
                    UserContext.setUserId(userId);
                } catch (Exception ex) {
                    log.warn("解析JWT失败，但请求路径在白名单中，放行请求", ex);
                }
            }
            return true;
        }

        //1、从请求头中获取令牌
        String token = request.getHeader(jwtProperties.getUserTokenName());

        //2、校验令牌
        try {
            log.debug("jwt校验:{}", token);
            if (token == null) {
                response.setStatus(HttpServletResponse.SC_UNAUTHORIZED);
                writeErrorResponse(response, message("user.unlogin"), UNAUTHORIZED);
                return false;
            }
            Jws<Claims> claims = JwtUtil.parseJWT(token,jwtProperties.getUserSecretKey());
            Long userId = Long.valueOf(claims.getPayload().get(JwtClaimsConstant.USER_ID).toString());
            log.debug("当前用户的id：{}", userId);
            UserContext.setUserId(userId);
            // 3、通过，放行
            return true;
        } catch (Exception ex) {
            // 4、不通过，响应401状态码
            response.setStatus(HttpServletResponse.SC_UNAUTHORIZED);
            writeErrorResponse(response, message("user.please.relogin"), UNAUTHORIZED);
            return false;
        }

    }
    // 辅助方法，用于写入错误响应
    private void writeErrorResponse(HttpServletResponse response, String message, int code) {
        response.setContentType("application/json");
        response.setCharacterEncoding("UTF-8");
        try {
            JSONObject error = new JSONObject();
            error.put("code", code);
            error.put("msg", message);
            response.getWriter().write(error.toString());
        } catch (IOException e) {
            log.error("Error writing JSON error response", e);
        }
    }
}
