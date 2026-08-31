package org.example.learntrace.Controller;


import org.example.learntrace.ApiResponse;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.oauth2.jwt.*;
import org.example.learntrace.mybatis.entity.User;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.web.bind.annotation.*;
import java.util.Map;
import java.util.UUID;

@RestController
public class UserController {

    @Autowired
    PasswordEncoder passwordEncoder;
    @Autowired
    private org.example.learntrace.service.UserService userServer;

    @GetMapping("/health")
    public ApiResponse<?> health() {
        return ApiResponse.success(Map.of("status", "health"));
    }

    //注册
    @PostMapping("/users")
    public ApiResponse<User> register(@RequestBody User user) {
        //加密密码
        user.setPassword_hash(passwordEncoder.encode(user.getPassword()));
        //创建id,去掉-防止超出数据库的字段大小
        user.setId(UUID.randomUUID().toString().replaceAll("-", ""));
        //定义角色,默认也是user
        user.setRole("user");
        //创建时间
        user.setCreateTime(System.currentTimeMillis());
        userServer.insertUser(user);
        String message = "账户创建成功";

        return ApiResponse.message(message);
    }

    //登录
    @PostMapping("/sessions")
    public ApiResponse<Map<String,String>> login(@RequestBody Map<String,String> user){
        User users = new User();
        users.setName(user.get("name"));
        users.setPassword(user.get("password"));
        String token = userServer.login(users);

        return ApiResponse.success("登录成功", Map.of("token", token));
    }
    //删除
    @DeleteMapping("/users/{id}")
    public ApiResponse<?> delete(@PathVariable  String id, @AuthenticationPrincipal Jwt jwt){
        userServer.deleteUser(id,jwt);
        String message = "注销成功";
        return ApiResponse.message(message);
    }
}
