package de.rubeen.bsc.service;

import de.rubeen.bsc.entities.db.enums.Calprovider;
import de.rubeen.bsc.entities.db.tables.Calendar;
import de.rubeen.bsc.entities.db.tables.records.CalendarRecord;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import java.sql.SQLException;

import static de.rubeen.bsc.entities.db.Tables.CALENDAR;

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
                .and(CALENDAR.USER_ID.eq(loginService.getUserID(userMail))).executeAsync();
    }
}
