package de.rubeen.bsc.service;

import com.google.common.collect.Multimap;
import com.google.common.collect.MultimapBuilder;
import de.rubeen.bsc.entities.db.tables.records.RoomRecord;
import de.rubeen.bsc.entities.web.RoomEntity;
import org.jooq.Record;
import org.jooq.Record2;
import org.jooq.Result;
import org.modelmapper.ModelMapper;
import org.modelmapper.convention.NameTokenizers;
import org.modelmapper.jooq.RecordValueReader;
import org.springframework.stereotype.Service;

import java.sql.SQLException;
import java.util.*;
import java.util.stream.Collectors;

import static com.google.common.base.Preconditions.checkNotNull;
import static de.rubeen.bsc.entities.db.tables.Room.ROOM;
import static de.rubeen.bsc.entities.db.tables.RoomEquipment.ROOM_EQUIPMENT;
import static de.rubeen.bsc.entities.db.tables.RoomRoomEquipment.ROOM_ROOM_EQUIPMENT;
import static org.jooq.impl.DSL.castNull;
import static org.jooq.impl.DSL.when;

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
            LOG.error("Unable to get all roomEntities", e);
            return Collections.emptyList();
        }
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

    public RoomEntity getRoomById(Integer roomId) {
        checkNotNull(roomId);
        LOG.debug("Search for room {} in database", roomId);
        RoomEntity room = databaseService.getContext()
                .selectFrom(ROOM)
                .where(ROOM.ROOM_ID.eq(roomId))
                .fetchOneInto(RoomEntity.class);
        LOG.debug("Got room {} for roomId {}", room, roomId);
        return room;
    }

    public RoomEntity getBestRoomFor(List<String> roomValues, int minSize) {
        final Result<RoomRecord> rooms = databaseService.getContext()
                .selectFrom(ROOM)
                .where(ROOM.ROOM_SIZE.greaterOrEqual(minSize))
                .fetch();

        int roomSize = rooms.size();
        if (roomSize == 0)
            return null;
        else if (roomSize == 1)
            return rooms.get(0).into(RoomEntity.class);
        else {
            int bestSize = roomValues.size();
            Multimap<Long, Integer> sizePerRoomIds = MultimapBuilder.treeKeys().linkedHashSetValues().build();

            final Result<Record2<Integer, String>> fetch = databaseService.getContext()
                    .select(ROOM.ROOM_ID,
                            when(ROOM_EQUIPMENT.EQUIP_NAME.in(roomValues),
                                    ROOM_EQUIPMENT.EQUIP_NAME)
                                    .otherwise(castNull(String.class)))
                    .from(ROOM)
                    .innerJoin(ROOM_ROOM_EQUIPMENT).onKey()
                    .innerJoin(ROOM_EQUIPMENT).onKey()
                    .where(ROOM.ROOM_SIZE.greaterOrEqual(minSize))
                    .fetch();
            fetch.forEach(integerRoomEquipmentRecord2 -> {
                LOG.info("Got FETCH-Result: {} - {}", integerRoomEquipmentRecord2.component1(), integerRoomEquipmentRecord2.component2());
            });

            final Map<Integer, List<Record2<Integer, String>>> map =
                    fetch.parallelStream()
                            .collect(Collectors.groupingBy(Record2::component1));
            map.forEach((roomId, record2s) -> {
                LOG.info("{} - {}", roomId, record2s);
                final long count = record2s.parallelStream()
                        .filter(integerStringRecord2 -> integerStringRecord2.component2() != null)
                        .count();
                LOG.info("{} has {} elements", roomId, count);
                sizePerRoomIds.put(count, roomId);
            });
            LOG.info("Multimap: {}", sizePerRoomIds);

            long i;
            for (i = bestSize; i >= 0 && !sizePerRoomIds.containsKey(i); i--) {
            }
            LOG.info("i = {}", i);
            int roomWithBestResult = getSmallestRoomId(sizePerRoomIds.get(i));
            LOG.info("Got room as best result: {}", roomWithBestResult);

            final RoomEntity roomEntity = databaseService.getContext()
                    .selectFrom(ROOM)
                    .where(ROOM.ROOM_ID.eq(roomWithBestResult))
                    .fetchOne().into(RoomEntity.class);
            roomEntity.setEquipments(
                    databaseService.getContext()
                            .select()
                            .from(ROOM_ROOM_EQUIPMENT)
                            .innerJoin(ROOM_EQUIPMENT).onKey()
                            .where(ROOM_ROOM_EQUIPMENT.ROOM_ID.eq(roomEntity.getId()))
                            .fetchInto(RoomEntity.EquipmentEntity.class)
            );
            return roomEntity;
        }
    }

    private int getSmallestRoomId(Collection<Integer> roomIds) {
        return databaseService.getContext()
                .selectFrom(ROOM)
                .where(ROOM.ROOM_ID.in(roomIds))
                .orderBy(ROOM.ROOM_SIZE.asc())
                .limit(1)
                .fetchOne().getRoomId();
    }
}
