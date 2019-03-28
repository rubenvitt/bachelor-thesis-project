package de.rubeen.bsc.service.provider;

import org.joda.time.DateTime;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.*;

class OfficeProviderServiceTest {

    @Test
    void dateConvertingTest() {
        //PRECONDITIONS
        DateTime dateTime = new DateTime()
                .withYear(2019)
                .withMonthOfYear(1)
                .withDayOfMonth(29)
                .withHourOfDay(8)
                .withMinuteOfHour(14)
                .withSecondOfMinute(12)
                .withMillisOfSecond(133);
        String shouldBe = "2019-01-29T08:14:12.133Z";

        //when...
        var dateToString = OfficeProviderService.convertDateToString(dateTime);

        //then...
        assertThat(dateToString)
                .isEqualTo(shouldBe);
    }

}
