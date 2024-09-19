package com.phoenix.clickpic.photolist.mapper;

import java.util.List;
import java.util.Map;

import org.apache.ibatis.annotations.Mapper;

@Mapper
public interface PhotoListMapper {
    List<Map<String, Object>> getUserPhotos(String userId);
}
