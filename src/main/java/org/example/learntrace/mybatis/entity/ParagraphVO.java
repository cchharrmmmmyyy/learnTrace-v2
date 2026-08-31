package org.example.learntrace.mybatis.entity;

import lombok.Data;

import java.util.List;

//文章段落：对应 content JSON 中的 paragraph
@Data
public class ParagraphVO {
    private String paragraphId;
    private List<SentenceVO> sentences;
}
