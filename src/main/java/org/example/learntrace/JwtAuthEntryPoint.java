package org.example.learntrace;

import com.fasterxml.jackson.databind.ObjectMapper;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.security.core.AuthenticationException;
import org.springframework.security.web.AuthenticationEntryPoint;
import org.springframework.stereotype.Component;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;

import static org.example.learntrace.ErrorCode.TOKEN_INVALID;

@Component
public class JwtAuthEntryPoint implements AuthenticationEntryPoint {
    @Autowired
    private ObjectMapper objectMapper;

    @Override
    public void commence(HttpServletRequest request,
                         HttpServletResponse response,
                         AuthenticationException authException) throws IOException{
        // HTTP协议状态码 401
        response.setStatus(HttpStatus.UNAUTHORIZED.value());
        response.setContentType("application/json;charset=UTF-8");

        // JSON内部业务码使用自定义常量1006，而不是401
        ApiResponse<?> errorResp = ApiResponse.error(TOKEN_INVALID, "未登录或Token无效");
        objectMapper.writeValue(response.getOutputStream(), errorResp);
    }
}
