package com.phoenix.clickpic.community.service;

import java.util.List;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import com.phoenix.clickpic.community.mapper.CommentMapper;
import com.phoenix.clickpic.community.model.Comment;
import com.phoenix.clickpic.member.mapper.UserMapper;

import lombok.extern.slf4j.Slf4j;

@Service
@Slf4j
public class CommentService {

    @Autowired
    private CommentMapper commentMapper;

    @Autowired
    private UserMapper userMapper;  // 닉네임을 가져오기 위한 매퍼

    // 특정 사진에 대한 댓글 조회 (닉네임 포함)
    public List<Comment> getCommentsWithNickname(Long photoId) {
        log.info("Fetching comments with nicknames from DB for photoId: {}", photoId);
        List<Comment> comments = commentMapper.getCommentsWithNickname(photoId);
        log.info("Fetched {} comments from DB for photoId: {}", comments.size(), photoId);
        return comments;
    }

    // 댓글 추가
    public void addComment(Comment comment) {
        // 댓글 추가 전에 닉네임 가져오기
        String nickname = userMapper.getNicknameByUserId(comment.getUserId());
        comment.setNickname(nickname);

        log.info("Inserting comment into DB: {}", comment);
        commentMapper.insertComment(comment);
    }
}