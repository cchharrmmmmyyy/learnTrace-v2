package org.example.learntrace.mybatis.entity;

import lombok.Data;

import java.util.List;

//文章正文结构：对应 lt_article.content JSON 的顶层
@Data
public class ArticleContentVO {
    private List<ParagraphVO> paragraphs;
}
