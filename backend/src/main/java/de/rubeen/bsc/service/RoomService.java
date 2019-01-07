package de.rubeen.bsc.service;

import com.google.common.collect.Multimap;
import com.google.common.collect.MultimapBuilder;
import de.rubeen.bsc.entities.web.RoomEntity;
import org.jooq.Record;
import org.jooq.Result;
import org.modelmapper.ModelMapper;
import org.modelmapper.convention.NameTokenizers;
import org.modelmapper.jooq.RecordValueReader;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import java.sql.SQLException;
import java.util.Collection;
import java.util.List;
import java.util.Set;

import static de.rubeen.bsc.entities.db.tables.Room.ROOM;
import static de.rubeen.bsc.entities.db.tables.RoomEquipment.ROOM_EQUIPMENT;
import static de.rubeen.bsc.entities.db.tables.RoomRoomEquipment.ROOM_ROOM_EQUIPMENT;

@Service
public class RoomService extends AbstractDatabaseService {
    public RoomService(@Value("${database.url}") final String url,
                       @Value("${database.user}") final String user,
                       @Value("${database.pass}") final String password) throws SQLException {
        super(url, user, password);
    }

    public Collection<RoomEntity> getAllRooms() {
        try {
            return getRoomEntities();
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return List.of(new RoomEntity(0, "test", 3, List.of(new RoomEntity.EquipmentEntity(1, "Equipment"))));
        //return null;
    }

    private Set<RoomEntity> getRoomEntities() throws SQLException {
        Multimap<RoomEntity, RoomEntity.EquipmentEntity> roomEntityEquipmentEntityMultimap =
                MultimapBuilder.treeKeys().arrayListValues().build();
        final ModelMapper modelMapper = new ModelMapper();
        modelMapper.getConfiguration().addValueReader(new RecordValueReader());
        modelMapper.getConfiguration().setSourceNameTokenizer(NameTokenizers.UNDERSCORE);

        final Result<Record> records = dslContext.select()
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
}
