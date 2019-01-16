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
import de.rubeen.bsc.entities.web.NewEventEntity;
import de.rubeen.bsc.service.CalendarService;
import de.rubeen.bsc.service.RoomService;
import de.rubeen.bsc.service.UserService;
import org.apache.commons.lang3.NotImplementedException;
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
import java.text.DateFormat;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.List;
import java.util.Objects;
import java.util.TimeZone;
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
    public GoogleProviderService(CalendarService calendarService, RoomService roomService, UserService userService) {
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

    public FreeBusyResponse getFreeBusyTimes(String user_id) throws IOException, CredentialException {
        Credential credential = flow.loadCredential(user_id);
        try {
            validateCredential(credential);
            //FreeBusyRequest request =
            String dIn = "2018-12-20 08:00:00";
            String dIne = "2018-12-20 20:00:00";
            DateFormat df = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");

            Date d = df.parse(dIn);
            DateTime startTime = new DateTime(d, TimeZone.getDefault());

            Date de = df.parse(dIne);
            DateTime endTime = new DateTime(de, TimeZone.getDefault());

            FreeBusyRequest req = new FreeBusyRequest();
            req.setTimeMin(startTime);
            req.setTimeMax(endTime);
            req.setItems(List.of(new FreeBusyRequestItem().setId("rubeenv3@gmail.com")));
            Calendar.Freebusy.Query fbq = getCalendar(credential).freebusy().query(req);

            FreeBusyResponse fbresponse = fbq.execute();
            System.out.println("-------------------------------");
            System.out.println(fbresponse.toString());
            System.out.println(fbresponse.getCalendars());
            fbresponse.getCalendars().forEach((s, freeBusyCalendar) -> System.out.println(freeBusyCalendar));
            fbresponse.getCalendars().forEach((s, freeBusyCalendar) -> freeBusyCalendar.getBusy().forEach(System.out::println));
            System.out.println("-------------------------------");
            return fbresponse;
        } catch (CredentialException e) {
            LOG.error("Credential exception: ", e);
            throw e;
        } catch (ParseException e) {
            LOG.error("Error while parsing: ", e);
        }
        return null;
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
