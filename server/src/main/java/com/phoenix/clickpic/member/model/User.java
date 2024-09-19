package com.phoenix.clickpic.member.model;


import java.time.LocalDateTime;

import lombok.Data;

@Data
public class User {
    private String userId;
    private String nickname;
    private String status;
    private LocalDateTime createdAt;
    private String password;  // 비밀번호 필드 추가
}