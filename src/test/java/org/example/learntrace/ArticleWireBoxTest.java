package org.example.learntrace;


import com.fasterxml.jackson.databind.ObjectMapper;
import com.jayway.jsonpath.JsonPath;
import org.example.learntrace.mybatis.entity.User;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.MvcResult;
import org.springframework.transaction.annotation.Transactional;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@SpringBootTest
@AutoConfigureMockMvc
@Transactional
public class ArticleWireBoxTest {

    @Autowired
    private MockMvc mvc;
    @Autowired
    private ObjectMapper mapper;

    //集成的注册登录
    private String login() throws Exception{
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
        MvcResult response =  mvc.perform(post("/sessions").contentType(MediaType.APPLICATION_JSON).content(json))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.code").value(0))
                .andExpect(jsonPath("$.data.token").isNotEmpty())
                .andReturn();
        String body = response.getResponse().getContentAsString();
        return JsonPath.read(body,"$.data.token");
    }


    //测试article列表接口
    @Test
    public void test_ArticleList_success() throws Exception{
        String token = login();

        //获取文章
        mvc.perform(get("/api/v1/articles")
                .param("title","The History of Coffee")
                .param("difficulty","medium")
                .param("theme","history")
                .header("Authorization", "Bearer " + token))
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$.code").value(0))
                    .andExpect(jsonPath("$.message").value("success"))
                    // data 是数组，第 0 篇就是预存的 art_001
                    .andExpect(jsonPath("$.data[0].id").value("art_001"))
                    .andExpect(jsonPath("$.data[0].title").value("The History of Coffee"))
                    .andExpect(jsonPath("$.data[0].difficulty").value("medium"))
                    .andExpect(jsonPath("$.data[0].wordCount").value(1200))
                    .andExpect(jsonPath("$.data[0].theme[0]").value("history"));
    }

    @Test
    public void test_ArticleList_fail() throws Exception{
        String token = login();

        //获取文章
        mvc.perform(get("/api/v1/articles")
                        .param("title","The History of Coffee")
                        .param("difficulty","xxx")
                        .param("theme","history")
                        .header("Authorization", "Bearer " + token))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.code").value(1008))
                .andExpect(jsonPath("$.message").value("难度参数不合法"));
    }

    @Test
    public void test_ArticleDetail_success() throws Exception{
        String token = login();
        mvc.perform(get("/api/v1/articles/art_001").header("Authorization", "Bearer " + token))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.code").value(0))
                // —— 基础字段（对照 lt_article 行）——
                .andExpect(jsonPath("$.data.id").value("art_001"))
                .andExpect(jsonPath("$.data.title").value("The History of Coffee"))
                .andExpect(jsonPath("$.data.wordCount").value(1200))
                .andExpect(jsonPath("$.data.difficulty").value("medium"))

                // —— theme 列表（对照 lt_article_theme + lt_theme 关联）——
                .andExpect(jsonPath("$.data.theme[0]").value("history"))

                // —— content 三层结构（对照 content JSON）——
                .andExpect(jsonPath("$.data.content.paragraphs[0].paragraphId").value("p1"))
                .andExpect(jsonPath("$.data.content.paragraphs[0].sentences[0].sentenceId").value("p1_s1"))
                .andExpect(jsonPath("$.data.content.paragraphs[0].sentences[0].text").value("Coffee was first discovered in Ethiopia."))
                .andExpect(jsonPath("$.data.content.paragraphs[0].sentences[0].words[0].wordId").value("p1_s1_w1"))
                .andExpect(jsonPath("$.data.content.paragraphs[0].sentences[0].words[0].text").value("Coffee"))
                .andExpect(jsonPath("$.data.content.paragraphs[0].sentences[0].words[1].text").value("was"))

                // —— questions（对照 questions JSON）——
                .andExpect(jsonPath("$.data.questions[0].questionId").value("q1"))
                .andExpect(jsonPath("$.data.questions[0].type").value("single_choice"))
                .andExpect(jsonPath("$.data.questions[0].text").value("Where was coffee first discovered?"))
                .andExpect(jsonPath("$.data.questions[0].options[0].optionId").value("q1_a"))
                .andExpect(jsonPath("$.data.questions[0].options[0].text").value("Brazil"))
                .andExpect(jsonPath("$.data.questions[0].options[1].text").value("Ethiopia"))
                .andExpect(jsonPath("$.data.questions[0].explanation").value("The passage states coffee was discovered in Ethiopia."))
                .andExpect(jsonPath("$.data.questions[0].sentenceIds[0]").value("p1_s1"))

                        // —— 防泄露断言：正确答案绝不能出现在返回里 ——
                        .andExpect(jsonPath("$.data.questions[0].correctAnswer").doesNotExist());
    }

    @Test
    public void test_ArticleDetail_fail() throws Exception{
        String token = login();
        mvc.perform(get("/api/v1/articles/art_004").header("Authorization", "Bearer " + token))
                .andExpect(status().isNotFound())
                .andExpect(jsonPath("$.message").value("文章不存在"));

    }

    @Test
    public void test_ArticleDetail_fail2() throws Exception{
        String token = login();
        mvc.perform(get("/api/v1/articles/art_002").header("Authorization", "Bearer " + token))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.code").value(0))
                .andExpect(jsonPath("$.data.id").value("art_002"))
                .andExpect(jsonPath("$.data.title").value("The Solar System"))
                .andExpect(jsonPath("$.data.questions").isEmpty());;
    }

    @Test
    public void test_ArticleDetail_fail3() throws Exception{
        String token = login();
        mvc.perform(get("/api/v1/articles/art_003").header("Authorization", "Bearer " + token))
                .andExpect(status().isInternalServerError())
                .andExpect(jsonPath("$.message").value("文章数据解析失败"));

    }




}
