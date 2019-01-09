package de.rubeen.bsc.entities.web;

import com.fasterxml.jackson.annotation.JsonGetter;
import com.fasterxml.jackson.annotation.JsonSetter;

import javax.annotation.Nullable;

import static java.text.MessageFormat.format;

public class NewEventEntity {
    private String subject, description;
    private boolean autoTime;
    private String manTimeDateStart, manTimeDateEnd;
    private String manTimeTimeStart, manTimeTimeEnd;

    public NewEventEntity() {}

    public NewEventEntity(String subject, String description, boolean autoTime) {
        this.subject = subject;
        this.description = description;
        this.autoTime = autoTime;
    }

    public NewEventEntity(String subject, String description, boolean autoTime,
                          String manTimeDateStart, String manTimeDateEnd, String manTimeTimeStart, String manTimeTimeEnd) {
        this.subject = subject;
        this.description = description;
        this.autoTime = autoTime;
        this.manTimeDateStart = manTimeDateStart;
        this.manTimeDateEnd = manTimeDateEnd;
        this.manTimeTimeStart = manTimeTimeStart;
        this.manTimeTimeEnd = manTimeTimeEnd;
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
}
