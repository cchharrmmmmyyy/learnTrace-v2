package org.example.learntrace.mybatis.entity;

import lombok.Data;

import java.util.List;

//文章句子：对应 content JSON 中的 sentence
@Data
public class SentenceVO {
    private String sentenceId;
    private String text;
    private List<WordVO> words;
}
