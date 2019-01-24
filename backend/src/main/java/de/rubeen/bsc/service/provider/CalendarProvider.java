package de.rubeen.bsc.service.provider;

import de.rubeen.bsc.entities.provider.NewCalendarEvent;
import de.rubeen.bsc.entities.web.CalendarEntity;

import java.util.List;

public interface CalendarProvider {
    /**
     * create an event in users calendar
     *
     * @return true if successfully added event to calendar
     */
    boolean createEvent(NewCalendarEvent newCalendarEvent);

    List<CalendarEntity> getAllCalendars(String user_id) throws CalendarException;

    List<CalendarEntity> getAllActiveCalendars(String user_id) throws CalendarException;

    class CalendarException extends Exception {
        CalendarException(String message, Exception e) {
            super(message, e);
        }
    }
}
