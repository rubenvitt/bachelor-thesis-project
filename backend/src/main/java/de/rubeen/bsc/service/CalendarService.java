package de.rubeen.bsc.service;

import de.rubeen.bsc.entities.db.enums.Calprovider;
import de.rubeen.bsc.entities.db.tables.records.CalendarRecord;
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
}
