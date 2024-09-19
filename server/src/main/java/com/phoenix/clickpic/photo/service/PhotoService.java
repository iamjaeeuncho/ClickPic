package com.phoenix.clickpic.photo.service;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import com.phoenix.clickpic.photo.mapper.PhotoMapper;
import com.phoenix.clickpic.photo.model.Photo;

@Service
public class PhotoService {
  
  @Autowired
  private PhotoMapper photoMapper;

  public void saveOriginPhoto(Photo photo) {
    photoMapper.insertOriginPhoto(photo);
  }

  public Photo getPhotoBySession(String roomSession) {
    Photo photo = photoMapper.findBySession(roomSession);
    return photo;
  }

  public void updateCompletePhoto(Long roomId, String completePhoto) {
    Map<String, Object> params = new HashMap<>();
    params.put("roomId", roomId);
    params.put("completePhoto", completePhoto);
    photoMapper.updateCompletePhoto(params);
  }

  public void updateFinalPhoto(Long roomId, String isPublic, String note) {
    Map<String, Object> params = new HashMap<>();
    params.put("roomId", roomId);
    params.put("isPublic", isPublic);
    params.put("note", note);
    photoMapper.updateFinalPhoto(params);
  }

  public List<Photo> selectHotPhotos() {
    return photoMapper.selectHotPhotos();
  }

}
