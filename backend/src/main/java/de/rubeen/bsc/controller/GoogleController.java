package de.rubeen.bsc.controller;

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
import com.google.api.services.calendar.model.CalendarList;
import com.google.api.services.calendar.model.Event;
import com.google.api.services.calendar.model.Events;
import de.rubeen.bsc.service.CalendarService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.servlet.view.RedirectView;

import javax.security.auth.login.CredentialException;
import javax.servlet.http.Cookie;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.servlet.http.HttpSession;
import java.io.File;
import java.io.IOException;
import java.security.GeneralSecurityException;
import java.util.List;
import java.util.Set;

import static java.text.MessageFormat.format;
import static org.joda.time.DateTimeConstants.MONDAY;
import static org.joda.time.DateTimeConstants.SUNDAY;

@RestController
public class GoogleController {
    private static String APP_NAME = "My-Business-Day";
    private final CalendarService calendarService;
    private GoogleClientSecrets clientSecrets;
    private GoogleAuthorizationCodeFlow flow;
    private JsonFactory jsonFactory = JacksonFactory.getDefaultInstance();
    private Logger LOG = LoggerFactory.getLogger(this.getClass());
    @Value("${google.client.client-id}")
    private String clientId;
    @Value("${google.client.client-secret}")
    private String clientSecret;
    @Value("${google.client.redirectUri}")
    private String redirectURL;
    @Value("${webapp.url}")
    private String webAppUrl;
    private Set<Event> eventSet;
    private NetHttpTransport httpTransport;

    @Autowired
    public GoogleController(CalendarService calendarService) {
        this.calendarService = calendarService;
    }

    public Set<Event> getEventSet() {
        return eventSet;
    }

    public void setEventSet(Set<Event> eventSet) {
        this.eventSet = eventSet;
    }

    @RequestMapping(value = "/auth-google", method = RequestMethod.GET)
    public RedirectView googleConnectionStatus(@RequestParam(value = "user_id") String user_id) throws GeneralSecurityException, IOException {
        return new RedirectView(authorize(user_id));
    }

    @RequestMapping(value = "/auth-google", method = RequestMethod.GET, params = "code")
    public String oauth2Callback(@RequestParam(value = "code") String code, HttpServletRequest request, HttpServletResponse response) throws IOException {
        Events events;
        String message;
        LOG.info("GOT Cookies: ");
        Cookie cookie = null;
        for (Cookie thisCookie : request.getCookies()) {
            LOG.info(thisCookie.getName());
            if (thisCookie.getName().equals("USER-ID"))
                cookie = thisCookie;
        }
        if (cookie == null) {
            response.sendError(HttpServletResponse.SC_NOT_ACCEPTABLE);
            return null;
        }
        try {
            LOG.info("OLD: " + code);
            TokenResponse tokenResponse = flow.newTokenRequest(code).setRedirectUri(redirectURL).execute();
            System.out.println("TOKEN-RESPONSE: " + tokenResponse);
            System.out.println(tokenResponse.getUnknownKeys().get("user-id"));
            Credential credential = flow.createAndStoreCredential(tokenResponse, cookie.getValue());
            System.out.println(flow.loadCredential(cookie.getValue()));
            message = credential.getAccessToken();
            //Cookie cookie = new Cookie("google-access-key", tokenResponse.getAccessToken());
            //response.addCookie(cookie);
            response.addCookie(new Cookie("google-access-key", tokenResponse.getAccessToken()));
            //System.out.println(cookie.getValue());
            response.sendRedirect(format("{0}/settings", webAppUrl));

            /*events = calendar.events().list("primary").setTimeMin(minDate).setTimeMax(maxDate).execute();
            message = events.getItems().toString();
            System.out.println("My: " + events.getItems());*/
        } catch (IOException e) {
            message = "Exception while handling OAuth2 callback (" + e.getMessage() + ")."
                    + " Redirecting to google connection status page.";
        }
        System.out.println("message: " + message);
        response.setStatus(200);
        return message;
        //return new ResponseEntity<>(message, HttpStatus.OK);
    }

    @RequestMapping("/logout-google")
    public ResponseEntity<String> logout(HttpServletRequest request, HttpServletResponse response) {
        HttpSession session = request.getSession();
        session.invalidate();
        for (Cookie cookie : request.getCookies()) {
            if (cookie.getName().equals("google-access-key")) {
                cookie.setMaxAge(0);
                response.addCookie(cookie);
            }
        }
        return new ResponseEntity<>(HttpStatus.OK);
    }

    @RequestMapping("/google/calendar")
    public CalendarList getAllCalendar(@RequestParam("user_id") String user_id, HttpServletResponse response) throws IOException, GeneralSecurityException {
        LOG.info("Getting a list of calendars");
        createFlow();
        Credential credential = flow.loadCredential(user_id);
        System.out.println("FLOW-CREDENTIAL: " + credential);
        try {
            validateCredential(credential);
            Calendar calendar = getCalendar(credential);
            CalendarList calendarList = calendar.calendarList().list().execute();
            calendarList.getItems().parallelStream().forEach(calendarListEntry -> calendarService.addCalendarToDatabase(calendarListEntry.getId(), user_id));
            return calendarList;
        } catch (CredentialException e) {
            LOG.error("Credential exception: ", e);
            response.setStatus(401);
            response.setHeader("auth-url", authorize(user_id));
            return null;
        }
    }

    @RequestMapping("/google/calendar/active")
    public CalendarList getActiveCalendar(@RequestParam("user_id") String user_id, HttpServletResponse response) throws IOException, GeneralSecurityException {
        LOG.info("Getting a list of all activated calendars");
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
            response.setStatus(401);
            response.setHeader("auth-url", authorize(user_id));
            return null;
        }
    }

    @RequestMapping("/google/events")
    public Events events(@RequestParam("user_id") String user_id, @RequestParam("calendar_id") String calendarId, HttpServletResponse response) throws GeneralSecurityException, IOException {
        LOG.info("Getting all events for calendar id: " + calendarId);
        createFlow();
        Credential credential = flow.loadCredential(user_id);
        if (credential == null)
            response.sendRedirect("/auth-google");
        Calendar calendar = getCalendar(credential);
        final DateTime minDate = new DateTime(org.joda.time.DateTime.now().withDayOfWeek(MONDAY).toDate());
        final DateTime maxDate = new DateTime(org.joda.time.DateTime.now().withDayOfWeek(SUNDAY).toDate());
        return calendar.events().list(calendarId).setTimeMin(minDate).setTimeMax(maxDate).execute();
    }

    private Calendar getCalendar(Credential credential) {
        return new Calendar.Builder(httpTransport, jsonFactory, credential).setApplicationName(APP_NAME).build();
    }

    private String authorize(String user_id) throws GeneralSecurityException, IOException {
        LOG.info("Try to authenticate user...");
        AuthorizationCodeRequestUrl authorizationCodeRequestUrl;
        createFlow();
        authorizationCodeRequestUrl = flow
                .newAuthorizationUrl()
                .setRedirectUri(redirectURL);
        System.out.println("URL: " + authorizationCodeRequestUrl);
        return authorizationCodeRequestUrl.build();
    }

    private void createFlow() throws IOException, GeneralSecurityException {
        if (flow == null) {
            System.out.println("Creating new flow");
            GoogleClientSecrets.Details web = new GoogleClientSecrets.Details()
                    .setClientId(clientId)
                    .setClientSecret(clientSecret);
            clientSecrets = new GoogleClientSecrets().setWeb(web);
            httpTransport = GoogleNetHttpTransport.newTrustedTransport();
            flow = new GoogleAuthorizationCodeFlow.Builder(httpTransport, jsonFactory, clientSecrets, List.of(CalendarScopes.CALENDAR))
                    .setDataStoreFactory(new FileDataStoreFactory(new File("google-auth-clients")))
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
}
