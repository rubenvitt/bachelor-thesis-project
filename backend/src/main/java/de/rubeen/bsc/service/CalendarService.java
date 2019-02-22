package de.rubeen.bsc.service;

import de.rubeen.bsc.entities.db.enums.Calprovider;
import de.rubeen.bsc.entities.db.tables.records.CalendarRecord;
import de.rubeen.bsc.entities.web.CalendarEntity;
import de.rubeen.bsc.entities.web.LoginHoursEntity;
import org.joda.time.DateTime;
import org.joda.time.Interval;
import org.joda.time.LocalTime;
import org.springframework.stereotype.Service;

import java.time.DayOfWeek;
import java.util.*;
import java.util.concurrent.atomic.AtomicReference;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import static de.rubeen.bsc.entities.db.Tables.CALENDAR;
import static java.time.DayOfWeek.*;
import static java.util.stream.Collectors.toCollection;
import static java.util.stream.Collectors.toList;

enum BusyBlockingState {
    INNER, OUTER, START_BEFORE, END_AFTER, BLOCKING;

    static boolean isBlockingState(Interval workingInterval, Interval busyInterval) {
        return busyInterval.contains(workingInterval);
    }

    static BusyBlockingState getBlockingState(Interval workingInterval, Interval busyInterval) {
        return
                busyInterval.contains(workingInterval) ? BusyBlockingState.BLOCKING
                        : workingInterval.contains(busyInterval) ? BusyBlockingState.INNER
                        : workingInterval.contains(busyInterval.getStart()) ? BusyBlockingState.END_AFTER
                        : workingInterval.contains(busyInterval.getEnd()) ? BusyBlockingState.START_BEFORE
                        : BusyBlockingState.OUTER;
    }
}

@Service
public class CalendarService extends LoggableService {
    private final LoginService loginService;
    private final DatabaseService databaseService;

    public CalendarService(LoginService loginService, DatabaseService databaseService) {
        this.loginService = loginService;
        this.databaseService = databaseService;
    }

    public void addCalendarToDatabase(String calendarID, String user, Calprovider provider) {
        Integer userID = loginService.getUserID(user);

        Integer allCalendarCount = databaseService.getContext()
                .selectCount().from(CALENDAR)
                .where(CALENDAR.USER_ID.eq(userID))
                .fetchOneInto(int.class);

        if (allCalendarCount < 1) {
            LOG.info("User has no calendars, add first one as default");
            databaseService.getContext()
                    .insertInto(CALENDAR).columns(CALENDAR.CALENDARID, CALENDAR.USER_ID, CALENDAR.ACTIVATED, CALENDAR.PROVIDER, CALENDAR.ISDEFAULT)
                    .values(calendarID, userID, true, provider, true).execute();
        } else {
            boolean calendarExist = databaseService.getContext()
                    .selectCount().from(CALENDAR)
                    .where(CALENDAR.CALENDARID.eq(calendarID))
                    .and(CALENDAR.USER_ID.eq(userID))
                    .fetchOne(0, int.class) > 0;
            if (!calendarExist) {
                databaseService.getContext()
                        .insertInto(CALENDAR).columns(CALENDAR.CALENDARID, CALENDAR.USER_ID, CALENDAR.ACTIVATED, CALENDAR.PROVIDER)
                        .values(calendarID, userID, true, provider).execute();
            }
        }
    }

    public boolean isCalendarActivated(String id, String user) {
        CalendarRecord calendar = databaseService.getContext()
                .selectFrom(CALENDAR)
                .where(CALENDAR.CALENDARID.eq(id))
                .and(CALENDAR.USER_ID.eq(loginService.getUserID(user)))
                .fetchOne();
        return calendar.getActivated();
    }

    public void setCalendarState(String calendarID, String userMail, boolean state) {
        databaseService.getContext().update(CALENDAR)
                .set(CALENDAR.ACTIVATED, state)
                .where(CALENDAR.CALENDARID.eq(calendarID))
                .and(CALENDAR.USER_ID.eq(loginService.getUserID(userMail.replace("%40", "@")))).executeAsync();
    }

    Collection<Interval> getFreeTimes(Stream<Interval> busyTimePeriods, Stream<LoginHoursEntity> workingHours, DateTime start, DateTime end) {
        LOG.debug("Searching for available meeting suggestions with busyTimes & workingHours between {} and {}",
                start.toLocalDate(), end.toLocalDate());
        final Interval initInterval = new Interval(start, end);
        final List<Interval> busyIntervals = busyTimePeriods
                .collect(toList());
        List<LoginHoursEntity> workingHoursList = workingHours.collect(Collectors.toList());
        LOG.info("Got workingHours: {}", workingHoursList);
        final Stream<Interval> workingIntervals = workingHoursList.stream()
                .map(loginHoursEntity -> {
                    List<Interval> intervals = new LinkedList<>();
                    List<DateTime> days = getDaysOfInterval(initInterval, loginHoursEntity.getMonday(), loginHoursEntity.getTuesday(),
                            loginHoursEntity.getWednesday(), loginHoursEntity.getThursday(), loginHoursEntity.getFriday(),
                            loginHoursEntity.getSaturday(), loginHoursEntity.getSunday());
                    final List<DateTime> startDateTimes = days.stream()
                            .map(dateTime -> dateTime.withTime(LocalTime.parse(loginHoursEntity.getStartTime()))).collect(toList());
                    final List<DateTime> endDateTimes = days.stream()
                            .map(dateTime -> dateTime.withTime(LocalTime.parse(loginHoursEntity.getEndTime()))).collect(toList());
                    assert startDateTimes.size() == endDateTimes.size();
                    for (int i = 0; i < startDateTimes.size(); i++) {
                        intervals.add(new Interval(startDateTimes.get(i), endDateTimes.get(i)));
                    }
                    LOG.info("[{}] has {} occurrences in {}", loginHoursEntity, intervals.size(), initInterval);
                    return intervals;
                }).flatMap(List::stream);
        LOG.info("Working-Hours mapped to dateTimes");

        Collection<Interval> intervals = calculateFreeTimeWith(workingIntervals, busyIntervals);
        LOG.info("got {} freeTimes in following intervals:", intervals.size());
        intervals.forEach(interval -> LOG.info("{} ({}) - {} ({})",
                interval.getStart().toLocalDate(), interval.getStart().toLocalTime(),
                interval.getEnd().toLocalDate(), interval.getEnd().toLocalTime()));

        return intervals;
    }

    Collection<Interval> calculateFreeTimeWith(final Stream<Interval> workingIntervals,
                                               final Collection<Interval> busyIntervals) {
        if (busyIntervals.isEmpty())
            return workingIntervals.collect(Collectors.toList());
        return workingIntervals
                .map(workingInterval -> calculateFreeTime(workingInterval, busyIntervals))
                .flatMap(Collection::stream)
                .collect(toCollection(HashSet::new));
    }

    private Collection<Interval> calculateFreeTime(Interval workingInterval, Collection<Interval> busyIntervals) {
        if (busyIntervals.parallelStream().anyMatch(interval -> BusyBlockingState.isBlockingState(workingInterval, interval)))
            return Collections.emptySet();
        final List<List<Interval>> freeIntervalsPerBusyTime = busyIntervals.parallelStream()
                .map(busyInterval -> calculateForInterval(workingInterval, busyInterval))
                .collect(toList());
        LOG.debug("Calculate union of: {}", freeIntervalsPerBusyTime);
        return getUnionOfTimeIntervals(freeIntervalsPerBusyTime);
    }

    private Collection<Interval> getUnionOfTimeIntervals(List<List<Interval>> freeIntervalsPerBusyTime) {
        if (freeIntervalsPerBusyTime.isEmpty())
            return List.of();

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

        if (firstIntervals.size() > 0) {
            //if (freePerDayFirst.size() > 0) {
            AtomicReference<DateTime> start = new AtomicReference<>();
            AtomicReference<DateTime> end = new AtomicReference<>();
            firstIntervals.forEach(interval -> {

                //freePerDayFirst.forEach(
                //(mapDateTime, interval) -> {
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
        if (secondIntervals.size() > 0) {
            //if (freePerDayFirst.size() > 0) {
            AtomicReference<DateTime> start = new AtomicReference<>();
            AtomicReference<DateTime> end = new AtomicReference<>();

            secondIntervals.forEach(interval -> {
                //freePerDaySecond.forEach(
                //        (mapDateTime, interval) -> {
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
            } else if (resultList.get(0).getStart().equals(resultList.get(1).getStart())) {
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
        BusyBlockingState blockingState = BusyBlockingState.getBlockingState(workingInterval, busyInterval);
        LOG.debug("BlockingState for {} in {} is: {}", busyInterval, workingInterval, blockingState);
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
    }

    @SuppressWarnings("Duplicates")
    private List<DateTime> getDaysOfInterval(final Interval initInterval, boolean monday, boolean tuesday, boolean wednesday, boolean thursday, boolean friday, boolean saturday, boolean sunday) {
        final List<DateTime> result = new LinkedList<>();
        if (monday)
            result.addAll(getDaysOfIntervalOfDay(MONDAY, initInterval));
        if (tuesday)
            result.addAll(getDaysOfIntervalOfDay(TUESDAY, initInterval));
        if (wednesday)
            result.addAll(getDaysOfIntervalOfDay(WEDNESDAY, initInterval));
        if (thursday)
            result.addAll(getDaysOfIntervalOfDay(THURSDAY, initInterval));
        if (friday)
            result.addAll(getDaysOfIntervalOfDay(FRIDAY, initInterval));
        if (saturday)
            result.addAll(getDaysOfIntervalOfDay(SATURDAY, initInterval));
        if (sunday)
            result.addAll(getDaysOfIntervalOfDay(SUNDAY, initInterval));
        return result;
    }

    private List<DateTime> getDaysOfIntervalOfDay(final DayOfWeek dayOfWeek, final Interval interval) {
        final DateTime start = interval.getStart();
        final List<DateTime> result = new LinkedList<>();

        long iMax = interval.toDuration().getStandardDays();
        for (int i = 0; i < (iMax > 0 ? iMax : 1); i++) {
            final DateTime dateTime = start.plusDays(i);
            if (dateTime.getDayOfWeek() == dayOfWeek.getValue())
                result.add(dateTime);
        }
        LOG.debug("get days of interval of day: i max={} dayOfWeek={}, interval={}, result={}", interval.toDuration().getStandardDays(), dayOfWeek, interval, result);
        return result;
    }
}