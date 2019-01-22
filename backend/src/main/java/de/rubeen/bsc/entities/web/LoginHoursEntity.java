package de.rubeen.bsc.entities.web;

import com.fasterxml.jackson.annotation.JsonGetter;
import org.joda.time.LocalTime;

import java.text.MessageFormat;

public class LoginHoursEntity {
    private Integer id;
    private String startTime, endTime;
    private boolean monday, tuesday, wednesday, thursday, friday, saturday, sunday;

    public LoginHoursEntity(Integer id, String startTime, String endTime, boolean monday, boolean tuesday, boolean wednesday, boolean thursday, boolean friday, boolean saturday, boolean sunday) {
        this.id = id;
        this.startTime = startTime;
        this.endTime = endTime;
        this.monday = monday;
        this.tuesday = tuesday;
        this.wednesday = wednesday;
        this.thursday = thursday;
        this.friday = friday;
        this.saturday = saturday;
        this.sunday = sunday;
    }

    public LoginHoursEntity() {
    }

    @JsonGetter
    public Integer getId() {
        return id;
    }

    public void setId(Integer id) {
        this.id = id;
    }

    @JsonGetter
    public String getStartTime() {
        if (startTime.length() == 5)
            return startTime + ":00";
        return startTime;
    }

    public void setStartTime(String startTime) {
        this.startTime = startTime;
    }

    @JsonGetter
    public String getEndTime() {
        if (endTime.length() == 5)
            return endTime + ":00";
        return endTime;
    }

    public void setEndTime(String endTime) {
        this.endTime = endTime;
    }

    @JsonGetter
    public boolean isMonday() {
        return monday;
    }

    public void setMonday(boolean monday) {
        this.monday = monday;
    }

    @JsonGetter
    public boolean isTuesday() {
        return tuesday;
    }

    public void setTuesday(boolean tuesday) {
        this.tuesday = tuesday;
    }

    @JsonGetter
    public boolean isWednesday() {
        return wednesday;
    }

    public void setWednesday(boolean wednesday) {
        this.wednesday = wednesday;
    }

    @JsonGetter
    public boolean isThursday() {
        return thursday;
    }

    public void setThursday(boolean thursday) {
        this.thursday = thursday;
    }

    @JsonGetter
    public boolean isFriday() {
        return friday;
    }

    public void setFriday(boolean friday) {
        this.friday = friday;
    }

    @JsonGetter
    public boolean isSaturday() {
        return saturday;
    }

    public void setSaturday(boolean saturday) {
        this.saturday = saturday;
    }

    @JsonGetter
    public boolean isSunday() {
        return sunday;
    }

    public void setSunday(boolean sunday) {
        this.sunday = sunday;
    }

    @Override
    public String toString() {
        LocalTime start = new LocalTime(this.getStartTime());
        LocalTime end = new LocalTime(this.getEndTime());
        String dayString = (this.isMonday() ? "Mon " : "") +
                (this.isTuesday() ? "Tue " : "") +
                (this.isWednesday() ? "Wed " : "") +
                (this.isThursday() ? "Thr " : "") +
                (this.isFriday() ? "Fri " : "") +
                (this.isSaturday() ? "Sat " : "") +
                (this.isSunday() ? "Sun " : "");
        return MessageFormat.format("{0} {1} - {2}", dayString, start, end);
    }
}
