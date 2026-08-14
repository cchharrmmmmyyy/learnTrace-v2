package org.example.learntrace.Controller;


import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.oauth2.jwt.*;
import org.example.learntrace.mybatis.entity.User;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.web.bind.annotation.*;
import java.util.Map;
import java.util.UUID;

@RestController
public class UserController {

    @Autowired
    PasswordEncoder passwordEncoder;
    @Autowired
    private org.example.learntrace.Server.userServer userServer;

    @GetMapping("/health")
    public Map<String,String> health() {
        return Map.of("status", "health");
    }

    //注册
    @PostMapping("/users")
    public ResponseEntity<Map<String,String>> register(@RequestBody User user) {
        //加密密码
        user.setPassword_hash(passwordEncoder.encode(user.getPassword()));
        //创建id,去掉-防止超出数据库的字段大小
        user.setId(UUID.randomUUID().toString().replaceAll("-", ""));
        //定义角色,默认也是user
        user.setRole("user");
        //创建时间
        user.setCreateTime(System.currentTimeMillis());
        Map<String,String> result = userServer.insertUser(user);

        if(result.get("status").equals("success")) {
            return ResponseEntity.ok(result);
        }
        return ResponseEntity.badRequest().body(result);
    }

    //登录
    @PostMapping("/sessions")
    public ResponseEntity<Map<String,String>> login(@RequestBody Map<String,String> user){
        User users = new User();
        users.setName(user.get("name"));
        users.setPassword(user.get("password"));
        Map<String,String> result = userServer.login(users);
        if(result.get("status").equals("success")) {
            return ResponseEntity.ok(result);
        }
        return ResponseEntity.status(401).body(result);
    }

    @DeleteMapping("/users/{id}")
    public ResponseEntity<Map<String,String>> delete(@PathVariable  String id,
                                                     @AuthenticationPrincipal Jwt jwt){
        Map<String,String> response = userServer.deleteUser(id,jwt);
        return ResponseEntity.ok(response);
    }
}
