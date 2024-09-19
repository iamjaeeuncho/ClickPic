package com.phoenix.clickpic.photo.model;

import java.time.LocalDateTime;
import lombok.Data;
import lombok.RequiredArgsConstructor;

@Data
@RequiredArgsConstructor
public class Room {
    private Long roomId;
    private String frameId;
    private Long userId;
    private Integer userCount;
    private String roomSession;
    private String roomName;
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;
}
