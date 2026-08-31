package org.example.learntrace.mybatis.entity;

import lombok.Data;

//文章单词：对应 content JSON 中的 word
@Data
public class WordVO {
    private String wordId;
    private String text;
    //文档预留的可选字段，后续按需增加：phonetic（音标）、pos（词性）、lemma（原形）
}
