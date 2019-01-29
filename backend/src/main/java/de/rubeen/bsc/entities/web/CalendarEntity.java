package de.rubeen.bsc.entities.web;

import com.google.api.services.calendar.model.CalendarListEntry;
import de.rubeen.bsc.entities.db.tables.records.CalendarRecord;
import de.rubeen.bsc.provider.office365.entities.Calendar;

public class CalendarEntity {
    private String calendarName, calendarID;
    private boolean activated;
    private String provider;

    public CalendarEntity() {}

    public CalendarEntity(CalendarListEntry calendarListEntry, boolean activated) {
        this.calendarName = calendarListEntry.getSummary();
        this.calendarID = calendarListEntry.getId();
        this.activated = activated;
        this.provider = "google";
    }

    public CalendarEntity(String calendarName, String calendarID, boolean activated, String provider) {
        this.calendarName = calendarName;
        this.calendarID = calendarID;
        this.activated = activated;
        this.provider = provider;
    }

    public CalendarEntity(Calendar calendar, boolean isActivated) {
        this.calendarName = calendar.getName();
        this.calendarID = calendar.getId();
        this.activated = isActivated;
        this.provider = "office";
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

    public String getProvider() {
        return provider;
    }

    public void setProvider(String provider) {
        this.provider = provider;
    }
}
