package com.phoenix.clickpic.community.service;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.List;
import java.util.Map;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import com.phoenix.clickpic.community.mapper.CommunityMapper;

@Service
public class CommunityService {

    @Autowired
    private CommunityMapper communityMapper;

    public List<Map<String, Object>> getCommunityPhotos(String sortType) {
        List<Map<String, Object>> photos = communityMapper.getCommunityPhotos(sortType);

        // 날짜 포맷을 맞춰줍니다.
        DateTimeFormatter formatter = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss");
        for (Map<String, Object> photo : photos) {
            LocalDateTime createdAt = (LocalDateTime) photo.get("created_at");
            if (createdAt != null) {
                photo.put("created_at", createdAt.format(formatter));
            } else {
                photo.put("created_at", "N/A");
            }
        }
        return photos;
    }

    
    // 사진 상세 정보
    public Map<String, Object> getPhotoDetail(Long photoId) {
        Map<String, Object> photoDetail = communityMapper.getPhotoDetail(photoId);

        // 날짜 포맷을 맞춰줍니다.
        if (photoDetail != null) {
            DateTimeFormatter formatter = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss");
            LocalDateTime createdAt = (LocalDateTime) photoDetail.get("created_at");
            if (createdAt != null) {
                photoDetail.put("created_at", createdAt.format(formatter));
            } else {
                photoDetail.put("created_at", "N/A");
            }
        }

        return photoDetail;
    }
}

