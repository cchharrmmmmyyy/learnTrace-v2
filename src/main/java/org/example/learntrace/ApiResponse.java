package org.example.learntrace;

import lombok.Data;

//用来规范返回数据的
@Data
public class ApiResponse<T> {
    private int code;
    private String message;
    private T data;

    public static <T> ApiResponse<T> success(String message,T data) {
        ApiResponse<T> rep =  new ApiResponse<>();
        rep.setCode(0);
        rep.setMessage(message);
        rep.setData(data);
        return rep;
    }
    public static <T> ApiResponse<T> success(T data) {
        return success("success",data);
    }
    public static <T> ApiResponse<T> success(String message) {
        return success(message,null);
    }
    public static <T> ApiResponse<T> success() {
        return success("success",null);
    }

    public static <T> ApiResponse<T> error(int code,String message,T data) {
        ApiResponse<T> rep = new ApiResponse<>();
        rep.setCode(code);
        rep.setMessage(message);
        rep.setData(data);
        return rep;
    }
    public static <T> ApiResponse<T> error(int code,String message) {
        return error(code,message,null);
    }
    public static <T> ApiResponse<T> error(int code) {
        return error(code,"error",null);
    }
    public static <T> ApiResponse<T> error(String message) {
        return error(500,message,null);//500代表Internal Server Error 服务器内部错误
    }
    public static <T> ApiResponse<T> error() {
        return error(500,"error",null);
    }
}
