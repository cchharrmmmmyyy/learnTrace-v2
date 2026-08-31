package org.example.learntrace.mybatis.entity;

import lombok.Data;

import java.util.List;

@Data
public class ArticleSimpleVO {
    private String id;
    private String title;
    private int wordCount;
    private String difficulty;
    private List<String> theme;
}