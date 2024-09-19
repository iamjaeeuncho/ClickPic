package com.phoenix.clickpic.community.service;

import java.util.List;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.phoenix.clickpic.community.mapper.LikeMapper;

@Service
public class LikeService {

    @Autowired
    private LikeMapper likeMapper;

    @Transactional  // 트랜잭션 처리 추가
    public boolean toggleLike(Long userId, Long photoId) {
        int count = likeMapper.countLikeByUserAndPhoto(userId, photoId);

        if (count > 0) {
            // 이미 좋아요가 눌려 있으면, 삭제
            likeMapper.deleteLike(userId, photoId);  // 실제 쿼리 호출
            return false;
        } else {
            // 좋아요가 눌려 있지 않으면, 추가
            likeMapper.insertLike(userId, photoId);  // 실제 쿼리 호출
            return true;
        }
    }

    public int getLikesCount(Long photoId) {
        return likeMapper.countLikesByPhoto(photoId);
    }

    public boolean isPhotoLikedByUser(Long userId, Long photoId) {
        int count = likeMapper.countLikeByUserAndPhoto(userId, photoId);
        return count > 0;  // 좋아요가 존재하면 true 반환
    }

    public List<Long> getLikedPhotosByUser(Long userId) {
        return likeMapper.getLikedPhotosByUserId(userId);
    }
}
