package de.rubeen.bsc.entities.web;


import org.joda.time.DateTime;

public class EventEntity {
    private String subject;
    private DateTime startTime, endTime;

    public EventEntity(String subject, DateTime startTime, DateTime endTime) {
        this.subject = subject;
        this.startTime = startTime;
        this.endTime = endTime;
    }


    public String getSubject() {
        return subject;
    }

    public DateTime getStartTime() {
        return startTime;
    }

    public DateTime getEndTime() {
        return endTime;
    }
}
