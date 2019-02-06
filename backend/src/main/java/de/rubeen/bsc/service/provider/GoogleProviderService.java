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
import de.rubeen.bsc.entities.db.enums.Calprovider;
import de.rubeen.bsc.entities.provider.CalendarEvent;
import de.rubeen.bsc.entities.web.CalendarEntity;
import de.rubeen.bsc.entities.web.NewEventEntity;
import de.rubeen.bsc.service.CalendarService;
import de.rubeen.bsc.service.LoginService;
import de.rubeen.bsc.service.RoomService;
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
import java.util.Collections;
import java.util.LinkedList;
import java.util.List;
import java.util.Objects;
import java.util.stream.Collectors;

@Service
public class GoogleProviderService implements CalendarProvider {
    private static final String APP_NAME = "My-Business-Day",
            CREDENTIAL_DATA_STORE_PATH = "google-auth-clients";
    private static final Logger LOG = LoggerFactory.getLogger(GoogleProviderService.class);
    private final CalendarService calendarService;
    private final RoomService roomService;
    private final UserService userService;
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
    public GoogleProviderService(CalendarService calendarService, RoomService roomService, UserService userService, LoginService loginService) {
        this.calendarService = calendarService;
        this.roomService = roomService;
        this.userService = userService;
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
            flow = new GoogleAuthorizationCodeFlow.Builder(httpTransport, jsonFactory, clientSecrets, List.of(CalendarScopes.CALENDAR))
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
            Credential credential = flow.loadCredential(user_id);
            validateCredential(credential);
            Calendar calendar = getCalendar(credential);
            CalendarList calendarList = calendar.calendarList().list().execute();
            calendarList.getItems().parallelStream().forEach(calendarListEntry -> calendarService.addCalendarToDatabase(calendarListEntry.getId(), user_id, Calprovider.google));
            return calendarList.getItems().parallelStream()
                    .map(calendarListEntry -> new CalendarEntity(calendarListEntry, calendarService.isCalendarActivated(calendarListEntry.getId(), user_id)))
                    .collect(Collectors.toList());
        } catch (CredentialException | IOException e) {
            LOG.error("Credential exception: ", e);
            throw new CalendarException("failure...", e);
        }
    }

    @Override
    public List<CalendarEntity> getAllActiveCalendars(String user_id) throws CalendarException {
        try {
            Credential credential = flow.loadCredential(user_id);
            validateCredential(credential);
            Calendar calendar = getCalendar(credential);
            CalendarList calendarList = calendar.calendarList().list().execute();
            return calendarList.getItems().parallelStream()
                    .filter(calendarListEntry -> calendarService.isCalendarActivated(calendarListEntry.getId(), user_id))
                    .map(calendarListEntry -> new CalendarEntity(calendarListEntry, true))
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
                                calendarId, eventInterval, attendees);
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
        try {
            credential = flow.loadCredential(userId);
        } catch (IOException e) {
            LOG.error("Error while getting credential for user {}", userId, e);
            throw new CalendarException("Unable to get credential for user " + userId, e);
        }
        Calendar calendar = getCalendar(credential);
        try {
            calendar.events().insert(calendarEvent.getCalendarId(),
                    new Event()
                            .setSummary(calendarEvent.getSubject())
                            .setDescription(calendarEvent.getDescription())
                            .setLocation(calendarEvent.getRoom())
                            .setStart(getEventDateTime(calendarEvent.getStartDateTime()))
                            .setEnd(getEventDateTime(calendarEvent.getEndDateTime()))
                            .setAttendees(getEventAttendees(calendarEvent.getAttendees()))
            ).execute();
            return true;
        } catch (IOException e) {
            LOG.error("Error while adding event {} for user {}", calendarEvent, userId);
            throw new CalendarException("Unable to add event for user {}" + userId, e);
        }
    }

    private List<EventAttendee> getEventAttendees(List<CalendarEvent.Attendee> attendees) {
        return attendees.parallelStream()
                .map(attendee -> new EventAttendee().setDisplayName(attendee.getName()).setEmail(attendee.getMail()))
                .collect(Collectors.toList());
    }

    @Override
    public List<Interval> getBusyTimes(String userId, NewEventEntity eventEntity) throws CalendarException {
        Credential credential;
        try {
            credential = flow.loadCredential(userId.replace("@", "%40"));
        } catch (IOException e) {
            throw new CalendarException("Unable to get credential for user " + userId, e);
        }
        //get active calendars for user
        try {
            DateTime timeMin = new DateTime(org.joda.time.DateTime.parse(eventEntity.getAutoTimeDateStart()).toDate());
            DateTime timeMax = new DateTime(org.joda.time.DateTime.parse(eventEntity.getAutoTimeDateEnd()).toDate());
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
    public CalendarEntity getCalendar(String calendarId, String userId, boolean isActivated) {
        try {
            Credential credential = flow.loadCredential(userId);
            validateCredential(credential);
            Calendar calendar = getCalendar(credential);
            CalendarListEntry calendarListEntry = calendar.calendarList().get(calendarId).execute();
            return new CalendarEntity(calendarListEntry, isActivated);
        } catch (IOException | CredentialException e) {
            LOG.error("Unable to handle credential for {}", userId);
            return null;
        }

    }

    private String getCredentialUserId(String mail) {
        return mail.replace("@", "%40");
    }

}
