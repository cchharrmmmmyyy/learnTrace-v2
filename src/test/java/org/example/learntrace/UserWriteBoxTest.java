package org.example.learntrace;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.jayway.jsonpath.JsonPath;
import org.example.learntrace.mybatis.entity.User;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.security.oauth2.jwt.Jwt;
import org.springframework.security.oauth2.jwt.JwtDecoder;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.MvcResult;
import org.springframework.transaction.annotation.Transactional;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@SpringBootTest
@AutoConfigureMockMvc
@Transactional //每个测试跑完就回滚，数据不残留
public class UserWriteBoxTest {
    @Autowired
    private MockMvc mvc;
    @Autowired
    private ObjectMapper mapper;
    @Qualifier("jwtDecoder")
    @Autowired
    private JwtDecoder jwtDecoder;

    //health
    @Test
    public void test_health_return200() throws Exception {
        mvc.perform(get("/health"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.status").value("health"));
    }

    //register
    @Test
    public void test_register_success() throws Exception {
        User user = new User();
        user.setName("test0");
        user.setPassword("123456");

        String json = mapper.writeValueAsString(user);
        mvc.perform(post("/users").contentType(MediaType.APPLICATION_JSON).content(json))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.status").value("success"))
                .andExpect(jsonPath("$.message").value("账户创建成功"));
    }

    @Test
    public void test_register_fail() throws Exception {
        User user = new User();
        user.setName("test0");
        user.setPassword("123456");
        String json = mapper.writeValueAsString(user);

        mvc.perform(post("/users").contentType(MediaType.APPLICATION_JSON).content(json))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.status").value("success"))
                .andExpect(jsonPath("$.message").value("账户创建成功"));

        mvc.perform(post("/users").contentType(MediaType.APPLICATION_JSON).content(json))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.status").value("fail"))
                .andExpect(jsonPath("$.message").value("用户名已存在"));
    }

    @Test
    public void test_register_fail2() throws Exception {
        User user = new User();
        user.setName("test0");
        user.setPassword("123456");

        String json = mapper.writeValueAsString(user);
        mvc.perform(post("/users").contentType(MediaType.APPLICATION_JSON).content(json))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.status").value("success"))
                .andExpect(jsonPath("$.message").value("账户创建成功"));

        user.setName(null);
        json = mapper.writeValueAsString(user);
        mvc.perform(post("/users").contentType(MediaType.APPLICATION_JSON).content(json))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.status").value("fail"))
                .andExpect(jsonPath("$.message").value("账户创建失败"));
    }

    //login
    @Test
    public void test_login_success() throws Exception {
        //注册
        User user = new User();
        user.setName("test0");
        user.setPassword("123456");

        String json = mapper.writeValueAsString(user);
        mvc.perform(post("/users").contentType(MediaType.APPLICATION_JSON).content(json))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.status").value("success"))
                .andExpect(jsonPath("$.message").value("账户创建成功"));
        //登录
        mvc.perform(post("/sessions").contentType(MediaType.APPLICATION_JSON).content(json))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.status").value("success"))
                .andExpect(jsonPath("$.token").isNotEmpty());
    }

    @Test
    public void test_login_fail() throws Exception {
        //注册
        User user = new User();
        user.setName("test0");
        user.setPassword("123456");

        String json = mapper.writeValueAsString(user);
        mvc.perform(post("/users").contentType(MediaType.APPLICATION_JSON).content(json))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.status").value("success"))
                .andExpect(jsonPath("$.message").value("账户创建成功"));
        //登录
        user.setPassword("111111");//错误密码
        json = mapper.writeValueAsString(user);
        mvc.perform(post("/sessions").contentType(MediaType.APPLICATION_JSON).content(json))
                .andExpect(status().isUnauthorized())
                .andExpect(jsonPath("$.status").value("fail"))
                .andExpect(jsonPath("$.message").value("用户名或密码错误"));
    }

    @Test
    public void test_deleteUser_success() throws Exception {
        //注册
        User user = new User();
        user.setName("test0");
        user.setPassword("123456");

        String json = mapper.writeValueAsString(user);
        mvc.perform(post("/users").contentType(MediaType.APPLICATION_JSON).content(json))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.status").value("success"))
                .andExpect(jsonPath("$.message").value("账户创建成功"));
        //登录
        MvcResult loginResult =mvc.perform(post("/sessions").contentType(MediaType.APPLICATION_JSON).content(json))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.status").value("success"))
                .andExpect(jsonPath("$.token").isNotEmpty())
                .andReturn();
        //解析token，拿到id
        String tokenStr = JsonPath.read(loginResult.getResponse().getContentAsString(), "$.token");
        Jwt jwt = jwtDecoder.decode(tokenStr);
        String id = jwt.getClaim("id");

        //带上token去删除
        mvc.perform(delete("/users/"+id).header("Authorization", "Bearer " + tokenStr))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.status").value("success"))
                .andExpect(jsonPath("$.message").value("注销成功"));
    }

    //不带token
    @Test
    public void test_deleteUser_fail() throws Exception {
        //注册
        User user = new User();
        user.setName("test0");
        user.setPassword("123456");

        String json = mapper.writeValueAsString(user);
        mvc.perform(post("/users").contentType(MediaType.APPLICATION_JSON).content(json))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.status").value("success"))
                .andExpect(jsonPath("$.message").value("账户创建成功"));
        //登录
        MvcResult loginResult =mvc.perform(post("/sessions").contentType(MediaType.APPLICATION_JSON).content(json))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.status").value("success"))
                .andExpect(jsonPath("$.token").isNotEmpty())
                .andReturn();
        //解析token，拿到id
        String tokenStr = JsonPath.read(loginResult.getResponse().getContentAsString(), "$.token");
        Jwt jwt = jwtDecoder.decode(tokenStr);
        String id = jwt.getClaim("id");

        //不带上token去删除
        mvc.perform(delete("/users/"+id))
                .andExpect(status().isUnauthorized());
    }
}
