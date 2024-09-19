package com.phoenix.clickpic.photo.controller;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import com.phoenix.clickpic.photo.model.Participant;
import com.phoenix.clickpic.photo.model.Room;
import com.phoenix.clickpic.photo.service.RoomService;

@RestController
@RequestMapping("/room")
public class RoomController {

  @Autowired
  private RoomService roomService;

  @PostMapping("/create")
  public Long createRoom(@RequestBody Room roomRequest) {
    Long userId = roomRequest.getUserId();
    int userCount = roomRequest.getUserCount();
    String roomName = roomRequest.getRoomName();
    String roomSession = roomRequest.getRoomSession();
    String frameId = roomRequest.getFrameId();

    Room room = new Room();
    room.setUserId(userId);
    room.setUserCount(userCount);
    room.setRoomName(roomName);
    room.setRoomSession(roomSession);
    room.setFrameId(frameId);

    Long roomId = roomService.createRoom(room);

    return roomId;
  }

  @PostMapping("/enter")
  public Room enterRoom(@RequestParam("userId") Long userId,
      @RequestParam("roomSession") String roomSession) {

    Room room = roomService.getRoomBySession(roomSession);
    Long roomId = room.getRoomId();

    Participant participant = new Participant();
    participant.setUserId(userId);
    participant.setRoomId(roomId);

    roomService.addParticipant(participant);

    return room;
  }

  @GetMapping("/info")
  public Room getRoomInfo(@RequestParam("roomSession") String roomSession) {
    Room room = roomService.getRoomBySession(roomSession);
    return room;
  }
}
