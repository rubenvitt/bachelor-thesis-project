package de.rubeen.bsc.service;

import com.google.api.services.calendar.model.TimePeriod;
import de.rubeen.bsc.entities.db.enums.Calprovider;
import de.rubeen.bsc.entities.db.tables.Calendar;
import de.rubeen.bsc.entities.db.tables.records.CalendarRecord;
import de.rubeen.bsc.entities.web.LoginHoursEntity;
import org.joda.time.DateTime;
import org.joda.time.Interval;
import org.joda.time.LocalTime;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import java.sql.SQLException;
import java.time.DayOfWeek;
import java.util.LinkedList;
import java.util.List;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import static de.rubeen.bsc.entities.db.Tables.CALENDAR;
import static java.time.DayOfWeek.*;

@Service
public class CalendarService extends AbstractDatabaseService {

    private final LoginService loginService;

    public CalendarService(@Value("${database.url}") final String url,
                           @Value("${database.user}") final String user,
                           @Value("${database.pass}") final String password, LoginService loginService) throws SQLException {
        super(url, user, password);
        this.loginService = loginService;
    }

    public void addCalendarToDatabase(String calendarID, String user, Calprovider provider) {
        Integer integer = dslContext.selectCount().from(CALENDAR).where(Calendar.CALENDAR.CALENDARID.eq(calendarID)).fetchOne(0, int.class);
        LOG.info("Found: " + integer);
        if (integer < 1) {
            dslContext.insertInto(CALENDAR).columns(CALENDAR.CALENDARID, CALENDAR.USER_ID, CALENDAR.ACTIVATED, CALENDAR.PROVIDER)
                    .values(calendarID, loginService.getUserID(user), true, provider).execute();
        }
    }

    public boolean isCalendarActivated(String id) {
        CalendarRecord calendar = dslContext.selectFrom(CALENDAR).where(CALENDAR.CALENDARID.eq(id)).fetchOne();
        return calendar.getActivated();
    }

    public void setCalendarState(String calendarID, String userMail, boolean state) {
        dslContext.update(CALENDAR)
                .set(CALENDAR.ACTIVATED, state)
                .where(CALENDAR.CALENDARID.eq(calendarID))
                .and(CALENDAR.USER_ID.eq(loginService.getUserID(userMail.replace("%40", "@")))).executeAsync();
    }

    public List<Interval> getFreeTimes(Stream<TimePeriod> busyTimePeriods, Stream<LoginHoursEntity> workingHours, DateTime start, DateTime end) {
        LOG.debug("Searching for available meeting suggestions with busyTimes & workingHours between {} and {}",
                start.toLocalDate(), end.toLocalDate());
        final Stream<Interval> busyInvervals = busyTimePeriods
                .map(timePeriod -> new Interval(timePeriod.getStart().getValue(), timePeriod.getEnd().getValue()));
        final Interval initInterval = new Interval(start, end);

        final Stream<Interval> workingIntervals = workingHours
                .map(loginHoursEntity -> {
                    List<Interval> intervals = new LinkedList<>();
                    List<DateTime> days = getDaysOfInterval(initInterval, loginHoursEntity.isMonday(), loginHoursEntity.isTuesday(),
                            loginHoursEntity.isWednesday(), loginHoursEntity.isThursday(), loginHoursEntity.isFriday(),
                            loginHoursEntity.isSaturday(), loginHoursEntity.isSunday());
                    final List<DateTime> startDateTimes = days.stream()
                            .map(dateTime -> dateTime.withTime(LocalTime.parse(loginHoursEntity.getStartTime()))).collect(Collectors.toList());
                    final List<DateTime> endDateTimes = days.stream()
                            .map(dateTime -> dateTime.withTime(LocalTime.parse(loginHoursEntity.getEndTime()))).collect(Collectors.toList());
                    assert startDateTimes.size() == endDateTimes.size();
                    for (int i = 0; i < startDateTimes.size(); i++) {
                        intervals.add(new Interval(startDateTimes.get(i), endDateTimes.get(i)));
                    }
                    LOG.info("[{}] has {} occurrences in {}", loginHoursEntity, intervals.size(), initInterval);
                    return intervals;
                }).flatMap(List::stream);
        LOG.info("Working-Hours mapped to dateTimes");
        workingIntervals.collect(Collectors.toList());

        //get times, where user is available in a given timeSpan
        return null;
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
        for (int i = 0; i < interval.toDuration().getStandardDays(); i++) {
            final DateTime dateTime = start.plusDays(i);
            if (dateTime.getDayOfWeek() == dayOfWeek.getValue())
                result.add(dateTime);
        }
        return result;
    }
}
