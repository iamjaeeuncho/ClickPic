package com.phoenix.clickpic.photolist.service;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.List;
import java.util.Map;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import com.phoenix.clickpic.photolist.mapper.PhotoListMapper;

@Service
public class PhotoListService {
	
    @Autowired
    private PhotoListMapper photoListMapper;

    public List<Map<String, Object>> getUserPhotos(Long userId) {
    	System.out.println(userId);
        List<Map<String, Object>> photos = photoListMapper.getUserPhotos(userId.toString());

        // 날짜를 포맷해서 클라이언트로 전송
        DateTimeFormatter formatter = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss");
        for (Map<String, Object> photo : photos) {
            LocalDateTime createdAt = (LocalDateTime) photo.get("created_at");
            if (createdAt != null) {
                photo.put("created_at", createdAt.format(formatter));
            } else {
                photo.put("created_at", "N/A");  // 기본값 설정
            }
        }

        return photos;
    }
}