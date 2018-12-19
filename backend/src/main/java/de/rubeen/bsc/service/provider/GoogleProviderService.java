package de.rubeen.bsc.service.provider;

import com.google.api.client.auth.oauth2.AuthorizationCodeRequestUrl;
import com.google.api.client.auth.oauth2.Credential;
import com.google.api.client.auth.oauth2.TokenResponse;
import com.google.api.client.googleapis.auth.oauth2.GoogleAuthorizationCodeFlow;
import com.google.api.client.googleapis.auth.oauth2.GoogleClientSecrets;
import com.google.api.client.googleapis.javanet.GoogleNetHttpTransport;
import com.google.api.client.http.javanet.NetHttpTransport;
import com.google.api.client.json.Json;
import com.google.api.client.json.JsonFactory;
import com.google.api.client.json.jackson2.JacksonFactory;
import com.google.api.client.util.DateTime;
import com.google.api.client.util.store.FileDataStoreFactory;
import com.google.api.services.calendar.Calendar;
import com.google.api.services.calendar.CalendarScopes;
import com.google.api.services.calendar.model.CalendarList;
import com.google.api.services.calendar.model.Events;
import de.rubeen.bsc.entities.db.enums.Calprovider;
import de.rubeen.bsc.entities.web.CalendarEntity;
import de.rubeen.bsc.service.CalendarService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import javax.security.auth.login.CredentialException;
import java.io.File;
import java.io.IOException;
import java.security.GeneralSecurityException;
import java.util.List;
import java.util.stream.Collectors;

import static org.joda.time.DateTimeConstants.MONDAY;
import static org.joda.time.DateTimeConstants.SUNDAY;

@Service
public class GoogleProviderService {
    private static final String APP_NAME = "My-Business-Day",
            CREDENTIAL_DATA_STORE_PATH = "google-auth-clients";
    private static final Logger LOG = LoggerFactory.getLogger(GoogleProviderService.class);
    private final CalendarService calendarService;
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
    public GoogleProviderService(CalendarService calendarService) {
        this.calendarService = calendarService;
    }

    public String authorize() throws GeneralSecurityException, IOException {
        LOG.info("Try to authenticate user...");
        AuthorizationCodeRequestUrl authorizationCodeRequestUrl;
        createFlow();
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
        createFlow();
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

    public CalendarList getAllActiveCalendars(String user_id) throws IOException, GeneralSecurityException {
        createFlow();
        Credential credential = flow.loadCredential(user_id);
        try {
            validateCredential(credential);
            Calendar calendar = getCalendar(credential);
            CalendarList calendarList = calendar.calendarList().list().execute();
            calendarList.getItems().removeIf(calendarListEntry -> !calendarService.isCalendarActivated(calendarListEntry.getId()));
            return calendarList;
        } catch (CredentialException e) {
            LOG.error("Credential exception: ", e);
            throw e;
        }
    }

    public Events getEvents(final String user_id, final String calendarId, final org.joda.time.DateTime startDateTime, final org.joda.time.DateTime endDateTime) throws IOException, GeneralSecurityException {
        return this.getEvents(user_id, calendarId, new DateTime(startDateTime.toDate()), new DateTime(endDateTime.toDate()));
    }

    public Events getEvents(final String user_id, final String calendarId, final DateTime startDateTime, final DateTime endDateTime) throws IOException, GeneralSecurityException {
        createFlow();
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
}
