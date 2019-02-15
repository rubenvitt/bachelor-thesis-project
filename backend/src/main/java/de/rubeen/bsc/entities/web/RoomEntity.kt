package de.rubeen.bsc.entities.web

import com.fasterxml.jackson.annotation.JsonGetter

import java.text.MessageFormat.format

class RoomEntity : Comparable<RoomEntity> {
    var id: Int = 0
        private set
    var name: String? = null
        private set
    var size: Int = 0
        private set
    var equipments: List<EquipmentEntity>? = null
        private set

    constructor() {}

    constructor(roomId: Int, roomName: String, roomSize: Int, equipments: List<EquipmentEntity>) {
        this.id = roomId
        this.name = roomName
        this.size = roomSize
        this.equipments = equipments
    }

    fun setRoomId(roomId: Int) {
        this.id = roomId
    }

    fun setRoomName(roomName: String) {
        this.name = roomName
    }

    fun setRoomSize(roomSize: Int) {
        this.size = roomSize
    }

    override fun equals(obj: Any?): Boolean {
        return if (obj is RoomEntity) {
            obj.id == this.id
        } else false
    }

    override fun compareTo(o: RoomEntity): Int {
        return this.id - o.id
    }

    fun setEquipments(equipmentEntities: Collection<EquipmentEntity>) {
        this.equipments = equipmentEntities.toList()
    }

    override fun toString(): String {
        return "(id: $id, name: $name, size: $size, equipments: $equipments)"
    }

    override fun hashCode(): Int {
        var result = id
        result = 31 * result + (name?.hashCode() ?: 0)
        result = 31 * result + size
        result = 31 * result + (equipments?.hashCode() ?: 0)
        return result
    }

    class EquipmentEntity {
        var id: Int = 0
            private set
        var name: String? = null
            private set

        constructor() {}

        constructor(equipId: Int, equipName: String) {
            this.id = equipId
            this.name = equipName
        }

        fun setEquipId(equipId: Int) {
            this.id = equipId
        }

        fun setEquipName(equipName: String) {
            this.name = equipName
        }

        override fun toString(): String {
            return "(id: $id, name: $name)"
        }
    }
}
