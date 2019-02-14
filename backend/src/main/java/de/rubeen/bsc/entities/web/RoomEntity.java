package de.rubeen.bsc.entities.web;

import com.fasterxml.jackson.annotation.JsonGetter;

import java.util.Collection;
import java.util.List;

import static java.text.MessageFormat.format;

public class RoomEntity implements Comparable<RoomEntity> {
    private int roomId;
    private String roomName;
    private int roomSize;
    private List<EquipmentEntity> equipments;

    public RoomEntity() {
    }

    public RoomEntity(int roomId, String roomName, int roomSize, List<EquipmentEntity> equipments) {
        this.roomId = roomId;
        this.roomName = roomName;
        this.roomSize = roomSize;
        this.equipments = equipments;
    }

    @JsonGetter
    public int getId() {
        return roomId;
    }

    public void setRoomId(int roomId) {
        this.roomId = roomId;
    }

    @JsonGetter
    public String getName() {
        return roomName;
    }

    public void setRoomName(String roomName) {
        this.roomName = roomName;
    }

    @JsonGetter
    public int getSize() {
        return roomSize;
    }

    public void setRoomSize(int roomSize) {
        this.roomSize = roomSize;
    }

    public List<EquipmentEntity> getEquipments() {
        return equipments;
    }

    @Override
    public boolean equals(Object obj) {
        if (obj instanceof RoomEntity) {
            return ((RoomEntity) obj).roomId == this.roomId;
        }
        return false;
    }

    @Override
    public int compareTo(RoomEntity o) {
        return this.roomId - o.roomId;
    }

    public void setEquipments(Collection<EquipmentEntity> equipmentEntities) {
        this.equipments = List.copyOf(equipmentEntities);
    }

    @Override
    public String toString() {
        return format("room: [id: {0}, name: {1}, size: {2}, equipments: {3}]",
                roomId, roomName, roomSize, equipments);
    }

    public static class EquipmentEntity {
        private int equipId;
        private String equipName;

        public EquipmentEntity() {
        }

        public EquipmentEntity(int equipId, String equipName) {
            this.equipId = equipId;
            this.equipName = equipName;
        }

        @JsonGetter
        public int getId() {
            return equipId;
        }

        public void setEquipId(int equipId) {
            this.equipId = equipId;
        }

        @JsonGetter
        public String getName() {
            return equipName;
        }

        public void setEquipName(String equipName) {
            this.equipName = equipName;
        }

        @Override
        public String toString() {
            return format("[id: {0}, name: {1}]", equipId, equipName);
        }
    }
}
