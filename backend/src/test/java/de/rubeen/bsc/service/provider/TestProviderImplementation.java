package de.rubeen.bsc.service.provider;

import de.rubeen.bsc.entities.provider.CalendarEvent;
import de.rubeen.bsc.entities.web.CalendarEntity;
import de.rubeen.bsc.entities.web.NewEventEntity;
import de.rubeen.bsc.service.LoggableService;
import org.joda.time.DateTime;
import org.joda.time.Interval;

import java.util.Collections;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

public class TestProviderImplementation extends LoggableService implements CalendarProvider {
    @Override
    public boolean createEvent(CalendarEvent calendarEvent, String userId) throws CalendarException {
        LOG.info("creating calendar-Event {} for user {}", calendarEvent, userId);
        return true;
    }

    @Override
    public List<CalendarEntity> getAllCalendars(String user_id) throws CalendarException {
        LOG.info("Getting all calendars for {}", user_id);
        CalendarEntity calendarEntity1 = new CalendarEntity("test-calendar 1", "test-calendar-1",
                true, "test-provider");
        CalendarEntity calendarEntity2 = new CalendarEntity("test-calendar 2", "test-calendar-2",
                true, "test-provider");
        CalendarEntity inactiveCalendarEntity = new CalendarEntity("inactive test-calendar", "test-calendar-3",
                true, "test-provider");
        return List.of(calendarEntity1, calendarEntity2, inactiveCalendarEntity);
    }

    @Override
    public List<CalendarEntity> getAllActiveCalendars(String user_id) throws CalendarException {
        LOG.info("Getting all ACTIVE calendars for {}", user_id);
        CalendarEntity calendarEntity1 = new CalendarEntity("test-calendar 1", "test-calendar-1",
                true, "test-provider");
        CalendarEntity calendarEntity2 = new CalendarEntity("test-calendar 2", "test-calendar-2",
                true, "test-provider");
        return List.of(calendarEntity1, calendarEntity2);
    }

    @Override
    public List<CalendarEvent> getEventsBetween(Interval interval, String userId, String calendarId) throws CalendarException {
        LOG.info("Getting events for {} - cal {} in interval: {}", userId, calendarId, interval);
        if (userId.equals("full-event")) {
            return List.of(new CalendarEvent("full-day-event", "event for given interval", "test-room",
                    getCalendar(calendarId, userId, true).getCalendarID(), interval, Collections.emptyList()));
        }
        if (userId.equals("one-hour-event-at-start")) {
            return List.of(new CalendarEvent("one-hour-event", "event with a duration of one hour", "test-room",
                    getCalendar(calendarId, userId, true).getCalendarID(),
                    new Interval(interval.getStart(), interval.getStart().plusHours(1)),
                    Collections.emptyList()));
        }
        if (userId.equals("two-one-hour-events-with-1-hour-break-and-one-hour-before")) {
            return List.of(new CalendarEvent("one-hour-event #1", "first of two one-hour events", "test-room",
                    getCalendar(calendarId, userId, true).getCalendarID(),
                    new Interval(interval.getStart().plusHours(1), interval.getStart().plusHours(1)),
                    Collections.emptyList()));
        }
        return Collections.emptyList();
    }

    @Override
    public List<Interval> getBusyTimes(String userId, NewEventEntity eventEntity) throws CalendarException {
        LOG.info("Getting busy-times for {} at event: {}", userId, eventEntity);
        DateTime timeMin = new DateTime(DateTime.parse(eventEntity.getAutoTimeDateStart()).toDate());
        DateTime timeMax = new DateTime(DateTime.parse(eventEntity.getAutoTimeDateEnd()).toDate());

        return getEventsBetween(new Interval(timeMin, timeMax), userId, "test-calendar").stream()
                .map(calendarEvent -> new Interval(calendarEvent.getStartDateTime(), calendarEvent.getEndDateTime()))
                .collect(Collectors.toList());
    }

    @Override
    public CalendarEntity getCalendar(String calendarId, String userMail, boolean isActivated) {
        LOG.info("Getting infos for calendar {}", calendarId);
        final Optional<CalendarEntity> calendar;
        try {
            calendar = getAllCalendars(userMail).stream()
                    .filter(calendarEntity -> calendarEntity.getCalendarID().equals(calendarId))
                    .findFirst();
        } catch (CalendarException e) {
            return null;
        }
        return calendar.orElse(null);
    }
}
