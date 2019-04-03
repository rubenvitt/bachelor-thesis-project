package de.rubeen.bsc.service.provider;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import de.rubeen.bsc.entities.db.enums.Calprovider;
import de.rubeen.bsc.entities.db.tables.records.CalendarRecord;
import de.rubeen.bsc.entities.provider.CalendarEvent;
import de.rubeen.bsc.entities.web.AppUserEntity;
import de.rubeen.bsc.entities.web.CalendarEntity;
import de.rubeen.bsc.entities.web.NewEventEntity;
import de.rubeen.bsc.provider.office365.AuthHelper;
import de.rubeen.bsc.provider.office365.OutlookService;
import de.rubeen.bsc.provider.office365.OutlookServiceBuilder;
import de.rubeen.bsc.provider.office365.TokenResponse;
import de.rubeen.bsc.provider.office365.entities.Calendar;
import de.rubeen.bsc.provider.office365.entities.Event;
import de.rubeen.bsc.service.*;
import org.joda.time.DateTime;
import org.joda.time.Interval;
import org.springframework.stereotype.Service;

import java.io.IOException;
import java.util.*;
import java.util.stream.Collectors;

import static de.rubeen.bsc.entities.db.tables.Calendar.CALENDAR;
import static de.rubeen.bsc.entities.db.tables.Credential.CREDENTIAL;

@Service
public class OfficeProviderService extends LoggableService implements CalendarProvider {

    private final DatabaseService databaseService;
    private final LoginService loginService;
    private final UserService userService;
    private final ObjectMapper objectMapper = new ObjectMapper();
    private final CalendarService calendarService;

    public OfficeProviderService(DatabaseService databaseService, LoginService loginService, UserService userService, CalendarService calendarService) {
        this.databaseService = databaseService;
        this.loginService = loginService;
        this.userService = userService;
        this.calendarService = calendarService;
    }

    public static String convertDateToString(DateTime toConvert) {
        return toConvert.toString("YYYY-MM-dd'T'HH:mm:ss.SSS'Z'");
    }

    @Override
    public boolean createEvent(CalendarEvent calendarEvent, String userId) throws CalendarException {
        try {
            TokenResponse tokenResponse = getToken(userId);
            OutlookService outlookService = OutlookServiceBuilder.getOutlookService(tokenResponse.getAccessToken(), userId);
            return outlookService.createEvent(calendarEvent.getCalendarId(), new Event(calendarEvent)).execute().isSuccessful();
        } catch (IOException e) {
            LOG.error("Unable to get token for {}", userId, e);
            throw new CalendarException("Unable to get token for {}" + userId, e);
        }
    }

    @Override
    public List<CalendarEntity> getAllCalendars(String user_id) throws CalendarException {
        try {
            final List<Calendar> calendarList = getCalendarFromOffice(user_id);
            calendarList.parallelStream()
                    .forEachOrdered(calendar ->
                            calendarService.addCalendarToDatabase(calendar.getId(), user_id, Calprovider.office));
            final Map<String, CalendarRecord> recordMap = new HashMap<>();
            databaseService.getContext().selectFrom(CALENDAR)
                    .where(CALENDAR.PROVIDER.eq(Calprovider.office))
                    .and(CALENDAR.USER_ID.eq(loginService.getUserID(user_id)))
                    .fetch().forEach(calendarRecord -> recordMap.put(calendarRecord.getCalendarid(), calendarRecord));

            return calendarList.parallelStream()
                    .map(calendar -> {
                        CalendarRecord calendarRecord = recordMap.get(calendar.getId());
                        return new CalendarEntity(calendar.getName(), calendar.getId(), calendarRecord.getActivated(),
                                calendarRecord.getProvider().getName(), calendarRecord.getIsdefault());
                    }).collect(Collectors.toList());
        } catch (IOException e) {
            LOG.error("Error while getting calendars");
            throw new CalendarException("Unable to get all calendars", e);
        }
    }

    private TokenResponse validateToken(TokenResponse token) {
        LOG.info("Validating token...: {}", token.getAccessToken());
        return AuthHelper.ensureTokens(token, token.getTokenTendantId());
    }

    @Override
    public List<CalendarEntity> getAllActiveCalendars(String user_id) throws CalendarException {
        try {
            final List<Calendar> calendarList = getCalendarFromOffice(user_id);

            Map<String, CalendarRecord> recordMap = new HashMap<>();
            databaseService.getContext().selectFrom(CALENDAR)
                    .where(CALENDAR.PROVIDER.eq(Calprovider.office))
                    .and(CALENDAR.ACTIVATED.isTrue())
                    .and(CALENDAR.USER_ID.eq(loginService.getUserID(user_id)))
                    .fetch().forEach(calendarRecord -> recordMap.put(calendarRecord.getCalendarid(), calendarRecord));

            return calendarList.parallelStream()
                    .filter(calendar -> calendarService.isCalendarActivated(calendar.getId(), user_id))
                    .map(calendar -> {
                        CalendarRecord calendarRecord = recordMap.get(calendar.getId());
                        return new CalendarEntity(calendar.getName(), calendar.getId(), calendarRecord.getActivated(),
                                calendarRecord.getProvider().getName(), calendarRecord.getIsdefault());
                    }).collect(Collectors.toList());
        } catch (IOException e) {
            LOG.error("Error while getting calendars");
            throw new CalendarException("Unable to get all active calendars", e);
        }
    }

    private List<Calendar> getCalendarFromOffice(String user_id) throws IOException {
        TokenResponse token = getToken(user_id);
        OutlookService outlookService = OutlookServiceBuilder.getOutlookService(token.getAccessToken(), user_id);
        Integer maxResults = 20; //nobody should have more than 20 active calendars...
        return outlookService.getCalendars(maxResults).execute().body().getValue();
    }

    private Calendar getSingleCalendarFromOffice(String userId, String calendarId) throws IOException {
        TokenResponse tokenResponse = getToken(userId);
        OutlookService outlookService = OutlookServiceBuilder.getOutlookService(tokenResponse.getAccessToken(), userId);
        return outlookService.getCalendar(calendarId).execute().body();
    }

    @Override
    public List<CalendarEvent> getEventsBetween(Interval interval, String userId, String calendarId) throws CalendarException {
        try {
            TokenResponse token = getToken(userId);
            OutlookService outlookService = OutlookServiceBuilder.getOutlookService(token.getAccessToken(), userId);
            Integer maxResults = 20;
            String sort = "start/dateTime DESC";
            String properties = "organizer,subject,start,end";
            AppUserEntity appUserEntity = userService.getAppUser(userId);

            final List<Event> outlookEvents = outlookService
                    .getEvents(calendarId, sort, properties, maxResults, convertDateToString(interval.getStart()), convertDateToString(interval.getEnd()))
                    .execute().body().getValue();
            return outlookEvents.parallelStream()
                    .map(event -> {
                        LOG.info("Got outlook-event: {}", event);
                        return new CalendarEvent(event.getSubject(), "empty description", "empty room",
                                calendarId, new Interval(event.getStart().getDateDateTime().toInstant().toEpochMilli(),
                                event.getEnd().getDateDateTime().toInstant().toEpochMilli()
                        ), Collections.emptyList(), new CalendarEvent.Attendee(appUserEntity.getName(), appUserEntity.getMail()));
                    }).collect(Collectors.toList());
        } catch (IOException e) {
            throw new CalendarException("Unable to get token for user " + userId, e);
        }
    }

    @Override
    public List<Interval> getBusyTimes(String userId, NewEventEntity eventEntity) throws CalendarException {
        try {
            TokenResponse tokenResponse = getToken(userId);
            DateTime timeMin = DateTime.parse(eventEntity.getAutoTimeDateStart());
            DateTime timeMax = DateTime.parse(eventEntity.getAutoTimeDateEnd());
            //get all events between this dates, which are marked as busy...
            String filter = "showAs eq 'busy'";
            Integer maxResults = 200; //set hardly to 200...

            List<Interval> allBusyTimes = new LinkedList<>();

            OutlookService outlookService = OutlookServiceBuilder.getOutlookService(tokenResponse.getAccessToken(), userId);
            getAllCalendars(userId).parallelStream()
                    .map(calendarEntity -> {
                        try {
                            return outlookService.getEvents(calendarEntity.getCalendarID(), filter, maxResults,
                                    convertDateToString(timeMin), convertDateToString(timeMax)).execute().body().getValue();
                        } catch (IOException e) {
                            LOG.error("Unable to get events for {} in calendar: {}", userId, calendarEntity.getCalendarID());
                            return null;
                        }
                    }).filter(Objects::nonNull)
                    .flatMap(List::parallelStream)
                    .map(event -> new Interval(event.getStart().getDateDateTime().toInstant().toEpochMilli(),
                            event.getEnd().getDateDateTime().toInstant().toEpochMilli()))
                    .forEach(allBusyTimes::add);

            LOG.info("Got {} busy-times for {}", allBusyTimes.size(), userId);
            allBusyTimes.forEach(interval -> LOG.info("From {} until {}", interval.getStart(), interval.getEnd()));
            return allBusyTimes;

            /*
            @GET("/v1.0/me/calendars/{id}/calendarView")
    Call<PagedResult<Event>> getEvents(
            @Path("id") String calendarId,
            @Query("$filter") String filter,
            @Query("$top") Integer maxResults,
            @Query("startdatetime") String startDateTime,
            @Query("enddatetime") String endDateTime
             */
        } catch (IOException e) {
            throw new CalendarException("Unable to get token for user " + userId, e);
        } catch (NullPointerException e) {
            LOG.error("NullPointerException at officeProvider-Service", e);
            return Collections.emptyList();
        }


        /*
        //get active calendars for user
        try {
            FreeBusyResponse response = getCalendar(credential).freebusy().query(
                    new FreeBusyRequest()
                            .setTimeMin(timeMin)
                            .setTimeMax(timeMax)
                            .setItems(getFreeBusyRequestItems(userId))
            ).execute();
            LOG.debug("Executed request");
            List<TimePeriod> result = new LinkedList<>();
            LOG.debug("List created... Filling list");
            response.getCalendars().forEach((s, freeBusyCalendar) -> result.addAll(freeBusyCalendar.getBusy()));
            LOG.debug("List was filled. finished getBusyTimes");
            return result.parallelStream()
                    .map(timePeriod -> new Interval(timePeriod.getStart().getValue(), timePeriod.getEnd().getValue()))
                    .collect(Collectors.toList());
        } catch (IOException | CalendarException e) {
            LOG.error("Unable to get free/busy request items for {}", userId, e);
            throw new CalendarException("Unable to get busyTimes for " + userId, e);
        }
         */
    }

    @Override
    public CalendarEntity getCalendar(String calendarId, String userMail, boolean isActivated, boolean isDefault) {
        try {
            Calendar calendar = getSingleCalendarFromOffice(userMail, calendarId);
            LOG.debug("Got calendar: {} with name {}", calendar.getId(), calendar.getName());
            return new CalendarEntity(calendar, isActivated, isDefault);
        } catch (IOException e) {
            LOG.error("Unable to get calendar {} for {}", calendarId, userMail);
            return null;
        }
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
        final TokenResponse tokenResponse = objectMapper.readValue(json, TokenResponse.class);
        return validateToken(tokenResponse);
    }
}
