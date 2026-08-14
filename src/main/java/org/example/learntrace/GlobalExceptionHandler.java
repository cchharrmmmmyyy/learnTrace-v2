package org.example.learntrace;

import org.springframework.http.HttpStatus;
import org.springframework.http.converter.HttpMessageNotReadableException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;
import javax.servlet.http.HttpServletResponse;
import static org.example.learntrace.ErrorCode.*;


@RestControllerAdvice//拦截所有 Controller 抛出来的异常。
public class GlobalExceptionHandler {

    @ExceptionHandler(BusinessException.class)
    public ApiResponse<?> catchBusinessException(BusinessException ex,HttpServletResponse  response) {
        response.setStatus(ex.getStatus().value());//设置真实的http响应状态码
        return ApiResponse.error(ex.getCode(),ex.getMessage());//包装成我们统一返回格式ApiResponse返回
    }

    @ExceptionHandler(HttpMessageNotReadableException.class)
    public ApiResponse<?> catchHttpMessageNotReadableException(HttpMessageNotReadableException ex,HttpServletResponse  response) {
        response.setStatus(HttpStatus.BAD_REQUEST.value());
        return ApiResponse.error(JSON_FORMAT_ERROR,"请求JSON格式错误，请检查请求体");
    }

    @ExceptionHandler(Exception.class)
    public ApiResponse<?> catchAllOtherException(Exception ex,HttpServletResponse  response) {
        response.setStatus(HttpStatus.INTERNAL_SERVER_ERROR.value());
        return ApiResponse.error(SERVER_ERROR,"Internal Server Error");
    }
}
