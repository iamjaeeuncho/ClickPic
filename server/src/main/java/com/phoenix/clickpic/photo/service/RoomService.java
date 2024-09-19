package com.phoenix.clickpic.photo.service;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import com.phoenix.clickpic.photo.mapper.RoomMapper;
import com.phoenix.clickpic.photo.model.Participant;
import com.phoenix.clickpic.photo.model.Room;

@Service
public class RoomService {

    @Autowired
    private RoomMapper roomMapper;

    public Long createRoom(Room room) {
        roomMapper.insertRoom(room);
        return room.getRoomId();
    }
    
    public Room getRoomBySession(String roomSession) {
      Room room = roomMapper.findBySession(roomSession);
      return room;
  }

    public void addParticipant(Participant participant) {
      roomMapper.addParticipant(participant);
    }

}
