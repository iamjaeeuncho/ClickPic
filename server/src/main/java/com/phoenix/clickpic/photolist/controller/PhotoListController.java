package com.phoenix.clickpic.photolist.controller;

import java.util.List;
import java.util.Map;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import com.phoenix.clickpic.photolist.service.PhotoListService;

@RestController
@RequestMapping("/api/photolist")
public class PhotoListController {
	
    @Autowired
    private PhotoListService photoListService;

    @GetMapping
    public ResponseEntity<List<Map<String, Object>>> getUserPhotos(@RequestParam("userId") Long userId) {
        System.out.println("Received userId: " + userId);

        // userId를 사용하여 사진 정보 조회
        List<Map<String, Object>> photos = photoListService.getUserPhotos(userId);

        return ResponseEntity.ok(photos);
    }
}