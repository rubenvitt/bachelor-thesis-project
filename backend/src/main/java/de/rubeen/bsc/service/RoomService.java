package de.rubeen.bsc.service;

import com.google.common.collect.Multimap;
import com.google.common.collect.MultimapBuilder;
import de.rubeen.bsc.entities.web.RoomEntity;
import org.jooq.Record;
import org.jooq.Result;
import org.modelmapper.ModelMapper;
import org.modelmapper.convention.NameTokenizers;
import org.modelmapper.jooq.RecordValueReader;
import org.springframework.stereotype.Service;

import java.sql.SQLException;
import java.util.Collection;
import java.util.LinkedList;
import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;

import static com.google.common.base.Preconditions.checkNotNull;
import static de.rubeen.bsc.entities.db.tables.Room.ROOM;
import static de.rubeen.bsc.entities.db.tables.RoomEquipment.ROOM_EQUIPMENT;
import static de.rubeen.bsc.entities.db.tables.RoomRoomEquipment.ROOM_ROOM_EQUIPMENT;

@Service
public class RoomService extends LoggableService {
    private final ModelMapper modelMapper = new ModelMapper();
    private final DatabaseService databaseService;

    public RoomService(DatabaseService databaseService) throws SQLException {
        this.databaseService = databaseService;
        modelMapper.getConfiguration().addValueReader(new RecordValueReader());
        modelMapper.getConfiguration().setSourceNameTokenizer(NameTokenizers.UNDERSCORE);

    }

    public Collection<RoomEntity.EquipmentEntity> getAllEquipments() {
        return databaseService.getContext()
                .select()
                .from(ROOM_EQUIPMENT)
                .fetch().parallelStream()
                .map(record -> modelMapper.map(record, RoomEntity.EquipmentEntity.class))
                .collect(Collectors.toCollection(LinkedList::new));
    }

    public Collection<RoomEntity> getAllRooms() {
        try {
            return getRoomEntities();
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return List.of(new RoomEntity(0, "test", 3, List.of(new RoomEntity.EquipmentEntity(1, "Equipment"))));
    }

    private Set<RoomEntity> getRoomEntities() throws SQLException {
        Multimap<RoomEntity, RoomEntity.EquipmentEntity> roomEntityEquipmentEntityMultimap =
                MultimapBuilder.treeKeys().arrayListValues().build();

        final Result<Record> records = databaseService.getContext()
                .select()
                .from(ROOM)
                .join(ROOM_ROOM_EQUIPMENT).onKey()
                .join(ROOM_EQUIPMENT).onKey()
                .fetch();

        records.forEach(record -> {
            RoomEntity roomEntity = modelMapper.map(record, RoomEntity.class);
            RoomEntity.EquipmentEntity equipmentEntity = modelMapper.map(record, RoomEntity.EquipmentEntity.class);
            roomEntityEquipmentEntityMultimap.put(roomEntity, equipmentEntity);
        });
        roomEntityEquipmentEntityMultimap.asMap().forEach(RoomEntity::setEquipments);
        System.out.println(roomEntityEquipmentEntityMultimap.keySet().size());
        return roomEntityEquipmentEntityMultimap.keySet();

        /*@SuppressWarnings("unchecked")
        List<RoomEntity> roomEntities = (List<RoomEntity>) mapper.stream(fetch)
                .filter(o -> {
                    LOG.info("o is instanceof {}", o.getClass());
                    return (o instanceof RoomEntity);
                })
                .collect(Collectors.toList());
        LOG.warn("size: {}", roomEntities.size());
        roomEntities.forEach(entity -> {
            LOG.info("{} with {} equipments", entity.getName(), entity.getEquipments());
        });
        LOG.info("result: {}", fetch);*/
    }

    public Collection<RoomEntity.EquipmentEntity> getEquipments(Integer roomId) {
        checkNotNull(roomId);
        return databaseService.getContext()
                .select()
                .from(ROOM)
                .innerJoin(ROOM_ROOM_EQUIPMENT).onKey()
                .innerJoin(ROOM_EQUIPMENT).onKey()
                .where(ROOM.ROOM_ID.eq(roomId))
                .fetch().parallelStream()
                .map(record -> modelMapper.map(record, RoomEntity.EquipmentEntity.class))
                .collect(Collectors.toCollection(LinkedList::new));
    }

    public String getRoomById(Integer roomId) {
        checkNotNull(roomId);
        return databaseService.getContext()
                .select(ROOM.ROOM_NAME)
                .from(ROOM)
                .where(ROOM.ROOM_ID.eq(roomId))
                .fetchOne().value1();
    }
}
