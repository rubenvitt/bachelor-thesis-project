package de.rubeen.bsc.service;

import de.rubeen.bsc.entities.db.enums.Calprovider;
import de.rubeen.bsc.entities.db.tables.records.CalendarRecord;
import de.rubeen.bsc.entities.web.CalendarEntity;
import de.rubeen.bsc.service.provider.*;
import org.apache.commons.lang3.NotImplementedException;
import org.modelmapper.ModelMapper;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.stream.Collectors;

import static de.rubeen.bsc.entities.db.Tables.CALENDAR;
import static de.rubeen.bsc.entities.db.Tables.ROOM;

@Service
public class ProviderService extends LoggableService {

    private final DatabaseService databaseService;
    private final LoginService loginService;
    private final GoogleProviderService googleProviderService;
    private final OfficeProviderService officeProviderService;
    private final PrototypeRoomProviderService prototypeRoomProviderService;
    private final ModelMapper modelMapper = new ModelMapper();

    public ProviderService(DatabaseService databaseService, LoginService loginService, GoogleProviderService googleProviderService, OfficeProviderService officeProviderService, PrototypeRoomProviderService prototypeRoomProviderService) {
        this.databaseService = databaseService;
        this.loginService = loginService;
        this.googleProviderService = googleProviderService;
        this.officeProviderService = officeProviderService;
        this.prototypeRoomProviderService = prototypeRoomProviderService;
    }

    CalendarProvider getCalendarProvider(String calendarId, String user) {
        final Calprovider calprovider = databaseService.getContext()
                .select(CALENDAR.PROVIDER)
                .from(CALENDAR)
                .where(CALENDAR.CALENDARID.eq(calendarId))
                .and(CALENDAR.USER_ID.eq(loginService.getUserID(user)))
                .fetchOneInto(Calprovider.class);
        LOG.debug("got calProvider: {} for {}", calprovider, calendarId);
        switch (calprovider) {
            case google:
                return googleProviderService;
            case office:
                return officeProviderService;
            case room_service:
                throw new NotImplementedException("RoomProviderService as calendarProvider for users is not available");
            default:
                throw new NotImplementedException("This method wasn't implemented, yet");
        }
    }

    public CalendarProvider getRoomCalendarProvider(int roomId) {
        final Calprovider calprovider = databaseService.getContext()
                .select(ROOM.PROVIDER)
                .from(ROOM)
                .where(ROOM.ROOM_ID.eq(roomId))
                .fetchOneInto(Calprovider.class);
        LOG.debug("Got calProvider {} for room {}", calprovider, roomId);

        switch (calprovider) {
            case google:
                throw new NotImplementedException("Google as room-cal-provider was not implemented");
            case office:
                throw new NotImplementedException("Office as room-cal-provider was not implemented");
            case room_service:
                return prototypeRoomProviderService;
            default:
                throw new NotImplementedException("This method wasn't implemented, yet");
        }
    }

    public List<CalendarEntity> getAllCalendarEntities(String userMail) {
        LOG.info("Getting all calendarEntities for {}", userMail);
        return databaseService.getContext().select()
                .from(CALENDAR)
                .where(CALENDAR.USER_ID.eq(loginService.getUserID(userMail)))
                .and(CALENDAR.ACTIVATED.eq(true))
                .fetch()
                .map(record -> modelMapper.map(record, CalendarRecord.class))
                .parallelStream()
                .map(calendarRecord -> getCalendarProvider(calendarRecord.getCalendarid(), userMail)
                        .getCalendar(calendarRecord.getCalendarid(), userMail, calendarRecord.getActivated(), calendarRecord.getIsdefault()))
                .collect(Collectors.toList());
    }

    public void setDefaultCalendar(String calendarID, String userID) {
        databaseService.getContext().update(CALENDAR)
                .set(CALENDAR.ISDEFAULT, true)
                .where(CALENDAR.CALENDARID.eq(calendarID))
                .and(CALENDAR.USER_ID.eq(loginService.getUserID(userID)))
                .execute();
        databaseService.getContext().update(CALENDAR)
                .set(CALENDAR.ISDEFAULT, false)
                .where(CALENDAR.CALENDARID.notEqual(calendarID))
                .and(CALENDAR.USER_ID.eq(loginService.getUserID(userID)))
                .execute();
    }

    public CalendarEntity getDefaultCalendar(String userID) {
        return databaseService.getContext().selectFrom(CALENDAR)
                .where(CALENDAR.USER_ID.eq(loginService.getUserID(userID)))
                .and(CALENDAR.ISDEFAULT)
                .and(CALENDAR.ACTIVATED)
                .fetchOne(calendarRecord -> getCalendarProvider(calendarRecord.getCalendarid(), userID)
                        .getCalendar(calendarRecord.getCalendarid(), userID, calendarRecord.getActivated(), calendarRecord.getIsdefault()));
    }
}
