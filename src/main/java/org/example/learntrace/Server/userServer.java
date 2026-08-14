package org.example.learntrace.Server;

import org.example.learntrace.mybatis.entity.User;
import org.example.learntrace.mybatis.mapper.UsersMapper;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.dao.DuplicateKeyException;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.AuthenticationException;
import org.springframework.security.oauth2.jose.jws.MacAlgorithm;
import org.springframework.security.oauth2.jwt.*;
import org.springframework.stereotype.Service;

import java.time.Instant;
import java.time.temporal.ChronoUnit;
import java.util.HashMap;
import java.util.Map;

@Service
public class userServer {
    @Autowired
    private UsersMapper usersMapper;
    @Autowired
    private AuthenticationManager authenticationManager;
    @Autowired
    private JwtEncoder jwtEncoder;

    public Map<String,String> insertUser(User user) {
        try {
            int response = usersMapper.insert(user);
            if (response == 1) {
                return Map.of("status", "success","message","账户创建成功");
            }
            return Map.of("status", "fail","message","账户创建失败");
        }catch (DuplicateKeyException e) {
            // 唯一索引冲突 = 用户名重复
            return Map.of("status", "fail","message","用户名已存在");
        }catch (DataIntegrityViolationException  e) {
            // 其他的完整性错误（如 NOT NULL）
            return Map.of("status","fail","message","账户创建失败");
        }
    }

    public Map<String, String> deleteUser(String id, Jwt jwt) {
        String userId = jwt.getClaimAsString("id");

        if(!userId.equals(id)) {
            return Map.of("status", "fail","message","只能注销自己的账户！");
        }
        int response = usersMapper.deleteById(id);

        if (response == 1) {
            return Map.of("status", "success","message","注销成功");
        }
        return Map.of("status", "fail","message","注销失败");
    }

    public Map<String, String> login(User user) {

        //待认证凭证
        Authentication loginRequest = new UsernamePasswordAuthenticationToken(user.getName(),user.getPassword());

        try {
            //认证用户
            Authentication authentication = authenticationManager.authenticate(loginRequest);
            User dbUser = usersMapper.selectByName(user.getName());

            //签发JWT
            Instant now = Instant.now();//获取时间
            JwsHeader header = JwsHeader.with(MacAlgorithm.HS256).build();//构造jwt头
            JwtClaimsSet claims = JwtClaimsSet.builder()//添加载荷部分
                    .issuer("learnTrace")//签发者
                    .issuedAt(now)//签发时间
                    .expiresAt(now.plus(1, ChronoUnit.HOURS))//过期时间为一个小时后
                    .subject(authentication.getName())//token的归属方，这里是用户名
                    .claim("scope","ROLE_USER")//自定义字段
                    .claim("id",dbUser.getId())
                    .build();//组装载荷

            //组装token
            String token = jwtEncoder.encode(JwtEncoderParameters.from(header,claims)).getTokenValue();
            return Map.of("status","success","token",token);

        }catch (AuthenticationException e) {
            System.out.println(e);
            return Map.of("status","fail","message","用户名或密码错误");
        }
    }
}
