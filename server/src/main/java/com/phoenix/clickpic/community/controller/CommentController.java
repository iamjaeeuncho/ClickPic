package com.phoenix.clickpic.community.controller;

import java.util.List;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.phoenix.clickpic.community.model.Comment;
import com.phoenix.clickpic.community.service.CommentService;

import lombok.extern.slf4j.Slf4j;

@RestController
@RequestMapping("/api/comments")
@Slf4j
public class CommentController {

    @Autowired
    private CommentService commentService;

    // 특정 사진에 달린 댓글 리스트 조회 (닉네임 포함)
    @GetMapping("/{photoId}")
    public ResponseEntity<List<Comment>> getCommentsByPhoto(@PathVariable("photoId") Long photoId) {
        log.info("Fetching comments for photoId: {}", photoId);
        List<Comment> comments = commentService.getCommentsWithNickname(photoId); 
        log.info("Retrieved {} comments for photoId: {}", comments.size(), photoId);
        return ResponseEntity.ok(comments);
    }

    // 댓글 추가
    @PostMapping
    public ResponseEntity<String> addComment(@RequestBody Comment comment) {
        try {
            commentService.addComment(comment);
            return new ResponseEntity<>("Comment added", HttpStatus.CREATED);
        } catch (Exception e) {
            log.error("Error adding comment: {}", comment, e);
            return new ResponseEntity<>("Error adding comment", HttpStatus.BAD_REQUEST);
        }
    }
}