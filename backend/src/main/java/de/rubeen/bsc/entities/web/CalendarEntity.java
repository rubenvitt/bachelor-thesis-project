package de.rubeen.bsc.entities.web;

import com.google.api.services.calendar.model.CalendarListEntry;

public class CalendarEntity {
    private String calendarName, calendarID;
    private boolean activated;

    public CalendarEntity(CalendarListEntry calendarListEntry, boolean activated) {
        this.calendarName = calendarListEntry.getSummary();
        this.calendarID = calendarListEntry.getId();
        this.activated = activated;
    }

    public CalendarEntity(String calendarName, String calendarID, boolean activated) {
        this.calendarName = calendarName;
        this.calendarID = calendarID;
        this.activated = activated;
    }

    public String getCalendarName() {
        return calendarName;
    }

    public String getCalendarID() {
        return calendarID;
    }

    public boolean isActivated() {
        return activated;
    }
}
