package de.rubeen.bsc.service.provider;

import com.google.api.client.auth.oauth2.AuthorizationCodeRequestUrl;
import com.google.api.client.auth.oauth2.Credential;
import com.google.api.client.auth.oauth2.TokenResponse;
import com.google.api.client.googleapis.auth.oauth2.GoogleAuthorizationCodeFlow;
import com.google.api.client.googleapis.auth.oauth2.GoogleClientSecrets;
import com.google.api.client.googleapis.javanet.GoogleNetHttpTransport;
import com.google.api.client.http.javanet.NetHttpTransport;
import com.google.api.client.json.JsonFactory;
import com.google.api.client.json.jackson2.JacksonFactory;
import com.google.api.client.util.DateTime;
import com.google.api.client.util.store.FileDataStoreFactory;
import com.google.api.services.calendar.Calendar;
import com.google.api.services.calendar.CalendarScopes;
import com.google.api.services.calendar.model.*;
import com.google.api.services.oauth2.Oauth2;
import com.google.api.services.oauth2.model.Userinfoplus;
import de.rubeen.bsc.entities.db.enums.Calprovider;
import de.rubeen.bsc.entities.db.tables.records.CalendarRecord;
import de.rubeen.bsc.entities.provider.CalendarEvent;
import de.rubeen.bsc.entities.web.AppUserEntity;
import de.rubeen.bsc.entities.web.CalendarEntity;
import de.rubeen.bsc.entities.web.NewEventEntity;
import de.rubeen.bsc.service.CalendarService;
import de.rubeen.bsc.service.DatabaseService;
import de.rubeen.bsc.service.LoginService;
import de.rubeen.bsc.service.UserService;
import org.joda.time.Interval;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import javax.annotation.PostConstruct;
import javax.security.auth.login.CredentialException;
import java.io.File;
import java.io.IOException;
import java.security.GeneralSecurityException;
import java.util.*;
import java.util.concurrent.atomic.AtomicReference;
import java.util.stream.Collectors;

import static de.rubeen.bsc.entities.db.tables.Calendar.CALENDAR;
import static de.rubeen.bsc.service.EventService.getBeginOfDay;
import static de.rubeen.bsc.service.EventService.getEndOfDay;
import static java.text.MessageFormat.format;

@Service
public class GoogleProviderService implements CalendarProvider {
    private static final String APP_NAME = "My-Business-Day",
            CREDENTIAL_DATA_STORE_PATH = "google-auth-clients";
    private static final Logger LOG = LoggerFactory.getLogger(GoogleProviderService.class);
    private final CalendarService calendarService;
    private final LoginService loginService;
    private final UserService userService;
    private final DatabaseService databaseService;
    @Value("${google.client.redirectUri}")
    private String redirectURL;
    @Value("${google.client.client-id}")
    private String clientId;
    @Value("${google.client.client-secret}")
    private String clientSecret;
    private GoogleAuthorizationCodeFlow flow;
    private NetHttpTransport httpTransport;
    private JsonFactory jsonFactory = JacksonFactory.getDefaultInstance();

    @Autowired
    public GoogleProviderService(CalendarService calendarService, LoginService loginService, UserService userService, DatabaseService databaseService) {
        this.calendarService = calendarService;
        this.loginService = loginService;
        this.userService = userService;
        this.databaseService = databaseService;
    }

    private static EventDateTime getEventDateTime(org.joda.time.DateTime startDateTime) {
        return new EventDateTime().setDateTime(new DateTime(startDateTime.getMillis()));
    }

    @PostConstruct
    public void init() throws IOException, GeneralSecurityException {
        createFlow();
    }

    private void createFlow() throws IOException, GeneralSecurityException {
        if (flow == null) {
            System.out.println("Creating new flow");
            GoogleClientSecrets.Details web = new GoogleClientSecrets.Details()
                    .setClientId(clientId)
                    .setClientSecret(clientSecret);
            GoogleClientSecrets clientSecrets = new GoogleClientSecrets().setWeb(web);
            httpTransport = GoogleNetHttpTransport.newTrustedTransport();
            flow = new GoogleAuthorizationCodeFlow.Builder(httpTransport, jsonFactory, clientSecrets,
                    List.of(
                            CalendarScopes.CALENDAR,
                            "https://www.googleapis.com/auth/userinfo.profile",
                            "https://www.googleapis.com/auth/userinfo.email"))
                    .setDataStoreFactory(new FileDataStoreFactory(new File(CREDENTIAL_DATA_STORE_PATH)))
                    .setApprovalPrompt("force")
                    .setAccessType("offline")
                    .build();
        }
    }

    private void validateCredential(Credential credential) throws IOException, CredentialException {
        if (credential == null)
            throw new CredentialException("Credential is null");
        credential.refreshToken();
    }

    private Calendar getCalendar(Credential credential) {
        return new Calendar.Builder(httpTransport, jsonFactory, credential).setApplicationName(APP_NAME).build();
    }

    private Oauth2 getOAuth2(Credential credential) {
        return new Oauth2.Builder(httpTransport, jsonFactory, credential).setApplicationName(APP_NAME).build();
    }

    private List<FreeBusyRequestItem> getFreeBusyRequestItems(String userId) throws IOException, CalendarException {
        try {
            LOG.debug("Getting freeBusyRequest items...");
            LOG.debug("Getting all active calendars for {}", userId);
            List<CalendarEntity> allActiveCalendars = getAllActiveCalendars(userId);
            LOG.debug("Got all calendars, size: " + allActiveCalendars.size());
            if (allActiveCalendars.size() == 0)
                throw new IOException("Active calendars is empty");
            LOG.debug("Creating freeBusyRequestItems now...");
            return allActiveCalendars.parallelStream()
                    .map(calendarEntity -> new FreeBusyRequestItem().setId(calendarEntity.getCalendarID()))
                    .collect(Collectors.toList());
        } catch (IOException | CalendarException e) {
            LOG.error("Unable to get calendars for user {}", userId);
            throw e;
        }
    }

    public String createAuthRequestUrl() {
        LOG.info("Try to authenticate user...");
        AuthorizationCodeRequestUrl authorizationCodeRequestUrl;
        authorizationCodeRequestUrl = flow
                .newAuthorizationUrl()
                .setRedirectUri(redirectURL);
        return authorizationCodeRequestUrl.build();
    }

    public void createCredentialFromCallback(String code, String userID) throws IOException {
        TokenResponse tokenResponse = flow.newTokenRequest(code).setRedirectUri(redirectURL).execute();
        flow.createAndStoreCredential(tokenResponse, userID);
    }

    @Override
    public List<CalendarEntity> getAllCalendars(String user_id) throws CalendarException {
        try {
            Credential credential = flow.loadCredential(getCredentialUserId(user_id));
            validateCredential(credential);
            Calendar calendar = getCalendar(credential);
            CalendarList calendarList = calendar.calendarList().list().execute();

            calendarList.getItems().parallelStream()
                    .forEachOrdered(calendarListEntry ->
                            calendarService.addCalendarToDatabase(calendarListEntry.getId(), user_id, Calprovider.google));

            final Map<String, CalendarRecord> recordMap = new HashMap<>();
            databaseService.getContext().selectFrom(CALENDAR)
                    .where(CALENDAR.PROVIDER.eq(Calprovider.google))
                    .and(CALENDAR.USER_ID.eq(loginService.getUserID(user_id)))
                    .fetch().forEach(calendarRecord -> recordMap.put(calendarRecord.getCalendarid(), calendarRecord));

            return calendarList.getItems().parallelStream()
                    .map(calendarListEntry -> {
                        CalendarRecord calendarRecord = recordMap.get(calendarListEntry.getId());
                        return new CalendarEntity(calendarListEntry, calendarRecord.getActivated(), calendarRecord.getIsdefault());
                    }).collect(Collectors.toList());
        } catch (CredentialException | IOException e) {
            LOG.error("Credential exception: ", e);
            throw new CalendarException("failure...", e);
        }
    }

    @Override
    public List<CalendarEntity> getAllActiveCalendars(String user_id) throws CalendarException {
        try {
            Credential credential = flow.loadCredential(getCredentialUserId(user_id));
            validateCredential(credential);
            Calendar calendar = getCalendar(credential);
            CalendarList calendarList = calendar.calendarList().list().execute();

            final Map<String, CalendarRecord> recordMap = new HashMap<>();
            databaseService.getContext().selectFrom(CALENDAR)
                    .where(CALENDAR.PROVIDER.eq(Calprovider.google))
                    .and(CALENDAR.ACTIVATED.isTrue())
                    .and(CALENDAR.USER_ID.eq(loginService.getUserID(user_id)))
                    .fetch().forEach(calendarRecord -> recordMap.put(calendarRecord.getCalendarid(), calendarRecord));


            return calendarList.getItems().parallelStream()
                    .filter(calendarListEntry -> calendarService.isCalendarActivated(calendarListEntry.getId(), user_id))
                    .map(calendarListEntry -> new CalendarEntity(calendarListEntry, true, recordMap.get(calendarListEntry.getId()).getIsdefault()))
                    .collect(Collectors.toList());
        } catch (CredentialException | IOException e) {
            LOG.error("Credential exception: ", e);
            throw new CalendarException("Unable to get all active calendars", e);
        }
    }

    @Override
    public List<CalendarEvent> getEventsBetween(Interval interval, String userId, String calendarId) throws CalendarException {
        Credential credential;
        try {
            credential = flow.loadCredential(getCredentialUserId(userId));
            validateCredential(credential);
        } catch (IOException | CredentialException e) {
            LOG.error("Error while getting credential for user {}", userId, e);
            throw new CalendarException("Unable to get credential for user " + userId, e);
        }
        Calendar calendar = getCalendar(credential);
        DateTime startDateTime = new DateTime(interval.getStartMillis()),
                endDateTime = new DateTime(interval.getEndMillis());
        AppUserEntity appUserEntity = userService.getAppUser(userId);
        try {
            return calendar.events().list(calendarId).setTimeMin(startDateTime).setTimeMax(endDateTime).execute()
                    .getItems().parallelStream()
                    .filter(Objects::nonNull)
                    .map(event -> {
                        List<CalendarEvent.Attendee> attendees;
                        if (event.getAttendees() == null || event.getAttendees().size() == 0)
                            attendees = Collections.emptyList();
                        else
                            attendees = event.getAttendees().parallelStream()
                                    .map(eventAttendee ->
                                            new CalendarEvent.Attendee(
                                                    eventAttendee.getDisplayName(),
                                                    eventAttendee.getEmail()))
                                    .collect(Collectors.toList());

                        Interval eventInterval =
                                event.getStart().getDateTime() != null ?
                                        new Interval(
                                                event.getStart().getDateTime().getValue(),
                                                event.getEnd().getDateTime().getValue()) :
                                        //all-day-event:
                                        new Interval(
                                                event.getStart().getDate().getValue(),
                                                event.getEnd().getDate().getValue());

                        return new CalendarEvent(event.getSummary(), event.getDescription(), event.getLocation(),
                                calendarId, eventInterval, attendees, new CalendarEvent.Attendee(appUserEntity.getName(), appUserEntity.getMail()));
                    })
                    .collect(Collectors.toList());
        } catch (IOException e) {
            LOG.error("Error while getting events for user {}", userId, e);
            throw new CalendarException("Unable to get events for user " + userId, e);
        }
    }

    @Override
    public boolean createEvent(CalendarEvent calendarEvent, String userId) throws CalendarException {
        Credential credential;
        AppUserEntity appUser = userService.getAppUser(userId);
        try {
            credential = flow.loadCredential(getCredentialUserId(userId));
        } catch (IOException e) {
            LOG.error("Error while getting credential for user {}", userId, e);
            throw new CalendarException("Unable to get credential for user " + userId, e);
        }
        Calendar calendar = getCalendar(credential);
        try {
            List<EventAttendee> attendees = getEventAttendees(calendarEvent.getAttendees());
            //remove actual user and add eventCreator as attendee
            AtomicReference<EventAttendee> creator = new AtomicReference<>();
            attendees.removeIf(eventAttendee -> {
                boolean isEventAttendee = eventAttendee.getEmail().equals(appUser.getMail());
                if (isEventAttendee) {
                    creator.set(new EventAttendee()
                            .setDisplayName(calendarEvent.getCreator().getName())
                            .setEmail(calendarEvent.getCreator().getMail()));
                }
                return isEventAttendee;
            });
            EventAttendee attendee;
            if ((attendee = creator.get()) != null)
                attendees.add(attendee);
            calendar.events().insert(calendarEvent.getCalendarId(),
                    new Event()
                            .setSource(new Event.Source().setTitle("My-Business-Day").setUrl(format("https://localhost:3333/callback?subject={0}", calendarEvent.getSubject())))
                            .setSummary(calendarEvent.getSubject())
                            .setDescription(calendarEvent.getDescription())
                            .setLocation(calendarEvent.getRoom())
                            .setStart(getEventDateTime(calendarEvent.getStartDateTime()))
                            .setEnd(getEventDateTime(calendarEvent.getEndDateTime()))
                            .setAttendees(attendees)
            ).setSendNotifications(false).execute();
            return true;
        } catch (IOException e) {
            LOG.error("Error while adding event {} for user {}", calendarEvent, userId);
            throw new CalendarException("Unable to add event for user {}" + userId, e);
        }
    }

    private List<EventAttendee> getEventAttendees(List<CalendarEvent.Attendee> attendees) {
        // FIXME: 2019-02-22 events will be created twice
        return attendees.parallelStream()
                .map(attendee -> new EventAttendee()
                        .setDisplayName(
                                getUserInfoPlus(attendee.getMail())
                                        .orElse(new Userinfoplus().setName(attendee.getName())).getName())
                        .setEmail(getUserInfoPlus(attendee.getMail())
                                .orElse(new Userinfoplus().setEmail(attendee.getMail())).getEmail()))
                .collect(Collectors.toList());
    }

    @Override
    public List<Interval> getBusyTimes(String userId, NewEventEntity eventEntity) throws CalendarException {
        Credential credential;
        try {
            credential = flow.loadCredential(getCredentialUserId(userId));
        } catch (IOException e) {
            throw new CalendarException("Unable to get credential for user " + userId, e);
        }
        //get active calendars for user
        try {
            DateTime timeMin = new DateTime(
                    getBeginOfDay(org.joda.time.DateTime.parse(eventEntity.getAutoTimeDateStart()))
                            .toDate()
            );
            DateTime timeMax = new DateTime(
                    getEndOfDay(org.joda.time.DateTime.parse(eventEntity.getAutoTimeDateEnd()))
                            .toDate()
            );
            LOG.debug("Creating response");
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
    }

    @Override
    public CalendarEntity getCalendar(String calendarId, String userId, boolean isActivated, boolean isDefault) {
        try {
            Credential credential = flow.loadCredential(getCredentialUserId(userId));
            validateCredential(credential);
            Calendar calendar = getCalendar(credential);
            CalendarListEntry calendarListEntry = calendar.calendarList().get(calendarId).execute();
            return new CalendarEntity(calendarListEntry, isActivated, isDefault);
        } catch (IOException | CredentialException e) {
            LOG.error("Unable to handle credential for {}", userId);
            return null;
        }

    }

    private String getCredentialUserId(String mail) {
        return mail.replace("@", "%40");
    }

    private Optional<Userinfoplus> getUserInfoPlus(String userId) {
        try {
            Credential credential = flow.loadCredential(getCredentialUserId(userId));
            return Optional.of(getOAuth2(credential).userinfo().get().execute());
        } catch (IOException e) {
            LOG.error("Unable to get OAuth2 for user {}", userId);
            return Optional.empty();
        }
    }

}
