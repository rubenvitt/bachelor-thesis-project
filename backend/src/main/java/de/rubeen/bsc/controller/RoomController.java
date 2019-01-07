package de.rubeen.bsc.controller;

import de.rubeen.bsc.entities.web.RoomEntity;
import de.rubeen.bsc.service.RoomService;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.util.Collection;

@RestController
@RequestMapping(value = "/rooms")
public class RoomController {
    private final RoomService roomService;

    public RoomController(RoomService roomService) {
        this.roomService = roomService;
    }

    @RequestMapping(value = "/all", method = RequestMethod.GET)
    public Collection<RoomEntity> allRooms(@RequestParam("user_id") String userID) {
        return roomService.getAllRooms();
    }

    @RequestMapping(value = "/equipments", method = RequestMethod.GET)
    public Collection<RoomEntity.EquipmentEntity> allEquipments(@RequestParam("user_id") String userID) {
        return roomService.getAllEquipments();
    }
}
