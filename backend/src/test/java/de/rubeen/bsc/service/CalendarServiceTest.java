package de.rubeen.bsc.service;

import org.assertj.core.api.Condition;
import org.joda.time.DateTime;
import org.joda.time.Interval;
import org.joda.time.LocalTime;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.mockito.Mock;

import java.sql.SQLException;
import java.util.*;
import java.util.stream.Stream;

import static org.assertj.core.api.Assertions.anyOf;
import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.MockitoAnnotations.initMocks;

class CalendarServiceTest extends LoggableService {

    @Mock
    LoginService loginService;
    @Mock
    DatabaseService databaseService;
    private CalendarService calendarService;

    @BeforeEach
    void setup() throws SQLException {
        initMocks(this);
        calendarService = new CalendarService(loginService, databaseService);
    }

    @Test
    @DisplayName("Easy freeTimes should be calculated correctly")
    void calculateFreeTimeWith() {
        //#0: (no meeting)
        DateTime startWorkingInterval = DateTime.now().withTime(getTime("08:00")),
                endWorkingInterval = DateTime.now().withTime(getTime("16:00"));
        Stream<Interval> workingIntervals = List.of(
                new Interval(startWorkingInterval, endWorkingInterval)
        ).stream();
        Collection<Interval> busyIntervals = List.of();
        Collection<Interval> result0 = calendarService.calculateFreeTimeWith(workingIntervals, busyIntervals);
        //#1: (meeting in workingTime)
        DateTime busy1Start = DateTime.now().withTime(getTime("14:00")),
                busy1End = DateTime.now().withTime(getTime("15:00"));
        workingIntervals = List.of(
                new Interval(startWorkingInterval, endWorkingInterval)
        ).stream();
        busyIntervals = List.of(
                new Interval(busy1Start, busy1End)
        );
        Collection<Interval> result1 = calendarService.calculateFreeTimeWith(workingIntervals, busyIntervals);

        //#2: (meeting starts in workingTime and ends after)
        DateTime busy2Start = DateTime.now().withTime(getTime("15:00")),
                busy2End = DateTime.now().withTime(getTime("18:00"));
        workingIntervals = List.of(
                new Interval(startWorkingInterval, endWorkingInterval)
        ).stream();
        busyIntervals = List.of(
                new Interval(busy2Start, busy2End)
        );
        Collection<Interval> result2 = calendarService.calculateFreeTimeWith(workingIntervals, busyIntervals);

        //#3: (meeting starts before workingTime and ends in time)
        DateTime busy3Start = DateTime.now().withTime(getTime("07:00")),
                busy3End = DateTime.now().withTime(getTime("10:00"));
        workingIntervals = List.of(
                new Interval(startWorkingInterval, endWorkingInterval)
        ).stream();
        busyIntervals = List.of(
                new Interval(busy3Start, busy3End)
        );
        Collection<Interval> result3 = calendarService.calculateFreeTimeWith(workingIntervals, busyIntervals);

        //4: (meeting starts before and ends after workingTime)
        DateTime busy4Start = DateTime.now().withTime(getTime("07:00")),
                busy4End = DateTime.now().withTime(getTime("17:00"));
        workingIntervals = List.of(
                new Interval(startWorkingInterval, endWorkingInterval)
        ).stream();
        busyIntervals = List.of(
                new Interval(busy4Start, busy4End)
        );
        Collection<Interval> result4 = calendarService.calculateFreeTimeWith(workingIntervals, busyIntervals);

        assertThat(result0)
                .size().isEqualTo(1);
        assertThat(result0)
                .extracting(interval -> {
                    assertThat(interval.getStart())
                            .isEqualTo(startWorkingInterval);
                    assertThat(interval.getEnd())
                            .isEqualTo(endWorkingInterval);
                    return true;
                }).containsExactly(true);
        assertThat(result1)
                .size().isEqualTo(2);
        assertThat(result1)
                .extracting(interval -> {
                    assertThat(interval.getStart())
                            .has(anyOf(
                                    new Condition<>(o -> interval.getStart().equals(startWorkingInterval) ||
                                            interval.getStart().equals(busy1End), "interval == workingStart || busyTimeEnd")
                            ));
                    return true;
                }).containsExactly(true, true);

        assertThat(result2)
                .size().isEqualTo(1);
        assertThat(result2)
                .extracting(interval -> {
                    assertThat(interval.getEnd())
                            .isEqualTo(busy2Start);
                    return true;
                }).containsExactly(true);

        assertThat(result3)
                .size().isEqualTo(1);
        assertThat(result3)
                .extracting(interval -> {
                    assertThat(interval.getStart())
                            .isEqualTo(busy3End);
                    return true;
                }).containsExactly(true);
        assertThat(result4)
                .isEmpty();
    }

    @Test
    @DisplayName("difficult freeTimes should be calculated correctly")
    void calculateFreeTimes() {
        //#1: is to easy, we don't need additional tests
        //#4: is to easy, no need for additional tests
        DateTime workingHourStart = DateTime.now().withTime(getTime("08:00")),
                workingHourEnd = DateTime.now().withTime(getTime("16:00"));
        DateTime busy2Start = DateTime.now().withTime(getTime("14:00")),
                busy2End = DateTime.now().withTime(getTime("16:00"));
        DateTime busy3Start = DateTime.now().withTime(getTime("08:00")),
                busy3End = DateTime.now().withTime(getTime("10:00"));

        //#2: meeting-end == workingHour-end
        Stream<Interval> workingIntervals = List.of(
                new Interval(workingHourStart, workingHourEnd)
        ).stream();
        Collection<Interval> busyIntervals = List.of(
                new Interval(busy2Start, busy2End)
        );
        Collection<Interval> result1 = calendarService.calculateFreeTimeWith(workingIntervals, busyIntervals);

        //#3: meeting-start == workingHour-start
        workingIntervals = List.of(
                new Interval(workingHourStart, workingHourEnd)
        ).stream();
        busyIntervals = List.of(
                new Interval(busy3Start, busy3End)
        );
        Collection<Interval> result2 = calendarService.calculateFreeTimeWith(workingIntervals, busyIntervals);

        System.out.println(result1);
        System.out.println(result2);
        assertThat(result1)
                .size().isEqualTo(1);
        assertThat(result1.iterator().next().getEnd())
                .isNotEqualTo(workingHourEnd);
        assertThat(result2)
                .size().isEqualTo(1);
        assertThat(result2.iterator().next().getStart())
                .isNotEqualTo(workingHourStart);
    }

    @Test
    @DisplayName("Should calculate free-times correctly")
    void calculateFreeTimesWithTest() {
        /*
        3 busy-times:
        #1: 4.2. 6:45 - 4.2. 13:00
        #2: 4.2. 12:00 - 8.2. 13:00
        #3: 4.2. 13:00 - 4.2. 14:00
         */
        List<Interval> busyTimesBlocking = List.of(
                Interval.parse("2019-02-04T06:45:00.000+01:00/2019-02-04T13:00:00.000+01:00"),
                Interval.parse("2019-02-04T12:00:00.000+01:00/2019-02-08T13:00:00.000+01:00"),
                Interval.parse("2019-02-04T13:00:00.000+01:00/2019-02-04T14:00:00.000+01:00")
        );
        List<Interval> workingIntervals = List.of(
                Interval.parse("2019-02-04T08:00:00.000+01:00/2019-02-04T16:00:00.000+01:00"),
                Interval.parse("2019-02-07T08:00:00.000+01:00/2019-02-07T16:00:00.000+01:00")
        );

        List<Interval> busyTimesNotBlocking = List.of(
                Interval.parse("2019-02-04T06:45:00.000+01:00/2019-02-04T13:00:00.000+01:00"),
                //Interval.parse("2019-02-04T12:00:00.000+01:00/2019-02-08T13:00:00.000+01:00"),
                Interval.parse("2019-02-04T13:00:00.000+01:00/2019-02-04T14:00:00.000+01:00")
        );

        final Collection<Interval> intervalsBlocked = calendarService.calculateFreeTimeWith(workingIntervals.stream(), busyTimesBlocking);
        final Collection<Interval> intervalsNotBlocked = calendarService.calculateFreeTimeWith(workingIntervals.stream(), busyTimesNotBlocking);

        LOG.info("got {} freeTimes in following blockedIntervals:", intervalsBlocked.size());
        intervalsBlocked.forEach(interval -> LOG.info("{} ({}) - {} ({})",
                interval.getStart().toLocalDate(), interval.getStart().toLocalTime(),
                interval.getEnd().toLocalDate(), interval.getEnd().toLocalTime()));

        LOG.info("got {} freeTimes in following notBlockedIntervals:", intervalsNotBlocked.size());
        intervalsNotBlocked.forEach(interval -> LOG.info("{} ({}) - {} ({})",
                interval.getStart().toLocalDate(), interval.getStart().toLocalTime(),
                interval.getEnd().toLocalDate(), interval.getEnd().toLocalTime()));

        assertThat(intervalsBlocked)
                .hasSize(0);
        assertThat(intervalsNotBlocked)
                .hasSize(2);
    }

    private LocalTime getTime(final String time) {
        return new LocalTime(time);
    }

}