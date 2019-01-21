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
import de.rubeen.bsc.entities.web.CalendarEntity;
import de.rubeen.bsc.entities.web.LoginHoursEntity;
import de.rubeen.bsc.entities.web.NewEventEntity;
import de.rubeen.bsc.service.CalendarService;
import de.rubeen.bsc.service.LoginService;
import de.rubeen.bsc.service.RoomService;
import de.rubeen.bsc.service.UserService;
import org.apache.commons.lang3.NotImplementedException;
import org.joda.time.LocalTime;
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
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.LinkedList;
import java.util.List;
import java.util.Objects;
import java.util.stream.Collectors;

import static java.text.MessageFormat.format;

@Service
public class GoogleProviderService {
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

    @PostConstruct
    public void init() throws IOException, GeneralSecurityException {
        createFlow();
    }

    public String authorize() throws GeneralSecurityException, IOException {
        LOG.info("Try to authenticate user...");
        AuthorizationCodeRequestUrl authorizationCodeRequestUrl;
        authorizationCodeRequestUrl = flow
                .newAuthorizationUrl()
                .setRedirectUri(redirectURL);
        return authorizationCodeRequestUrl.build();
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

    public void createCredentialFromCallback(String code, String userID) throws IOException {
        TokenResponse tokenResponse = flow.newTokenRequest(code).setRedirectUri(redirectURL).execute();
        Credential credential = flow.createAndStoreCredential(tokenResponse, userID);

    }

    public List<CalendarEntity> getAllCalendars(String user_id) throws IOException, GeneralSecurityException {
        Credential credential = flow.loadCredential(user_id);
        try {
            validateCredential(credential);
            Calendar calendar = getCalendar(credential);
            CalendarList calendarList = calendar.calendarList().list().execute();
            calendarList.getItems().parallelStream().forEach(calendarListEntry -> calendarService.addCalendarToDatabase(calendarListEntry.getId(), user_id, Calprovider.google));
            return calendarList.getItems().parallelStream()
                    .map(calendarListEntry -> new CalendarEntity(calendarListEntry, calendarService.isCalendarActivated(calendarListEntry.getId())))
                    .collect(Collectors.toList());
        } catch (CredentialException e) {
            LOG.error("Credential exception: ", e);
            throw e;
        }
    }

    private Calendar getCalendar(Credential credential) {
        return new Calendar.Builder(httpTransport, jsonFactory, credential).setApplicationName(APP_NAME).build();
    }

    public List<CalendarEntity> getAllActiveCalendars(String user_id) throws IOException, GeneralSecurityException {
        Credential credential = flow.loadCredential(user_id);
        try {
            validateCredential(credential);
            Calendar calendar = getCalendar(credential);
            CalendarList calendarList = calendar.calendarList().list().execute();
            return calendarList.getItems().parallelStream()
                    .filter(calendarListEntry -> calendarService.isCalendarActivated(calendarListEntry.getId()))
                    .map(calendarListEntry -> new CalendarEntity(calendarListEntry, calendarService.isCalendarActivated(calendarListEntry.getId())))
                    .collect(Collectors.toList());
        } catch (CredentialException e) {
            LOG.error("Credential exception: ", e);
            throw e;
        }
    }

    public Events getEvents(final String user_id, final String calendarId, final org.joda.time.DateTime startDateTime, final org.joda.time.DateTime endDateTime) throws IOException, GeneralSecurityException {
        return this.getEvents(user_id, calendarId, new DateTime(startDateTime.toDate()), new DateTime(endDateTime.toDate()));
    }

    public Events getEvents(final String user_id, final String calendarId, final DateTime startDateTime, final DateTime endDateTime) throws IOException, GeneralSecurityException {
        Credential credential = flow.loadCredential(user_id);
        try {
            validateCredential(credential);
            Calendar calendar = getCalendar(credential);
            return calendar.events().list(calendarId).setTimeMin(startDateTime).setTimeMax(endDateTime).execute();
        } catch (CredentialException e) {
            LOG.error("Credential exception: ", e);
            throw e;
        }
    }

    public void createAutoEvent(String userId, String calendarId, NewEventEntity eventEntity) throws IOException, GeneralSecurityException {
        Credential credential = flow.loadCredential(userId.replace("@", "%40"));
        try {
            validateCredential(credential);
            Calendar calendar = getCalendar(credential);
            //if manual room, search for time with specific room...
            if (eventEntity.isAutoRoom()) {
                throw new NotImplementedException("Auto-Room was not implemented, yet");
            } else {
                int roomId = eventEntity.getRoomId();
                //roomService can only return room-name
                //TODO implement roomService functionality to get calendar (with provider) for rooms
                LOG.info("using room: {}", roomService.getRoomById(roomId));

                LOG.info("Meeting will be between {} and {} with a duration of {} {}",
                        eventEntity.getAutoTimeDateStart(), eventEntity.getAutoTimeDateEnd(),
                        eventEntity.getMeetingDuration(), eventEntity.getDurationUnit());
            }
            List<TimePeriod> busyTimes = getBusyTimes(userId, eventEntity, calendar);
            List<LoginHoursEntity> workingHours = userService.getWorkingHours(userId);
            LOG.debug("Got {} busyTimes: {}", busyTimes.size(), busyTimes);
            LOG.info("Meeting cannot be between:");
            busyTimes.forEach(timePeriod -> {
                org.joda.time.DateTime start = new org.joda.time.DateTime(timePeriod.getStart().getValue());
                org.joda.time.DateTime end = new org.joda.time.DateTime(timePeriod.getEnd().getValue());
                LOG.info("{} - {}", start.toString("d.M.y (H:m)"), end.toString("d.M.y (H:m)"));
            });

            LOG.debug("Got {} workingHours: {}", workingHours.size(), workingHours);
            workingHours.forEach(loginHoursEntity -> {
                LocalTime start = new LocalTime(loginHoursEntity.getStartTime());
                LocalTime end = new LocalTime(loginHoursEntity.getEndTime());
                String dayString = (loginHoursEntity.isMonday() ? "Mon " : "") +
                        (loginHoursEntity.isTuesday() ? "Tue " : "") +
                        (loginHoursEntity.isWednesday() ? "Wed " : "") +
                        (loginHoursEntity.isThursday() ? "Thr " : "") +
                        (loginHoursEntity.isFriday() ? "Fri " : "") +
                        (loginHoursEntity.isSaturday() ? "Sat " : "") +
                        (loginHoursEntity.isSunday() ? "Sun " : "");
                LOG.info("{}{} - {}", dayString, start.toString("H:m"), end.toString("H:m"));
            });
        } catch (GeneralSecurityException e) {
            LOG.error("Security-Exception: ", e);
            throw e;
        }
    }

    private List<TimePeriod> getBusyTimes(String userId, NewEventEntity eventEntity, Calendar calendar) throws IOException, GeneralSecurityException {
        try {
            DateTime timeMin = new DateTime(org.joda.time.DateTime.parse(eventEntity.getAutoTimeDateStart()).toDate());
            DateTime timeMax = new DateTime(org.joda.time.DateTime.parse(eventEntity.getAutoTimeDateEnd()).toDate());
            LOG.debug("Creating response");
            FreeBusyResponse response = calendar.freebusy().query(
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
            return result;
        } catch (IOException | GeneralSecurityException e) {
            LOG.error("Unable to get free/busy request items for {}", userId, e);
            throw e;
        }
    }

    private List<FreeBusyRequestItem> getFreeBusyRequestItems(String userId) throws IOException, GeneralSecurityException {
        try {
            LOG.debug("Getting freeBusyRequest items...");
            LOG.debug("Getting all active calendars for {}", userId);
            List<CalendarEntity> allActiveCalendars = getAllActiveCalendars(userId);
            LOG.debug("Got all calendars, size: " + allActiveCalendars.size());
            if (allActiveCalendars.size() == 0)
                throw new IOException("Active calendars is empty");
            LOG.debug("Creating freeBusyRequestItems now...t");
            return allActiveCalendars.parallelStream()
                    .map(calendarEntity -> new FreeBusyRequestItem().setId(calendarEntity.getCalendarID()))
                    .collect(Collectors.toList());
        } catch (IOException | GeneralSecurityException e) {
            LOG.error("Unable to get calendars for user {}", userId);
            throw e;
        }
    }

    public void createEvent(String user_id, String calendarId, NewEventEntity newEventEntity) throws IOException, CredentialException {
        Credential credential = flow.loadCredential(user_id.replace("@", "%40"));
        try {
            validateCredential(credential);
            Calendar calendar = getCalendar(credential);
            String room = "";
            if (newEventEntity.isAutoRoom()) {
                throw new NotImplementedException("AutoRoom was not implemented, yet");
            } else {
                room = roomService.getRoomById(newEventEntity.getRoomId());
            }
            List<EventAttendee> attendeeList = newEventEntity.getAttendees().parallelStream()
                    .map(userService::getAppUser)
                    .filter(Objects::nonNull)
                    .map(appUserEntity -> new EventAttendee()
                            .setDisplayName(appUserEntity.getName())
                            .setEmail(appUserEntity.getMail()))
                    .collect(Collectors.toList());
            LOG.info("Using calendar: " + calendarId);
            DateTime dateTimeStart = new DateTime(new SimpleDateFormat("yyyy-MM-dd HH:mm").parse(format("{0} {1}", newEventEntity.getManTimeDateStart(), newEventEntity.getManTimeTimeStart())));
            DateTime dateTimeEnd = new DateTime(new SimpleDateFormat("yyyy-MM-dd HH:mm").parse(format("{0} {1}", newEventEntity.getManTimeDateEnd(), newEventEntity.getManTimeTimeEnd())));
            LOG.info("" + dateTimeStart);
            LOG.info("" + dateTimeEnd);
            Event event = new Event()
                    .setSummary(newEventEntity.getSubject())
                    .setDescription(newEventEntity.getDescription())
                    .setStart(new EventDateTime().setDateTime(dateTimeStart))
                    .setEnd(new EventDateTime().setDateTime(dateTimeEnd))
                    .setAttendees(attendeeList)
                    .setLocation(room);
            //.setEtag("test");
            calendar.events().insert(calendarId, event).setSendUpdates("all").execute();
        } catch (CredentialException e) {
            LOG.error("Credential exception: ", e);
            throw e;
        } catch (ParseException e) {
            LOG.error("parsing-error", e);
        }
    }
}
