package de.rubeen.bsc.service;

import de.rubeen.bsc.entities.db.enums.Calprovider;
import de.rubeen.bsc.service.provider.CalendarProvider;
import de.rubeen.bsc.service.provider.GoogleProviderService;
import de.rubeen.bsc.service.provider.OfficeProviderService;
import org.springframework.stereotype.Service;

import static de.rubeen.bsc.entities.db.Tables.CALENDAR;

@Service
public class ProviderService extends LoggableService {

    private final DatabaseService databaseService;
    private final GoogleProviderService googleProviderService;
    private final OfficeProviderService officeProviderService;

    public ProviderService(DatabaseService databaseService, GoogleProviderService googleProviderService, OfficeProviderService officeProviderService) {
        this.databaseService = databaseService;
        this.googleProviderService = googleProviderService;
        this.officeProviderService = officeProviderService;
    }

    CalendarProvider getCalendarProvider(String calendarId) {
        Calprovider calprovider = databaseService.getContext()
                .select(CALENDAR.PROVIDER)
                .from(CALENDAR)
                .where(CALENDAR.CALENDARID.eq(calendarId))
                .fetchOneInto(Calprovider.class);
        LOG.info("got calProvider: {}", calprovider);
        return calprovider.equals(Calprovider.google) ? googleProviderService : officeProviderService;
    }
}
