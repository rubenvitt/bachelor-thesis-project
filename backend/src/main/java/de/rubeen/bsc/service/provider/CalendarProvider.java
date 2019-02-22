package de.rubeen.bsc.service.provider;

import de.rubeen.bsc.entities.provider.CalendarEvent;
import de.rubeen.bsc.entities.web.CalendarEntity;
import de.rubeen.bsc.entities.web.NewEventEntity;
import org.joda.time.Interval;

import java.util.List;

public interface CalendarProvider {
    /**
     * create an event in users calendar
     *
     * @return true if successfully added event to calendar
     */
    boolean createEvent(CalendarEvent calendarEvent, String userId) throws CalendarException;

    List<CalendarEntity> getAllCalendars(String user_id) throws CalendarException;

    List<CalendarEntity> getAllActiveCalendars(String user_id) throws CalendarException;

    List<CalendarEvent> getEventsBetween(Interval interval, String userId, String calendarId) throws CalendarException;

    List<Interval> getBusyTimes(String userId, NewEventEntity eventEntity) throws CalendarException;

    CalendarEntity getCalendar(String calendarId, String userMail, boolean isActivated, boolean isDefault);

    class CalendarException extends Exception {
        public CalendarException(String message, Exception e) {
            super(message, e);
        }
    }
}
