package org.example.learntrace.mybatis.entity;

import lombok.Data;

import java.util.List;

//文章数据库实体：对应 lt_article 表
//content / questions 是 JSON 列，MyBatis 原样读成 String，由 Service 解析成 VO
@Data
public class ArticleDetail {
    private String id;
    private String title;
    private Integer wordCount;
    private String difficulty;
    private List<String> theme;
    private String content;
    private String questions;
}
