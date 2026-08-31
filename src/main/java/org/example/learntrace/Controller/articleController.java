package org.example.learntrace.Controller;


import org.example.learntrace.ApiResponse;
import org.example.learntrace.mybatis.entity.ArticleDetailVO;
import org.example.learntrace.mybatis.entity.ArticleSimpleVO;
import org.example.learntrace.service.ArticleService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/v1")
public class articleController {
    @Autowired
    ArticleService articleService;

    @GetMapping("/articles")
    public ApiResponse<List<ArticleSimpleVO>> articleList(
            @RequestParam(required = false) String title,
            @RequestParam(required = false) String difficulty,
            @RequestParam(required = false) String theme) {
        //获取文章列表，可按难度和主题筛选
        return ApiResponse.success(articleService.getArticle(title, difficulty, theme));
    }

    @GetMapping("/articles/{articleId}")
    public ApiResponse<ArticleDetailVO> articleDetail(@PathVariable String articleId) {
        //获取文章正文和题目，不返回正确答案
        return ApiResponse.success(articleService.getArticleDetail(articleId));
    }
}
