package de.rubeen.bsc.provider.office365.entities;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import de.rubeen.bsc.entities.provider.CalendarEvent;

import java.util.Collection;
import java.util.stream.Collectors;

@JsonIgnoreProperties(ignoreUnknown = true)
public class Event {
    private String id;
    private String subject;
    private Recipient organizer;
    private DateTimeTimeZone start;
    private DateTimeTimeZone end;
    private Collection<Recipient> attendees;
    private Body body;
    private Location location;
    private String onlineMeetingUrl;

    public Event() {
    }

    public Event(String subject, Recipient organizer, DateTimeTimeZone start, DateTimeTimeZone end) {
        this.subject = subject;
        this.organizer = organizer;
        this.start = start;
        this.end = end;
    }

    public Event(CalendarEvent calendarEvent) {
        this.subject = calendarEvent.getSubject();
        this.start = new DateTimeTimeZone(calendarEvent.getStartDateTime());
        this.end = new DateTimeTimeZone(calendarEvent.getEndDateTime());
        this.body = new Body(calendarEvent.getDescription());
        this.location = new Location(calendarEvent.getRoom());
        this.attendees = calendarEvent.getAttendees().parallelStream()
                .map(attendee -> new Recipient(attendee.getName(), attendee.getMail()))
                .collect(Collectors.toList());
    }

    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }

    public String getSubject() {
        return subject;
    }

    public void setSubject(String subject) {
        this.subject = subject;
    }

    public Recipient getOrganizer() {
        return organizer;
    }

    public void setOrganizer(Recipient organizer) {
        this.organizer = organizer;
    }

    public DateTimeTimeZone getStart() {
        return start;
    }

    public void setStart(DateTimeTimeZone start) {
        this.start = start;
    }

    public DateTimeTimeZone getEnd() {
        return end;
    }

    public void setEnd(DateTimeTimeZone end) {
        this.end = end;
    }

    public Collection<Recipient> getAttendees() {
        return attendees;
    }

    public void setAttendees(Collection<Recipient> attendees) {
        this.attendees = attendees;
    }

    public Body getBody() {
        return body;
    }

    public void setBody(Body body) {
        this.body = body;
    }

    public Location getLocation() {
        return location;
    }

    public void setLocation(Location location) {
        this.location = location;
    }

    public String getOnlineMeetingUrl() {
        return onlineMeetingUrl;
    }

    public void setOnlineMeetingUrl(String onlineMeetingUrl) {
        this.onlineMeetingUrl = onlineMeetingUrl;
    }
}