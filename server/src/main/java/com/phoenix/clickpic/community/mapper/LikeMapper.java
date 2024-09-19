package com.phoenix.clickpic.community.mapper;

import java.util.List;

import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;

@Mapper
public interface LikeMapper {
	 int countLikeByUserAndPhoto(@Param("userId") Long userId, @Param("photoId") Long photoId);
	 void insertLike(@Param("userId") Long userId, @Param("photoId") Long photoId);
	 void deleteLike(@Param("userId") Long userId, @Param("photoId") Long photoId);
	 int countLikesByPhoto(@Param("photoId") Long photoId);
	 List<Long> getLikedPhotosByUserId(@Param("userId") Long userId);
}
