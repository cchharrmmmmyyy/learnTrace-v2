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
import static org.example.learntrace.ErrorCode.*;

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
                .andExpect(jsonPath("$.data.status").value("health"));
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
                .andExpect(jsonPath("$.code").value(0))
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
                .andExpect(jsonPath("$.code").value(0))
                .andExpect(jsonPath("$.message").value("账户创建成功"));

        mvc.perform(post("/users").contentType(MediaType.APPLICATION_JSON).content(json))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.code").value(USER_NAME_DUPLICATE))
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
                .andExpect(jsonPath("$.code").value(0))
                .andExpect(jsonPath("$.message").value("账户创建成功"));

        user.setName(null);
        json = mapper.writeValueAsString(user);
        mvc.perform(post("/users").contentType(MediaType.APPLICATION_JSON).content(json))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.code").value(USER_OPERATE_FAIL))
                .andExpect(jsonPath("$.message").value("账户创建失败，字段非法"));
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
                .andExpect(jsonPath("$.code").value(0))
                .andExpect(jsonPath("$.message").value("账户创建成功"));
        //登录
        mvc.perform(post("/sessions").contentType(MediaType.APPLICATION_JSON).content(json))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.code").value(0))
                .andExpect(jsonPath("$.data.token").isNotEmpty());
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
                .andExpect(jsonPath("$.code").value(0))
                .andExpect(jsonPath("$.message").value("账户创建成功"));
        //登录
        user.setPassword("111111");//错误密码
        json = mapper.writeValueAsString(user);
        mvc.perform(post("/sessions").contentType(MediaType.APPLICATION_JSON).content(json))
                .andExpect(status().isUnauthorized())
                .andExpect(jsonPath("$.code").value(USER_LOGIN_FAIL))
                .andExpect(jsonPath("$.message").value("用户名或密码错误"));
    }

    @Test
    public void test_login_fail2() throws Exception {
        //注册
        User user = new User();
        user.setName("test0");
        user.setPassword("123456");

        String json = mapper.writeValueAsString(user);
        mvc.perform(post("/users").contentType(MediaType.APPLICATION_JSON).content(json))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.code").value(0))
                .andExpect(jsonPath("$.message").value("账户创建成功"));
        //登录
        user.setName(null);//无用户名
        json = mapper.writeValueAsString(user);
        mvc.perform(post("/sessions").contentType(MediaType.APPLICATION_JSON).content(json))
                .andExpect(status().isUnauthorized())
                .andExpect(jsonPath("$.code").value(USER_LOGIN_FAIL))
                .andExpect(jsonPath("$.message").isNotEmpty());
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
                .andExpect(jsonPath("$.code").value(0))
                .andExpect(jsonPath("$.message").value("账户创建成功"));
        //登录
        MvcResult loginResult =mvc.perform(post("/sessions").contentType(MediaType.APPLICATION_JSON).content(json))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.code").value(0))
                .andExpect(jsonPath("$.data.token").isNotEmpty())
                .andReturn();
        //解析token，拿到id
        String tokenStr = JsonPath.read(loginResult.getResponse().getContentAsString(), "$.data.token");
        Jwt jwt = jwtDecoder.decode(tokenStr);
        String id = jwt.getClaim("id");

        //带上token去删除
        mvc.perform(delete("/users/"+id).header("Authorization", "Bearer " + tokenStr))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.code").value(0))
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
                .andExpect(jsonPath("$.code").value(0))
                .andExpect(jsonPath("$.message").value("账户创建成功"));
        //登录
        MvcResult loginResult =mvc.perform(post("/sessions").contentType(MediaType.APPLICATION_JSON).content(json))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.code").value(0))
                .andExpect(jsonPath("$.data.token").isNotEmpty())
                .andReturn();
        //解析token，拿到id
        String tokenStr = JsonPath.read(loginResult.getResponse().getContentAsString(), "$.data.token");
        Jwt jwt = jwtDecoder.decode(tokenStr);
        String id = jwt.getClaim("id");

        //不带上token去删除
        mvc.perform(delete("/users/"+id))
                .andExpect(status().isUnauthorized())
                .andExpect(jsonPath("$.code").value(TOKEN_INVALID))
                .andExpect(jsonPath("$.message").value("未登录或Token无效"));
    }

    //重复删除（覆盖 deleteById != 1 分支）
    @Test
    public void test_deleteUser_secondDelete_fails() throws Exception {
        //注册
        User user = new User();
        user.setName("test0");
        user.setPassword("123456");

        String json = mapper.writeValueAsString(user);
        mvc.perform(post("/users").contentType(MediaType.APPLICATION_JSON).content(json))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.code").value(0))
                .andExpect(jsonPath("$.message").value("账户创建成功"));
        //登录
        MvcResult loginResult = mvc.perform(post("/sessions").contentType(MediaType.APPLICATION_JSON).content(json))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data.token").isNotEmpty())
                .andReturn();
        //解析token，拿到id
        String tokenStr = JsonPath.read(loginResult.getResponse().getContentAsString(), "$.data.token");
        Jwt jwt = jwtDecoder.decode(tokenStr);
        String id = jwt.getClaim("id");

        //第一次删除成功
        mvc.perform(delete("/users/"+id).header("Authorization", "Bearer " + tokenStr))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.code").value(0))
                .andExpect(jsonPath("$.message").value("注销成功"));
        //第二次删除失败（用户已不存在）
        mvc.perform(delete("/users/"+id).header("Authorization", "Bearer " + tokenStr))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.code").value(USER_OPERATE_FAIL))
                .andExpect(jsonPath("$.message").value("注销失败，用户不存在"));
    }

    //越权删除（A的token删B的账户，覆盖 USER_NO_PERMISSION 分支）
    @Test
    public void test_deleteUser_noPermission() throws Exception {
        //注册A
        User userA = new User();
        userA.setName("testA");
        userA.setPassword("123456");
        String jsonA = mapper.writeValueAsString(userA);
        mvc.perform(post("/users").contentType(MediaType.APPLICATION_JSON).content(jsonA))
                .andExpect(status().isOk());
        //注册B
        User userB = new User();
        userB.setName("testB");
        userB.setPassword("123456");
        String jsonB = mapper.writeValueAsString(userB);
        mvc.perform(post("/users").contentType(MediaType.APPLICATION_JSON).content(jsonB))
                .andExpect(status().isOk());

        //A登录拿token
        MvcResult loginA = mvc.perform(post("/sessions").contentType(MediaType.APPLICATION_JSON).content(jsonA))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data.token").isNotEmpty())
                .andReturn();
        String tokenA = JsonPath.read(loginA.getResponse().getContentAsString(), "$.data.token");

        //B登录拿id
        MvcResult loginB = mvc.perform(post("/sessions").contentType(MediaType.APPLICATION_JSON).content(jsonB))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data.token").isNotEmpty())
                .andReturn();
        String tokenB = JsonPath.read(loginB.getResponse().getContentAsString(), "$.data.token");
        Jwt jwtB = jwtDecoder.decode(tokenB);
        String idB = jwtB.getClaim("id");

        //A拿自己的token去删B
        mvc.perform(delete("/users/"+idB).header("Authorization", "Bearer " + tokenA))
                .andExpect(status().isForbidden())
                .andExpect(jsonPath("$.code").value(USER_NO_PERMISSION))
                .andExpect(jsonPath("$.message").value("只能注销自己的账户！"));
    }
}
