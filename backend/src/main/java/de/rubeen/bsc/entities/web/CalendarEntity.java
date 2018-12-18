package de.rubeen.bsc.entities.web;

public class CalendarEntity {
    private String calendarName, calendarID;
    private boolean activated;

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
