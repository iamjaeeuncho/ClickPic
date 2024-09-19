package com.phoenix.clickpic.community.mapper;

import java.util.List;
import java.util.Map;

import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;

@Mapper
public interface CommunityMapper {
    
    List<Map<String, Object>> getCommunityPhotos();
    
    List<Map<String, Object>> getCommunityPhotos(@Param("sortType") String sortType);
    
    // 사진 상세 정보
    Map<String, Object> getPhotoDetail(@Param("photoId") Long photoId);

}
