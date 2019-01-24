package de.rubeen.bsc.entities.provider;

import org.joda.time.DateTime;

import java.util.List;

public class NewCalendarEvent {
    private String subject, description, room, calendarId;
    private DateTime startDateTime, endDateTime;
    private List<Attendee> attendees;

    public NewCalendarEvent(String subject, String description, String room, String calendarId, DateTime startDateTime, DateTime endDateTime, List<Attendee> attendees) {
        this.subject = subject;
        this.description = description;
        this.room = room;
        this.calendarId = calendarId;
        this.startDateTime = startDateTime;
        this.endDateTime = endDateTime;
        this.attendees = attendees;
    }

    public NewCalendarEvent() {}

    public String getSubject() {
        return subject;
    }

    public void setSubject(String subject) {
        this.subject = subject;
    }

    public String getDescription() {
        return description;
    }

    public void setDescription(String description) {
        this.description = description;
    }

    public String getRoom() {
        return room;
    }

    public void setRoom(String room) {
        this.room = room;
    }

    public DateTime getStartDateTime() {
        return startDateTime;
    }

    public void setStartDateTime(DateTime startDateTime) {
        this.startDateTime = startDateTime;
    }

    public DateTime getEndDateTime() {
        return endDateTime;
    }

    public void setEndDateTime(DateTime endDateTime) {
        this.endDateTime = endDateTime;
    }

    public List<Attendee> getAttendees() {
        return attendees;
    }

    public void setAttendees(List<Attendee> attendees) {
        this.attendees = attendees;
    }

    public String getCalendarId() {
        return calendarId;
    }

    public void setCalendarId(String calendarId) {
        this.calendarId = calendarId;
    }

    private class Attendee {
        private String name, mail;

        public Attendee() {}

        public Attendee(String name, String mail) {
            this.name = name;
            this.mail = mail;
        }

        public String getName() {
            return name;
        }

        public void setName(String name) {
            this.name = name;
        }

        public String getMail() {
            return mail;
        }

        public void setMail(String mail) {
            this.mail = mail;
        }
    }
}
