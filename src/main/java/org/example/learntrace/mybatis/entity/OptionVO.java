package org.example.learntrace.mybatis.entity;

import lombok.Data;

//题目选项：对应 questions JSON 中的 option
@Data
public class OptionVO {
    private String optionId;
    private String text;
}
