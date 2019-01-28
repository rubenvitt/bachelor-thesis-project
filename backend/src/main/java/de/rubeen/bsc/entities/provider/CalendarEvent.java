package de.rubeen.bsc.entities.provider;

import org.joda.time.DateTime;
import org.joda.time.Interval;

import java.util.List;

import static java.text.MessageFormat.format;

public class CalendarEvent {
    private String subject, description, room, calendarId;
    private DateTime startDateTime, endDateTime;
    private List<Attendee> attendees;

    public CalendarEvent(String subject, String description, String room, String calendarId, DateTime startDateTime, DateTime endDateTime, List<Attendee> attendees) {
        this.subject = subject;
        this.description = description;
        this.room = room;
        this.calendarId = calendarId;
        this.startDateTime = startDateTime;
        this.endDateTime = endDateTime;
        this.attendees = attendees;
    }

    public CalendarEvent() {
    }

    public CalendarEvent(String subject, String description, String room, String calendarId, Interval meetingInterval, List<Attendee> attendees) {
        this(subject, description, room, calendarId, meetingInterval.getStart(), meetingInterval.getEnd(), attendees);
    }

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

    @Override
    public String toString() {
        return format(
                "CalendarEvent=[subject: \"{0}\", description: \"{1}\", room: \"{2}\", calendarId: \"{3}\"startDateTime: \"{4}\", endDateTime: \"{5}\", attendees: \"{6}\"]",
                subject, description, room, calendarId, startDateTime, endDateTime, attendees);
    }

    public static class Attendee {
        private String name, mail;

        public Attendee() {
        }

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

        @Override
        public String toString() {
            return format("Attendee=[name: \"{0}\", email: \"{1}\"]",
                    name, mail);
        }
    }
}
