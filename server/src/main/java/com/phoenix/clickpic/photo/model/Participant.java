package com.phoenix.clickpic.photo.model;

import lombok.Data;
import lombok.RequiredArgsConstructor;

@Data
@RequiredArgsConstructor
public class Participant {
  private Long userId;
  private Long roomId;
}
