package de.rubeen.bsc.service;

import com.google.api.services.calendar.model.Events;
import de.rubeen.bsc.entities.db.enums.Calprovider;
import de.rubeen.bsc.entities.web.EventEntity;
import de.rubeen.bsc.helper.EventComparatorFactory;
import de.rubeen.bsc.service.provider.GoogleProviderService;
import org.joda.time.DateTime;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import java.io.IOException;
import java.security.GeneralSecurityException;
import java.sql.SQLException;
import java.util.LinkedList;
import java.util.List;
import java.util.Objects;
import java.util.stream.Collectors;

import static de.rubeen.bsc.entities.db.Tables.APPUSER;
import static de.rubeen.bsc.entities.db.Tables.CALENDAR;

@Service
public class EventService extends AbstractDatabaseService {
    private final GoogleProviderService googleProviderService;
    private final LoginService loginService;

    @Autowired
    public EventService(@Value("${database.url}") final String url,
                        @Value("${database.user}") final String user,
                        @Value("${database.pass}") final String password, GoogleProviderService googleProviderService, LoginService loginService) throws SQLException {
        super(url, user, password);
        this.googleProviderService = googleProviderService;
        this.loginService = loginService;
    }

    public List<EventEntity> getAllEventsForUser(String userMail) {
        return this.getAllEventsForUser(userMail, getBeginOfToday().getMillis(), getEndOfToday().getMillis());
    }

    public List<EventEntity> getAllEventsForUser(String userMail, Long startMillis, Long endMillis) {
        final DateTime startTime = new DateTime(startMillis),
                endTime = new DateTime(endMillis);
        final Integer userID = loginService.getUserID(userMail);
        final List<String> googleCalendars = dslContext
                .select(CALENDAR.CALENDARID)
                .from(CALENDAR)
                .innerJoin(APPUSER).onKey()
                .where(APPUSER.ID.eq(userID))
                .and(CALENDAR.PROVIDER.eq(Calprovider.google))
                .and(CALENDAR.ACTIVATED.eq(true)).fetch(CALENDAR.CALENDARID);
        LOG.info("Found following active calendars: ");
        googleCalendars.parallelStream().forEach(LOG::info);

        final List<Events> eventsList = googleCalendars.parallelStream()
                .map(calendar -> {
                    try {
                        return googleProviderService.getEvents(userMail,
                                calendar,
                                startTime,
                                endTime);
                    } catch (IOException | GeneralSecurityException e) {
                        LOG.error("Can't get events for user: {} - calendar: {}", userMail, calendar, e);
                        return null;
                    }
                }).filter(Objects::nonNull)
                .collect(Collectors.toList());
        List<EventEntity> eventEntities = new LinkedList<>();
        eventsList.forEach(events -> eventEntities.addAll(
                events.getItems().parallelStream()
                        .map(event -> new EventEntity(event.getSummary(), new DateTime(event.getStart().getDateTime().toString()), new DateTime(event.getEnd().getDateTime().toString())))
                        .collect(Collectors.toList())
        ));
        eventEntities.sort(EventComparatorFactory.getDateComparator());
        return eventEntities;
    }

    private DateTime getEndOfToday() {
        return DateTime.now().withHourOfDay(23).withMinuteOfHour(59).withSecondOfMinute(59).withMillisOfSecond(999);
    }

    private DateTime getBeginOfToday() {
        return DateTime.now().withHourOfDay(0).withMinuteOfHour(0).withSecondOfMinute(0).withMillisOfSecond(0);
    }
}
