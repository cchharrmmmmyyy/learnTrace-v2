package org.example.learntrace.mybatis.entity;

import lombok.Data;

import java.util.List;

//文章详情响应对象：GET /api/v1/articles/{articleId}
@Data
public class ArticleDetailVO {
    private String id;
    private String title;
    private Integer wordCount;
    private String difficulty;
    private List<String> theme;
    private ArticleContentVO content;
    private List<QuestionVO> questions;
}
