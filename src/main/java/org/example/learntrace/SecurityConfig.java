package org.example.learntrace;

import com.nimbusds.jose.jwk.source.ImmutableSecret;
import com.nimbusds.jose.proc.SecurityContext;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.config.Customizer;
import org.springframework.security.config.annotation.authentication.configuration.AuthenticationConfiguration;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.config.annotation.web.configurers.AbstractHttpConfigurer;
import org.springframework.security.config.http.SessionCreationPolicy;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.oauth2.jwt.JwtDecoder;
import org.springframework.security.oauth2.jwt.JwtEncoder;
import org.springframework.security.oauth2.jwt.NimbusJwtDecoder;
import org.springframework.security.oauth2.jwt.NimbusJwtEncoder;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.security.web.util.matcher.AntPathRequestMatcher;

import javax.crypto.spec.SecretKeySpec;

@Configuration
@EnableWebSecurity
public class SecurityConfig {
    @Bean
    //网页访问规则,返回一整套安全过滤器链
    public SecurityFilterChain securityFilterChain(HttpSecurity http) throws Exception {
        http
                .csrf(AbstractHttpConfigurer::disable)//关闭CSRF
                .sessionManagement(session -> session
                        .sessionCreationPolicy(SessionCreationPolicy.STATELESS))// 不创建session，无状态
                .authorizeHttpRequests(auth ->auth
                        .requestMatchers(
                                new AntPathRequestMatcher("/health","GET"),
                                new AntPathRequestMatcher("/users","POST"),
                                new AntPathRequestMatcher("/sessions","POST")
                                ).permitAll()//放行完全不验证的URL
                        .anyRequest().authenticated())//剩下的全部验证
                .oauth2ResourceServer(oauth2 -> oauth2.jwt(Customizer.withDefaults()));//启用jwt
        return http.build();//返回组装好的链条
    }

    @Bean
    //认证管理器：用来校验账号密码
    //这个 Bean 的作用就是把 Spring 组装好的认证管理器拿出来，放到容器里，供你的登录接口使用。
    public AuthenticationManager authenticationManager(AuthenticationConfiguration configuration) throws Exception {
        return configuration.getAuthenticationManager();
    }

    @Bean
    //密码加密工具
    public PasswordEncoder passwordEncoder() {
        return new BCryptPasswordEncoder();
    }

    @Bean
    //JWT 解码器
    public JwtDecoder jwtDecoder(){
        //密钥（>=32字节）
        String secret = "this-is-a-32-byte-secret-key!!!123";
        SecretKeySpec key = new SecretKeySpec(secret.getBytes(), "HmacSHA256");//把密钥变为字节，然后标明使用的算法
        return NimbusJwtDecoder
                .withSecretKey(key)
                .build();
    }

    @Bean
    public JwtEncoder jwtEncoder(){
        //签发JWT
        String secret = "this-is-a-32-byte-secret-key!!!123";
        SecretKeySpec key = new SecretKeySpec(secret.getBytes(), "HmacSHA256");
        //Nimbus 库提供的类，把裸密钥包装成 JWK（JSON Web Key，密钥的标准化 JSON 格式）
        ImmutableSecret<SecurityContext> jwkSource = new ImmutableSecret<>(key);
        return new NimbusJwtEncoder(jwkSource);
    }
}
