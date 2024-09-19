package com.phoenix.clickpic.community.model;

import java.time.LocalDate;

import lombok.Data;

@Data
public class Comment {

    private String commentId;  
    private Long userId;     
    private Long photoId;      
    private String content;  
    private LocalDate createdAt;  
    private String nickname;
}   