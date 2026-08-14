package org.example.learntrace.mybatis.entity;

import lombok.Data;

@Data
public class User {
    private String id;
    private String name;
    private String password_hash;
    private String role;
    private Long createTime;
    private String password;
}
