package org.example.learntrace.mybatis.entity;

import lombok.Data;

import java.util.List;

//文章题目：对应 lt_article.questions JSON 中的单个题目
//注意：没有 correctAnswer 字段 —— 详情接口天然不泄露正确答案（文档红线）
@Data
public class QuestionVO {
    private String questionId;
    private String type;
    private String text;
    private List<OptionVO> options;
    private String explanation;
    private List<String> sentenceIds;
}
