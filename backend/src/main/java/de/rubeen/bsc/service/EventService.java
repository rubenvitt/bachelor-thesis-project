package de.rubeen.bsc.service;

import com.google.api.services.calendar.model.Events;
import de.rubeen.bsc.entities.db.enums.Calprovider;
import de.rubeen.bsc.entities.web.EventEntity;
import de.rubeen.bsc.service.provider.GoogleProviderService;
import org.hibernate.validator.internal.util.logging.formatter.CollectionOfObjectsToStringFormatter;
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

    @Autowired
    public EventService(@Value("${database.url}") final String url,
                        @Value("${database.user}") final String user,
                        @Value("${database.pass}") final String password, GoogleProviderService googleProviderService) throws SQLException {
        super(url, user, password);
        this.googleProviderService = googleProviderService;
    }

    public List<EventEntity> getAllEventsForUser(String userMail) {
        final List<String> googleCalendars = dslContext
                .select(CALENDAR.CALENDARID)
                .from(CALENDAR)
                .innerJoin(APPUSER).onKey()
                .where(APPUSER.MAIL.eq(userMail))
                .and(CALENDAR.PROVIDER.eq(Calprovider.google))
                .and(CALENDAR.ACTIVATED.eq(true)).fetch(CALENDAR.CALENDARID);
        LOG.info("Found following active calendars: ");
        googleCalendars.parallelStream().forEach(LOG::info);

        final List<Events> eventsList = googleCalendars.parallelStream()
                .map(calendar -> {
                    try {
                        return googleProviderService.getEvents(userMail, calendar);
                    } catch (IOException | GeneralSecurityException e) {
                        LOG.error("Can't get events for user: {} - calendar: {}", userMail, calendar, e);
                        return null;
                    }
                }).filter(Objects::nonNull)
                .collect(Collectors.toList());
        List<EventEntity> eventEntities = new LinkedList<>();
        eventsList.parallelStream()
                .forEach(events -> eventEntities.addAll(
                        events.getItems().parallelStream()
                        .map(event -> new EventEntity(event.getSummary(), new DateTime(event.getStart().getDateTime()), new DateTime(event.getEnd().getDateTime())))
                        .collect(Collectors.toList())
                ));
        return eventEntities;
    }
}
