package de.rubeen.bsc.entities.web;

import com.fasterxml.jackson.annotation.JsonGetter;
import com.fasterxml.jackson.annotation.JsonSetter;

import java.util.List;

import static java.text.MessageFormat.format;

public class NewEventEntity {
    private String subject, description;
    private boolean autoTime, autoRoom;
    private String manTimeDateStart, manTimeDateEnd;
    private String autoTimeDateStart, autoTimeDateEnd;
    private Integer meetingDuration;
    private String durationUnit;
    private String manTimeTimeStart, manTimeTimeEnd;
    private List<String> roomValues;
    private Integer roomId;
    private List<Integer> attendees;

    public NewEventEntity() {}

    public NewEventEntity(String subject, String description, boolean autoTime, boolean autoRoom, String autoTimeDateStart, String autoTimeDateEnd, Integer meetingDuration, String durationUnit, List<Integer> attendees) {
        this.subject = subject;
        this.description = description;
        this.autoTime = autoTime;
        this.autoTimeDateStart = autoTimeDateStart;
        this.autoTimeDateEnd = autoTimeDateEnd;
        this.meetingDuration = meetingDuration;
        this.durationUnit = durationUnit;
        this.attendees = attendees;
    }

    public NewEventEntity(String subject, String description, boolean autoTime, boolean autoRoom,
                          String manTimeDateStart, String manTimeDateEnd, String autoTimeDateStart, String autoTimeDateEnd, Integer meetingDuration, String durationUnit, String manTimeTimeStart, String manTimeTimeEnd,
                          List<String> roomValues, Integer roomId, List<Integer> attendees) {
        this.subject = subject;
        this.description = description;
        this.autoTime = autoTime;
        this.autoRoom = autoRoom;
        this.manTimeDateStart = manTimeDateStart;
        this.manTimeDateEnd = manTimeDateEnd;
        this.autoTimeDateStart = autoTimeDateStart;
        this.autoTimeDateEnd = autoTimeDateEnd;
        this.meetingDuration = meetingDuration;
        this.durationUnit = durationUnit;
        this.manTimeTimeStart = manTimeTimeStart;
        this.manTimeTimeEnd = manTimeTimeEnd;
        this.roomValues = roomValues;
        this.roomId = roomId;
        this.attendees = attendees;
    }

    @JsonGetter
    public String getManTimeTimeStart() {
        return manTimeTimeStart;
    }

    @JsonSetter
    public void setManTimeTimeStart(String manTimeTimeStart) {
        this.manTimeTimeStart = manTimeTimeStart;
    }

    @JsonGetter
    public String getManTimeTimeEnd() {
        return manTimeTimeEnd;
    }

    @JsonSetter
    public void setManTimeTimeEnd(String manTimeTimeEnd) {
        this.manTimeTimeEnd = manTimeTimeEnd;
    }

    @JsonGetter
    public String getManTimeDateEnd() {
        return manTimeDateEnd;
    }

    @JsonSetter
    public void setManTimeDateEnd(String manTimeDateEnd) {
        this.manTimeDateEnd = manTimeDateEnd;
    }

    @JsonGetter
    public String getManTimeDateStart() {
        return manTimeDateStart;
    }

    @JsonSetter
    public void setManTimeDateStart(String manTimeDateStart) {
        this.manTimeDateStart = manTimeDateStart;
    }

    @JsonGetter
    public String getSubject() {
        return subject;
    }

    @JsonSetter
    public void setSubject(String subject) {
        this.subject = subject;
    }

    @JsonGetter
    public String getDescription() {
        return description;
    }

    @JsonSetter
    public void setDescription(String description) {
        this.description = description;
    }

    @JsonGetter
    public boolean isAutoTime() {
        return autoTime;
    }

    @JsonSetter
    public void setAutoTime(boolean autoTime) {
        this.autoTime = autoTime;
    }

    @Override
    public String toString() {
        return format("(subject: {0}, description: {1}, autoTime: {2}, manTimeDateStart: {3}, manTimeDateEnd: {4}, manTimeTimeStart: {5}, manTimeTimeEnd: {6})",
                this.subject, this.description, this.autoTime, this.manTimeDateStart, this.manTimeDateEnd,
                this.manTimeTimeStart, this.manTimeTimeEnd);
    }

    public boolean isAutoRoom() {
        return autoRoom;
    }

    public void setAutoRoom(boolean autoRoom) {
        this.autoRoom = autoRoom;
    }

    public List<String> getRoomValues() {
        return roomValues;
    }

    public void setRoomValues(List<String> roomValues) {
        this.roomValues = roomValues;
    }

    public Integer getRoomId() {
        return roomId;
    }

    public void setRoomId(Integer roomId) {
        this.roomId = roomId;
    }

    public List<Integer> getAttendees() {
        return attendees;
    }

    public void setAttendees(List<Integer> attendees) {
        this.attendees = attendees;
    }

    public String getAutoTimeDateStart() {
        return autoTimeDateStart;
    }

    public void setAutoTimeDateStart(String autoTimeDateStart) {
        this.autoTimeDateStart = autoTimeDateStart;
    }

    public String getAutoTimeDateEnd() {
        return autoTimeDateEnd;
    }

    public void setAutoTimeDateEnd(String autoTimeDateEnd) {
        this.autoTimeDateEnd = autoTimeDateEnd;
    }

    public Integer getMeetingDuration() {
        return meetingDuration;
    }

    public void setMeetingDuration(Integer meetingDuration) {
        this.meetingDuration = meetingDuration;
    }

    public String getDurationUnit() {
        return durationUnit;
    }

    public void setDurationUnit(String durationUnit) {
        this.durationUnit = durationUnit;
    }
}
