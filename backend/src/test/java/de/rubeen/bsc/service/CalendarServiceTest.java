package de.rubeen.bsc.service;

import de.rubeen.bsc.entities.web.LoginHoursEntity;
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
import java.util.concurrent.atomic.AtomicReference;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import static java.util.stream.Collectors.toList;
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
    void calculateFreeTimesWithTest() {
        /*
        3 busy-times:
        #1: 4.2. 6:45 - 4.2. 13:00
        #2: 4.2. 12:00 - 8.2. 13:00
        #3: 4.2. 13:00 - 4.2. 14:00
         */
        List<Interval> busyTimes = List.of(
                Interval.parse("2019-02-04T06:45:00.000+01:00/2019-02-04T13:00:00.000+01:00"),
                Interval.parse("2019-02-04T12:00:00.000+01:00/2019-02-08T13:00:00.000+01:00"),
                Interval.parse("2019-02-04T13:00:00.000+01:00/2019-02-04T14:00:00.000+01:00")
        );
        Interval searchBetween = new Interval(
                new DateTime().withYear(2019).withMonthOfYear(2).withDayOfMonth(3).withHourOfDay(0),
                new DateTime().withYear(2019).withMonthOfYear(2).withDayOfMonth(9).withHourOfDay(23));
        //Working-hours:
        //monday & thursday 8:00 - 16:00
        List<LoginHoursEntity> workingHours = List.of(new LoginHoursEntity(null, "08:00", "16:00",
                true, false, false,
                true, false, false, false));
        List<Interval> workingIntervals = List.of(
                Interval.parse("2019-02-04T08:00:00.000+01:00/2019-02-04T16:00:00.000+01:00"),
                Interval.parse("2019-02-07T08:00:00.000+01:00/2019-02-07T16:00:00.000+01:00")
        );

        final Collection<Interval> intervals = calendarService.calculateFreeTimeWith(workingIntervals.stream(), busyTimes);

        LOG.info("got {} freeTimes in following intervals:", intervals.size());
        intervals.forEach(interval -> LOG.info("{} ({}) - {} ({})",
                interval.getStart().toLocalDate(), interval.getStart().toLocalTime(),
                interval.getEnd().toLocalDate(), interval.getEnd().toLocalTime()));

        assertThat(intervals)
                .hasSize(0);
    }

    @Test
    void newCalculationMethod() {
        /*
        3 busy-times:
        #1: 4.2. 6:45 - 4.2. 13:00
        #2: 4.2. 12:00 - 8.2. 13:00
        #3: 4.2. 13:00 - 4.2. 14:00
         */
        List<Interval> busyTimes = List.of(
                Interval.parse("2019-02-04T06:45:00.000+01:00/2019-02-04T13:00:00.000+01:00"),
                //Interval.parse("2019-02-04T12:00:00.000+01:00/2019-02-08T13:00:00.000+01:00"),
                Interval.parse("2019-02-04T13:00:00.000+01:00/2019-02-04T14:00:00.000+01:00")
        );
        Interval searchBetween = new Interval(
                new DateTime().withYear(2019).withMonthOfYear(2).withDayOfMonth(3).withHourOfDay(0),
                new DateTime().withYear(2019).withMonthOfYear(2).withDayOfMonth(9).withHourOfDay(23));
        //Working-hours:
        //monday & thursday 8:00 - 16:00
        List<LoginHoursEntity> workingHours = List.of(new LoginHoursEntity(null, "08:00", "16:00",
                true, false, false,
                true, false, false, false));
        List<Interval> workingIntervals = List.of(
                Interval.parse("2019-02-04T08:00:00.000+01:00/2019-02-04T16:00:00.000+01:00"),
                Interval.parse("2019-02-07T08:00:00.000+01:00/2019-02-07T16:00:00.000+01:00")
        );

        final Collection<Interval> intervals = calculateFreeTimesWith(workingIntervals, busyTimes);

        LOG.info("got {} freeTimes in following intervals:", intervals.size());
        intervals.forEach(interval -> LOG.info("{} ({}) - {} ({})",
                interval.getStart().toLocalDate(), interval.getStart().toLocalTime(),
                interval.getEnd().toLocalDate(), interval.getEnd().toLocalTime()));

        assertThat(intervals)
                .hasSize(0);
    }

    private Collection<Interval> calculateFreeTimesWith(List<Interval> workingIntervals, List<Interval> busyTimes) {
        LOG.info("Got {} busyTimes: {}", busyTimes.size(), busyTimes);
        return workingIntervals.stream()
                .map(interval -> calculate(interval, busyTimes))
                .flatMap(Collection::stream)
                .collect(Collectors.toCollection(HashSet::new));
    }

    private Collection<Interval> calculate(Interval workingInterval, List<Interval> busyTimes) {
        final List<List<Interval>> freeIntervalsPerBusyTime = busyTimes.stream()
                .map(busyInterval -> calculateForInterval(workingInterval, busyInterval))
                .collect(toList());
        LOG.info("Get union of time-intervals: {}", freeIntervalsPerBusyTime);
        return getUnionOfTimeIntervals(freeIntervalsPerBusyTime);
        //return List.of(new Interval(DateTime.now(), DateTime.now()));
    }

    private Collection<Interval> getUnionOfTimeIntervals(List<List<Interval>> freeIntervalsPerBusyTime) {
        final List<Interval> firstIntervals = freeIntervalsPerBusyTime.stream()
                .map(intervals -> {
                    if (intervals.size() == 1)
                        return intervals;
                    else if (intervals.size() == 2)
                        return List.of(intervals.get(0));
                    return null;
                }).filter(Objects::nonNull)
                .flatMap(List::stream)
                .collect(toList());

        Map<DateTime, Interval> freePerDayFirst = freeIntervalsPerBusyTime.stream()
                .map(intervals -> {
                    if (intervals.size() == 1)
                        return intervals;
                    else if (intervals.size() == 2)
                        return List.of(intervals.get(0));
                    return null;
                }).filter(Objects::nonNull)
                .flatMap(Collection::stream)
                .collect(Collectors.toMap(interval -> new DateTime().withDate(1, 1, 1), interval -> interval, (o, o2) -> o));
        //.collect(Collectors.toMap(interval -> new DateTime().withDate(interval.getStart().toLocalDate()), interval -> interval, (o, o2) -> o));

        Map<DateTime, Interval> freePerDaySecond = freeIntervalsPerBusyTime.stream()
                .map(intervals -> {
                    if (intervals.size() == 1)
                        return intervals;
                    else if (intervals.size() == 2)
                        return List.of(intervals.get(1));
                    return null;
                }).filter(Objects::nonNull)
                .flatMap(Collection::stream)
                .collect(Collectors.toMap(interval -> new DateTime().withDate(1, 1, 1), interval -> interval, (o, o2) -> o));
        //.collect(Collectors.toMap(interval -> new DateTime().withDate(interval.getStart().toLocalDate()), interval -> interval, (o, o2) -> o));

        final List<Interval> secondIntervals = freeIntervalsPerBusyTime.stream()
                .map(intervals -> {
                    if (intervals.size() == 1)
                        return intervals;
                    else if (intervals.size() == 2)
                        return List.of(intervals.get(1));
                    return null;
                }).filter(Objects::nonNull)
                .flatMap(List::stream)
                .collect(toList());

        List<Interval> resultList = new LinkedList<>();

        if (freePerDayFirst.size() > 0) {
            AtomicReference<DateTime> start = new AtomicReference<>();
            AtomicReference<DateTime> end = new AtomicReference<>();
            freePerDayFirst.forEach(
                    (mapDateTime, interval) -> {
                        start.getAndUpdate(dateTime ->
                                dateTime == null || dateTime.isBefore(interval.getStart())
                                        ? interval.getStart()
                                        : dateTime);
                        end.getAndUpdate(dateTime ->
                                dateTime == null || dateTime.isAfter(interval.getEnd())
                                        ? interval.getEnd()
                                        : dateTime);
                    });
            if (start.get().isBefore(end.get()))
                resultList.add(new Interval(start.get(), end.get()));

            LOG.info("Start of first list: {}", start.get());
            LOG.info("end of first list: {}", end.get());
        }
        if (freePerDayFirst.size() > 0) {
            AtomicReference<DateTime> start = new AtomicReference<>();
            AtomicReference<DateTime> end = new AtomicReference<>();
            freePerDaySecond.forEach(
                    (mapDateTime, interval) -> {
                        start.getAndUpdate(dateTime ->
                                dateTime == null || dateTime.isBefore(interval.getStart())
                                        ? interval.getStart()
                                        : dateTime);
                        end.getAndUpdate(dateTime ->
                                dateTime == null || dateTime.isAfter(interval.getEnd())
                                        ? interval.getEnd()
                                        : dateTime);
                    });
            if (start.get().isBefore(end.get()))
                resultList.add(new Interval(start.get(), end.get()));

            LOG.info("Start of second list: {}", start.get());
            LOG.info("end of second list: {}", end.get());
        }
        if (resultList.size() == 2) {
            if (resultList.get(0).getEnd().equals(resultList.get(1).getEnd())) {
                if (resultList.get(0).getStart().equals(resultList.get(1).getStart())
                        || resultList.get(1).getStart().isBefore(resultList.get(0).getStart())) {
                    resultList.remove(1);
                } else if (resultList.get(0).getStart().isBefore(resultList.get(1).getStart()))
                    resultList.remove(0);
            }
            else if (resultList.get(0).getStart().equals(resultList.get(1).getStart())) {
                if (resultList.get(0).getEnd().equals(resultList.get(1).getEnd())
                || resultList.get(1).getEnd().isAfter(resultList.get(0).getEnd())) {
                    resultList.remove(1);
                } else if (resultList.get(0).getEnd().isAfter(resultList.get(1).getEnd()))
                    resultList.remove(0);
            }
        }
        return resultList;
    }

    private List<Interval> calculateForInterval(Interval workingInterval, Interval busyInterval) {
        BusyBlockingState blockingState = getBlockingState(workingInterval, busyInterval);
        LOG.info("BlockingState for {} in {} is: {}", busyInterval, workingInterval, blockingState);
        switch (blockingState) {
            case INNER:
                LOG.debug("{} is in {}", busyInterval, workingInterval);
                return List.of(new Interval(workingInterval.getStart(), busyInterval.getStart()),
                        new Interval(busyInterval.getEnd(), workingInterval.getEnd()));
            case START_BEFORE:
                LOG.debug("{} starts before and ends in {}", busyInterval, workingInterval);
                return List.of(new Interval(busyInterval.getEnd(), workingInterval.getEnd()));
            case END_AFTER:
                LOG.debug("{} starts in and ends after {}", busyInterval, workingInterval);
                return List.of(new Interval(workingInterval.getStart(), busyInterval.getStart()));
            case BLOCKING:
                LOG.debug("{} starts before and ends after {}", busyInterval, workingInterval);
                return Collections.emptyList();
            case OUTER:
                LOG.debug("{} is not in {}", busyInterval, workingInterval);
                return List.of(workingInterval);
            default:
                Error error = new AssertionError("This switch should not have a default-way!");
                LOG.error("should not happen!", error);
                throw error;
        }
        /*
        //#4:
                    final Optional<Interval> anyOverlapping = busyIntervals.parallelStream()
                            .filter(busyInterval -> busyInterval.contains(workingInterval))
                            .findAny();

                    if (anyOverlapping.isPresent()) {
                        LOG.info("#4: Working times {} were illuminated by busyTime", workingInterval);
                        return new LinkedList<Interval>();
                    }

                    //#1:
                    busyIntervals.parallelStream()
                            .filter(workingInterval::contains)
                            .forEach(busyInterval -> {
                                //meeting is in workingTime
                                LOG.info("#1: {} contains {}", workingInterval, busyInterval);
                                if (!workingInterval.getStart().equals(busyInterval.getStart()))
                                    workingDayIntervals.add(new Interval(workingInterval.getStart(), busyInterval.getStart()));
                                if (!workingInterval.getEnd().equals(busyInterval.getEnd()))
                                    workingDayIntervals.add(new Interval(busyInterval.getEnd(), workingInterval.getEnd()));
                                meetingAtThisDay.set(true);
                            });
                    //#2:
                    busyIntervals.parallelStream()
                            .filter(busyInterval -> workingInterval.contains(busyInterval.getStart()))
                            .filter(busyInterval -> busyInterval.getEnd().isAfter(workingInterval.getEnd()))
                            .forEach(busyInterval -> {
                                //meeting begins in workingTime & ends after workingTime
                                LOG.info("#2: {} starts in and ends after {}", busyInterval, workingInterval);
                                workingDayIntervals.add(new Interval(workingInterval.getStart(), busyInterval.getStart()));
                                meetingAtThisDay.set(true);
                            });
                    //#3:
                    busyIntervals.parallelStream()
                            .filter(busyInterval -> workingInterval.contains(busyInterval.getEnd()))
                            .filter(busyInterval -> busyInterval.getStart().isBefore(workingInterval.getStart()))
                            .forEach(busyInterval -> {
                                //meeting begins before workingTime & ends in workingTime
                                LOG.info("#3: {} starts before and ends in {}", busyInterval, workingInterval);
                                workingDayIntervals.add(new Interval(busyInterval.getEnd(), workingInterval.getEnd()));
                                meetingAtThisDay.set(true);
                            });
         */
    }

    private BusyBlockingState getBlockingState(Interval workingInterval, Interval busyInterval) {
        return
                busyInterval.contains(workingInterval) ? BusyBlockingState.BLOCKING
                        : workingInterval.contains(busyInterval) ? BusyBlockingState.INNER
                        : workingInterval.contains(busyInterval.getStart()) ? BusyBlockingState.END_AFTER
                        : workingInterval.contains(busyInterval.getEnd()) ? BusyBlockingState.START_BEFORE
                        : BusyBlockingState.OUTER;
    }

    private LocalTime getTime(final String time) {
        return new LocalTime(time);
    }

    enum BusyBlockingState {
        INNER, OUTER, START_BEFORE, END_AFTER, BLOCKING
    }
}