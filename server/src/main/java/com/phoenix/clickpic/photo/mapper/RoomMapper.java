package com.phoenix.clickpic.photo.mapper;

import org.apache.ibatis.annotations.Mapper;
import com.phoenix.clickpic.photo.model.Participant;
import com.phoenix.clickpic.photo.model.Room;

@Mapper
public interface RoomMapper {
  
    void insertRoom(Room room);
    
    Room findBySession(String roomSession);

    void addParticipant(Participant participant);

}