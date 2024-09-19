package com.phoenix.clickpic.photo.model;

import java.time.LocalDateTime;

import lombok.Data;
import lombok.RequiredArgsConstructor;

@Data
@RequiredArgsConstructor
public class Photo {
  private Long photoId;
  private Long roomId;
  private String completePhoto;
  private LocalDateTime createdAt;
  private String note;
  private String originPhoto;
  private LocalDateTime updatedAt;
  private String isPublic;
}
