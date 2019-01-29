package de.rubeen.bsc.service;

import de.rubeen.bsc.entities.db.enums.Calprovider;
import de.rubeen.bsc.entities.db.tables.records.CalendarRecord;
import de.rubeen.bsc.entities.web.CalendarEntity;
import de.rubeen.bsc.service.provider.CalendarProvider;
import de.rubeen.bsc.service.provider.GoogleProviderService;
import de.rubeen.bsc.service.provider.OfficeProviderService;
import org.modelmapper.ModelMapper;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.stream.Collectors;

import static de.rubeen.bsc.entities.db.Tables.CALENDAR;

@Service
public class ProviderService extends LoggableService {

    private final DatabaseService databaseService;
    private final LoginService loginService;
    private final GoogleProviderService googleProviderService;
    private final OfficeProviderService officeProviderService;
    private final ModelMapper modelMapper = new ModelMapper();

    public ProviderService(DatabaseService databaseService, LoginService loginService, GoogleProviderService googleProviderService, OfficeProviderService officeProviderService) {
        this.databaseService = databaseService;
        this.loginService = loginService;
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


    public List<CalendarEntity> getAllCalendarEntities(String userMail) {
        LOG.info("Getting all calendarEntities for {}", userMail);
        return databaseService.getContext().select()
                .from(CALENDAR)
                .where(CALENDAR.USER_ID.eq(loginService.getUserID(userMail)))
                .and(CALENDAR.ACTIVATED.eq(true))
                .fetch()
                .map(record -> modelMapper.map(record, CalendarRecord.class))
                .parallelStream()
                .map(calendarRecord -> getCalendarProvider(calendarRecord.getCalendarid())
                        .getCalendar(calendarRecord.getCalendarid(), userMail, calendarRecord.getActivated()))
                .collect(Collectors.toList());
    }
}
