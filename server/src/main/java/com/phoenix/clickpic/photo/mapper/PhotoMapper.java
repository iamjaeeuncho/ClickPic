package com.phoenix.clickpic.photo.mapper;

import java.util.List;
import java.util.Map;
import org.apache.ibatis.annotations.Mapper;
import com.phoenix.clickpic.photo.model.Photo;

@Mapper
public interface PhotoMapper {

  public void insertOriginPhoto(Photo photo);

  public Photo findBySession(String roomSession);

  public void updateCompletePhoto(Map<String, Object> params);
  
  public void updateFinalPhoto(Map<String, Object> params);

  public List<Photo> selectHotPhotos();
  
}
