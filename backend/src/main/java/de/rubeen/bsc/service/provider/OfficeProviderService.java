package de.rubeen.bsc.service.provider;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import de.rubeen.bsc.entities.db.enums.Calprovider;
import de.rubeen.bsc.entities.provider.CalendarEvent;
import de.rubeen.bsc.entities.web.CalendarEntity;
import de.rubeen.bsc.entities.web.NewEventEntity;
import de.rubeen.bsc.provider.office365.AuthHelper;
import de.rubeen.bsc.provider.office365.OutlookService;
import de.rubeen.bsc.provider.office365.OutlookServiceBuilder;
import de.rubeen.bsc.provider.office365.TokenResponse;
import de.rubeen.bsc.provider.office365.entities.Calendar;
import de.rubeen.bsc.service.CalendarService;
import de.rubeen.bsc.service.DatabaseService;
import de.rubeen.bsc.service.LoggableService;
import de.rubeen.bsc.service.LoginService;
import org.apache.commons.lang3.NotImplementedException;
import org.joda.time.DateTime;
import org.joda.time.Interval;
import org.springframework.stereotype.Service;

import java.io.IOException;
import java.util.Date;
import java.util.List;
import java.util.stream.Collectors;

import static de.rubeen.bsc.entities.db.tables.Credential.CREDENTIAL;

@Service
public class OfficeProviderService extends LoggableService implements CalendarProvider {

    private final DatabaseService databaseService;
    private final LoginService loginService;
    private final ObjectMapper objectMapper = new ObjectMapper();
    private final CalendarService calendarService;

    public OfficeProviderService(DatabaseService databaseService, LoginService loginService, CalendarService calendarService) {
        this.databaseService = databaseService;
        this.loginService = loginService;
        this.calendarService = calendarService;
    }

    @Override
    public boolean createEvent(CalendarEvent calendarEvent, String userId) throws CalendarException {
        return false;
    }

    @Override
    public List<CalendarEntity> getAllCalendars(String user_id) throws CalendarException {
        try {
            final List<Calendar> calendarList = getCalendarFromOffice(user_id);
            calendarList.parallelStream().forEach(calendar -> calendarService.addCalendarToDatabase(calendar.getId(), user_id, Calprovider.office));
            return calendarList.parallelStream()
                    .map(calendar -> new CalendarEntity(calendar.getName(), calendar.getId(), calendarService.isCalendarActivated(calendar.getId())))
                    .collect(Collectors.toList());
        } catch (IOException e) {
            LOG.error("Error while getting calendars");
            throw new CalendarException("Unable to get all calendars", e);
        }
    }

    private void validateToken(TokenResponse token) {
        LOG.info("Validating token...: {}", token.getAccessToken());
        AuthHelper.ensureTokens(token, token.getTokenTendantId());
    }

    @Override
    public List<CalendarEntity> getAllActiveCalendars(String user_id) throws CalendarException {
        try {
            final List<Calendar> calendarList = getCalendarFromOffice(user_id);
            return calendarList.parallelStream()
                    .filter(calendar -> calendarService.isCalendarActivated(calendar.getId()))
                    .map(calendar -> new CalendarEntity(calendar.getName(), calendar.getId(), calendarService.isCalendarActivated(calendar.getId())))
                    .collect(Collectors.toList());
        } catch (IOException e) {
            LOG.error("Error while getting calendars");
            throw new CalendarException("Unable to get all active calendars", e);
        }
    }

    private List<Calendar> getCalendarFromOffice(String user_id) throws IOException {
        TokenResponse token = getToken(user_id);
        LOG.info("Token is valid until...: {}", new DateTime(token.getExpirationTime()));
        validateToken(token);
        OutlookService outlookService = OutlookServiceBuilder.getOutlookService(token.getAccessToken(), user_id);
        Integer maxResults = 20; //nobody should have more than 20 active calendars...
        return outlookService.getCalendars(maxResults).execute().body().getValue();
    }

    @Override
    public List<CalendarEvent> getEventsBetween(Interval interval, String userId, String calendarId) throws CalendarException {
        throw new NotImplementedException("Can't get events, because it wasn't implemented, yet");
    }

    @Override
    public List<Interval> getBusyTimes(String userId, NewEventEntity eventEntity) throws CalendarException {
        throw new NotImplementedException("Can't get busyTimes, because it wasn't implemented, yet");
    }

    public void saveToken(String userMail, TokenResponse tokenResponse) throws JsonProcessingException {
        LOG.info("Save office-token for user {} to database", userMail);
        LOG.debug("Jackson-Converted: {}", objectMapper.writeValueAsString(tokenResponse));
        databaseService.getContext().insertInto(CREDENTIAL)
                .values(loginService.getUserID(userMail), objectMapper.writeValueAsString(tokenResponse), Calprovider.office)
                .onDuplicateKeyUpdate()
                .set(CREDENTIAL.CREDENTIAL_, objectMapper.writeValueAsString(tokenResponse))
                .set(CREDENTIAL.PROVIDER, Calprovider.office)
                .execute();
    }

    public TokenResponse getToken(String userMail) throws IOException {
        LOG.info("Read office-token for user {} from database", userMail);
        String json = databaseService.getContext().select(CREDENTIAL.CREDENTIAL_)
                .from(CREDENTIAL)
                .where(CREDENTIAL.USER_ID.eq(loginService.getUserID(userMail)))
                .fetchSingleInto(String.class);
        LOG.debug("Got JSON from database: {}", json);
        return objectMapper.readValue(json, TokenResponse.class);
    }
}
