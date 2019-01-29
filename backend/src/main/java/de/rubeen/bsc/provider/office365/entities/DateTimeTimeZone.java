package de.rubeen.bsc.provider.office365.entities;

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import de.rubeen.bsc.service.provider.OfficeProviderService;
import org.joda.time.DateTime;

import java.util.Date;

@JsonIgnoreProperties(ignoreUnknown = true)
public class DateTimeTimeZone {
    private Date dateTime;
    private String timeZone;


    public DateTimeTimeZone() {
    }

    public DateTimeTimeZone(DateTime startDateTime) {
        this.dateTime = startDateTime.toDate();
        this.timeZone = startDateTime.getZone().getID();
    }

    @JsonIgnore
    public Date getDateDateTime() {
        return dateTime;
    }

    public String getDateTime() {
        return OfficeProviderService.convertDateToString(new DateTime(dateTime));
    }


    public void setDateTime(Date dateTime) {
        this.dateTime = dateTime;
    }

    public String getTimeZone() {
        return timeZone;
    }

    public void setTimeZone(String timeZone) {
        this.timeZone = timeZone;
    }
}