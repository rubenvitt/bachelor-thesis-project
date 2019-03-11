package de.rubeen.bsc.service.provider;

import de.rubeen.bsc.entities.db.tables.records.RoomRecord;
import de.rubeen.bsc.entities.provider.CalendarEvent;
import de.rubeen.bsc.entities.web.CalendarEntity;
import de.rubeen.bsc.entities.web.NewEventEntity;
import de.rubeen.bsc.service.DatabaseService;
import de.rubeen.bsc.service.LoggableService;
import org.joda.time.Interval;
import org.springframework.stereotype.Service;

import java.util.List;

import static de.rubeen.bsc.entities.db.tables.Room.ROOM;

@Service
public class PrototypeRoomProviderService extends LoggableService implements CalendarProvider {
    private final DatabaseService databaseService;

    public PrototypeRoomProviderService(DatabaseService databaseService) {
        this.databaseService = databaseService;
    }

    @Override
    public boolean createEvent(CalendarEvent calendarEvent, String roomId) throws CalendarException {
        LOG.info("ROOM-EVENT: Create event {} for room {}", calendarEvent, roomId);
        return false;
    }

    @Override
    public List<CalendarEntity> getAllCalendars(String roomId) throws CalendarException {
        LOG.info("Getting all calendars for room {}", roomId);
        return getAllActiveCalendars(roomId);
    }

    @Override
    public List<CalendarEntity> getAllActiveCalendars(String roomId) throws CalendarException {
        LOG.info("Getting all active calendars for room {}", roomId);
        return List.of(getCalendar(roomId, true));
    }

    @Override
    public List<CalendarEvent> getEventsBetween(Interval interval, String roomId, String calendarId) throws CalendarException {
        LOG.info("Getting events between {} for room {} (calendar: {})", interval, roomId, calendarId);
        LOG.info("--- not implemented, dummy-values! ---");
        return List.of();
    }

    @Override
    public List<Interval> getBusyTimes(String roomId, NewEventEntity eventEntity) throws CalendarException {
        LOG.info("Getting busyTimes for room {} at {}", roomId, eventEntity);
        LOG.info("--- not implemented, dummy-values! ---");
        return List.of();
    }

    @Override
    public CalendarEntity getCalendar(String calendarId, String roomId, boolean isActivated, boolean isDefault) {
        LOG.info("Getting calendar {} for room {}", calendarId, roomId);
        //calendarID is not needed for rooms, since they only have a single calendar
        return getCalendar(roomId, isDefault);
    }

    private CalendarEntity getCalendar(String roomId, boolean isDefault) {
        final RoomRecord roomRecord = databaseService.getContext()
                .selectFrom(ROOM)
                .where(ROOM.ROOM_ID.eq(Integer.parseInt(roomId)))
                .fetchOne();
        return new CalendarEntity(roomRecord.getRoomName(), roomRecord.getCalendarid(), true,
                roomRecord.getProvider().getName(), isDefault);
    }
}
