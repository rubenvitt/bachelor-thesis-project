package de.rubeen.bsc.provider.office365.entities;

import de.rubeen.bsc.service.LoggableService;
import org.joda.time.DateTime;
import org.joda.time.DateTimeZone;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

class DateTimeTimeZoneTest extends LoggableService {

    @Test
    void timeConvertingTest() {
        DateTime dateTime = new DateTime();
        LOG.info("dateTime original: {}", dateTime);
        LOG.info("dateTime with changed tz: {}", dateTime.withZone(DateTimeZone.UTC));
        LOG.info("changed tz to localDateTime: {}", dateTime.withZone(DateTimeZone.UTC).toLocalDateTime().toDate());
    }
}