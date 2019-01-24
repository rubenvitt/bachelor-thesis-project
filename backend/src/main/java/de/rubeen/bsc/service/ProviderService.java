package de.rubeen.bsc.service;

import de.rubeen.bsc.entities.db.enums.Calprovider;
import de.rubeen.bsc.service.provider.CalendarProvider;
import de.rubeen.bsc.service.provider.GoogleProviderService;
import org.springframework.stereotype.Service;

import static de.rubeen.bsc.entities.db.Tables.CALENDAR;

@Service
public class ProviderService extends LoggableService {

    private final DatabaseService databaseService;
    private final GoogleProviderService googleProviderService;

    public ProviderService(DatabaseService databaseService, GoogleProviderService googleProviderService) {
        this.databaseService = databaseService;
        this.googleProviderService = googleProviderService;
    }

    CalendarProvider getCalendarProvider(String calendarId) {
        Calprovider calprovider = databaseService.getContext()
                .select(CALENDAR.PROVIDER)
                .from(CALENDAR)
                .where(CALENDAR.CALENDARID.eq(calendarId))
                .fetchOneInto(Calprovider.class);
        LOG.info("got calProvider: {}", calprovider);
        return googleProviderService;
    }
}
