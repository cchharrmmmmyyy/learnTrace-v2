package org.example.learntrace.service;

import org.example.learntrace.BusinessException;
import org.example.learntrace.mybatis.entity.User;
import org.example.learntrace.mybatis.mapper.UsersMapper;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.dao.DuplicateKeyException;
import org.springframework.http.HttpStatus;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.AuthenticationException;
import org.springframework.security.oauth2.jose.jws.MacAlgorithm;
import org.springframework.security.oauth2.jwt.*;
import org.springframework.stereotype.Service;
import static org.example.learntrace.ErrorCode.*;

import java.time.Instant;
import java.time.temporal.ChronoUnit;

@Service
public class UserService {
    @Autowired
    private UsersMapper usersMapper;
    @Autowired
    private AuthenticationManager authenticationManager;
    @Autowired
    private JwtEncoder jwtEncoder;

    public void insertUser(User user) {
        try {
            usersMapper.insert(user);
        }catch (DuplicateKeyException e) {
            //用户名重复
            throw new BusinessException(USER_NAME_DUPLICATE,HttpStatus.BAD_REQUEST,"用户名已存在");
        }catch (DataIntegrityViolationException  e) {
            // 其他的完整性错误（如 NOT NULL）
            throw new BusinessException(USER_OPERATE_FAIL,HttpStatus.BAD_REQUEST,"账户创建失败，字段非法");
        }
    }

    public void deleteUser(String id, Jwt jwt) {
        String userId = jwt.getClaimAsString("id");
        if(!userId.equals(id)) {
            throw new BusinessException(USER_NO_PERMISSION,HttpStatus.FORBIDDEN,"只能注销自己的账户！");
        }
        int response = usersMapper.deleteById(id);
        if (response != 1) {
            throw new BusinessException(USER_OPERATE_FAIL,HttpStatus.BAD_REQUEST,"注销失败，用户不存在");
        }
    }

    public String login(User user) {

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

            //组装返回token
            return jwtEncoder.encode(JwtEncoderParameters.from(header,claims)).getTokenValue();

        }catch (AuthenticationException e) {
            throw new BusinessException(USER_LOGIN_FAIL,HttpStatus.UNAUTHORIZED,"用户名或密码错误");
        }
    }
}
