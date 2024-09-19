package com.phoenix.clickpic.community.controller;

import java.util.List;
import java.util.Map;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import com.phoenix.clickpic.community.service.CommunityService;

@RestController
@RequestMapping("/api/community")
public class CommunityController {

    @Autowired
    private CommunityService communityService;

    @GetMapping
    public ResponseEntity<List<Map<String, Object>>> getCommunityPhotos(
        @RequestParam(value = "sortType", required = false, defaultValue = "date") String sortType) {
        // 공개된 사진들을 가져옵니다.
        List<Map<String, Object>> photos = communityService.getCommunityPhotos(sortType);
        return ResponseEntity.ok(photos);
    }
    

    @GetMapping("/{photoId}")
    public ResponseEntity<Map<String, Object>> getPhotoDetail(@PathVariable("photoId") Long photoId) {
        Map<String, Object> photoDetail = communityService.getPhotoDetail(photoId);
        System.out.println(photoDetail);
        if (photoDetail != null) {
            return ResponseEntity.ok(photoDetail);
        } else {
            return ResponseEntity.status(HttpStatus.NOT_FOUND).body(null);
        }
    }
}
