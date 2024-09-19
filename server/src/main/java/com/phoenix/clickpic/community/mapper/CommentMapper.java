package com.phoenix.clickpic.community.mapper;

import java.util.List;

import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;

import com.phoenix.clickpic.community.model.Comment;

@Mapper
public interface CommentMapper {

    // 특정 사진에 대한 댓글 조회 (닉네임 포함)
    List<Comment> getCommentsWithNickname(@Param("photoId") Long photoId);

    // 댓글 추가
    void insertComment(Comment comment);
}