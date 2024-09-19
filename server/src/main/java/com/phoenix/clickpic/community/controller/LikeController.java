package com.phoenix.clickpic.community.controller;

import java.util.List;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import com.phoenix.clickpic.community.service.LikeService;
import lombok.extern.slf4j.Slf4j;

@RestController
@RequestMapping("/api/like")
@Slf4j
public class LikeController {

    @Autowired
    private LikeService likeService;

    // 좋아요 토글 엔드포인트
    @PostMapping("/{photoId}")
    public ResponseEntity<String> toggleLike(@RequestParam("userId") Long userId, @PathVariable("photoId") Long photoId) {
        log.info("Received toggleLike request - userId: {}, photoId: {}", userId, photoId);

        try {
            // LikeService의 toggleLike 호출
            boolean isLiked = likeService.toggleLike(userId, photoId);
            log.info("Like toggle successful - userId: {}, photoId: {}, isLiked: {}", userId, photoId, isLiked);
            return new ResponseEntity<>(isLiked ? "Liked" : "Unliked", HttpStatus.OK);
        } catch (Exception e) {
            log.error("Error toggling like - userId: {}, photoId: {}, error: {}", userId, photoId, e.getMessage());
            return new ResponseEntity<>("Failed to toggle like", HttpStatus.BAD_REQUEST);
        }
    }

    // 특정 사진에 대한 좋아요 수 가져오기
    @GetMapping("/{photoId}")
    public ResponseEntity<Integer> getLikesCount(@PathVariable("photoId") Long photoId) {
        log.info("Received getLikesCount request - photoId: {}", photoId);
        int count = likeService.getLikesCount(photoId);
        log.info("Likes count for photoId {}: {}", photoId, count);
        return new ResponseEntity<>(count, HttpStatus.OK);
    }

    // 특정 사진에 대해 사용자가 좋아요를 눌렀는지 확인
    @GetMapping("/{photoId}/is-liked")
    public ResponseEntity<Boolean> isLiked(@RequestParam("userId") Long userId, @PathVariable("photoId") Long photoId) {
        log.info("Checking if photo is liked by user - userId: {}, photoId: {}", userId, photoId);
        boolean isLiked = likeService.isPhotoLikedByUser(userId, photoId);
        return new ResponseEntity<>(isLiked, HttpStatus.OK);
    }

    // 사용자가 좋아요한 사진 목록 가져오기
    @GetMapping("/user/{userId}/liked-photos")
    public ResponseEntity<List<Long>> getLikedPhotosByUser(@PathVariable("userId") Long userId) {
        log.info("Received getLikedPhotosByUser request - userId: {}", userId);
        List<Long> likedPhotoIds = likeService.getLikedPhotosByUser(userId);
        log.info("Liked Photo IDs: {}", likedPhotoIds);
        return new ResponseEntity<>(likedPhotoIds, HttpStatus.OK);
    }
    
    @RestController
    @RequestMapping("/api/likescount")
    public class PhotoController {

        @Autowired
        private LikeService likeService;

        @GetMapping("/{photoId}/likesCount")
        public ResponseEntity<Integer> getLikesCount(@PathVariable("photoId") Long photoId) {
            int likesCount = likeService.getLikesCount(photoId);
            return ResponseEntity.ok(likesCount);
        }
    }

}
