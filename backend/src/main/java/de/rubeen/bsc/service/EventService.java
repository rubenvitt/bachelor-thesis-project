package de.rubeen.bsc.service;

import com.google.api.services.calendar.model.Events;
import de.rubeen.bsc.entities.db.enums.Calprovider;
import de.rubeen.bsc.entities.web.EventEntity;
import de.rubeen.bsc.entities.web.NewEventEntity;
import de.rubeen.bsc.helper.EventComparatorFactory;
import de.rubeen.bsc.service.provider.CalendarProvider;
import de.rubeen.bsc.service.provider.GoogleProviderService;
import org.apache.commons.lang3.NotImplementedException;
import org.joda.time.DateTime;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import javax.security.auth.login.CredentialException;
import java.io.IOException;
import java.security.GeneralSecurityException;
import java.sql.SQLException;
import java.util.LinkedList;
import java.util.List;
import java.util.Objects;
import java.util.stream.Collectors;

import static de.rubeen.bsc.entities.db.Tables.APPUSER;
import static de.rubeen.bsc.entities.db.Tables.CALENDAR;

@Service
public class EventService extends LoggableService {
    private final GoogleProviderService googleProviderService;
    private final ProviderService providerService;
    private final LoginService loginService;
    private final RoomService roomService;
    private final DatabaseService databaseService;

    @Autowired
    public EventService(GoogleProviderService googleProviderService, ProviderService providerService, LoginService loginService, RoomService roomService, DatabaseService databaseService) throws SQLException {
        this.googleProviderService = googleProviderService;
        this.providerService = providerService;
        this.loginService = loginService;
        this.roomService = roomService;
        this.databaseService = databaseService;
    }

    public List<EventEntity> getAllEventsForToday(String userMail) {
        return this.getAllEventsForUser(userMail, getBeginOfToday().getMillis(), getEndOfToday().getMillis());
    }

    public List<EventEntity> getAllEventsForWeekNumber(Integer weekNumber, String userMail) {
        DateTime begin = getBeginOfWeek(weekNumber);
        DateTime end = getEndOfWeek(weekNumber);
        return this.getAllEventsForUser(userMail, begin.getMillis(), end.getMillis());
    }

    public List<EventEntity> getAllEventsForUser(String userMail, Long startMillis, Long endMillis) {
        final DateTime startTime = new DateTime(startMillis),
                endTime = new DateTime(endMillis);
        final Integer userID = loginService.getUserID(userMail);
        final List<String> googleCalendars = databaseService.getContext()
                .select(CALENDAR.CALENDARID)
                .from(CALENDAR)
                .innerJoin(APPUSER).onKey()
                .where(APPUSER.ID.eq(userID))
                .and(CALENDAR.PROVIDER.eq(Calprovider.google))
                .and(CALENDAR.ACTIVATED.eq(true)).fetch(CALENDAR.CALENDARID);
        LOG.info("Found following active calendars: ");
        googleCalendars.parallelStream().forEach(LOG::info);

        final List<Events> eventsList = googleCalendars.parallelStream()
                .map(calendar -> {
                    try {
                        return googleProviderService.getEvents(userMail,
                                calendar,
                                startTime,
                                endTime);
                    } catch (IOException | GeneralSecurityException e) {
                        LOG.error("Can't get events for user: {} - calendar: {}", userMail, calendar, e);
                        return null;
                    }
                }).filter(Objects::nonNull)
                .collect(Collectors.toList());
        List<EventEntity> eventEntities = new LinkedList<>();
        eventsList.stream()
                .filter(events -> events.getItems().size() > 0)
                .forEach(events -> eventEntities.addAll(
                        events.getItems().stream()
                                .map(event -> {
                                    LOG.info("Mapping: {}", event);
                                    DateTime start, end;
                                    LOG.info("START-DT: {}", event.getStart().getDateTime());
                                    if (event.getStart().getDateTime() == null)
                                        start = new DateTime(event.getStart().getDate().toString());
                                    else
                                        start = new DateTime(event.getStart().getDateTime().toString());
                                    if (event.getEnd().getDateTime() == null)
                                        end = new DateTime(event.getEnd().getDate().toString());
                                    else
                                        end = new DateTime(event.getEnd().getDateTime().toString());
                                    return new EventEntity(event.getSummary(), start, end);
                                })
                                .collect(Collectors.toList())
                ));
        eventEntities.sort(EventComparatorFactory.getDateComparator());
        return eventEntities;
    }

    public void addEvent(NewEventEntity newEventEntity, String userMail, String calendarId) {
        CalendarProvider calendarProvider = providerService.getCalendarProvider(calendarId);
        if (newEventEntity.isAutoTime())
            createAutoEvent(newEventEntity, userMail, calendarId, calendarProvider);
        else
            throw new NotImplementedException("Manual events are not implemented yet");

        /*try {
            if (newEventEntity.isAutoTime()) //create auto event
                createAutoEvent(newEventEntity, userMail, calendarId);
            else
                createManualEvent(newEventEntity, userMail, calendarId);
        } catch (GeneralSecurityException | IOException e) {
            LOG.error("Error while creating event", e);
        }*/
    }

    private void createAutoEvent(final NewEventEntity newEventEntity, final String userMail, final String calendarId,
                                 final CalendarProvider calendarProvider) {
        LOG.info("using calendarProvider: {} to create an event for {} - calendarId: {} - event: {}",
                calendarProvider, userMail, calendarId, newEventEntity);
    }

    private void createManualEvent(NewEventEntity newEventEntity, String userMail, String calendarId) throws IOException, CredentialException {
        final Integer userID = loginService.getUserID(userMail);
        googleProviderService.createEvent(userMail, calendarId, newEventEntity);
    }

    private void createAutoEvent(NewEventEntity newEventEntity, String userMail, String calendarId) throws IOException, GeneralSecurityException, CalendarProvider.CalendarException {
        googleProviderService.createAutoEvent(userMail, calendarId, newEventEntity);
    }

    private DateTime getBeginOfWeek(Integer weekNumber) {
        if (weekNumber == null)
            weekNumber = DateTime.now().getWeekOfWeekyear();
        return getBeginOfDay(new DateTime().withWeekOfWeekyear(weekNumber).dayOfWeek().withMinimumValue());
    }

    private DateTime getEndOfWeek(Integer weekNumber) {
        if (weekNumber == null)
            weekNumber = DateTime.now().getWeekOfWeekyear();
        return getEndOfDay(new DateTime().withWeekOfWeekyear(weekNumber).dayOfWeek().withMaximumValue());
    }

    private DateTime getEndOfToday() {
        return getEndOfDay(DateTime.now());
    }

    private DateTime getBeginOfToday() {
        return getBeginOfDay(DateTime.now());
    }

    private DateTime getBeginOfDay(DateTime day) {
        return day.withHourOfDay(0).withMinuteOfHour(0).withSecondOfMinute(0).withMillisOfSecond(0);
    }

    private DateTime getEndOfDay(DateTime day) {
        return day.withHourOfDay(23).withMinuteOfHour(59).withSecondOfMinute(59).withMillisOfSecond(999);
    }
}
