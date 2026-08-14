package org.example.learntrace;

import lombok.Getter;
import org.springframework.http.HttpStatus;
import static org.example.learntrace.ErrorCode.*;

//业务的异常处理代码
@Getter
public class BusinessException extends RuntimeException{
    private final int code;
    private final HttpStatus status;//Spring 提供的枚举类

    public BusinessException(int code, HttpStatus status,String message) {
        super(message);//调用父类RuntimeException，传入异常消息
        this.code = code;
        this.status = status;
    }

    public BusinessException(HttpStatus status, String message) {
        this(status.value(), status, message);
    }


    public BusinessException(String message) {//只传message，默认500服务器错误
        this(SERVER_ERROR, HttpStatus.INTERNAL_SERVER_ERROR, message);
    }

}
