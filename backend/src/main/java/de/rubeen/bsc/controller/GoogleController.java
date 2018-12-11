package de.rubeen.bsc.controller;

import com.google.api.client.auth.oauth2.AuthorizationCodeRequestUrl;
import com.google.api.client.auth.oauth2.Credential;
import com.google.api.client.auth.oauth2.TokenResponse;
import com.google.api.client.googleapis.auth.oauth2.GoogleAuthorizationCodeFlow;
import com.google.api.client.googleapis.auth.oauth2.GoogleClientSecrets;
import com.google.api.client.googleapis.auth.oauth2.GoogleCredential;
import com.google.api.client.googleapis.javanet.GoogleNetHttpTransport;
import com.google.api.client.http.javanet.NetHttpTransport;
import com.google.api.client.json.JsonFactory;
import com.google.api.client.json.jackson2.JacksonFactory;
import com.google.api.client.util.DateTime;
import com.google.api.services.calendar.Calendar;
import com.google.api.services.calendar.CalendarScopes;
import com.google.api.services.calendar.model.CalendarList;
import com.google.api.services.calendar.model.Event;
import com.google.api.services.calendar.model.Events;
import org.joda.time.DateTimeConstants;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.servlet.view.RedirectView;

import javax.servlet.http.Cookie;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.servlet.http.HttpSession;
import java.io.IOException;
import java.security.GeneralSecurityException;
import java.util.List;
import java.util.Set;

import static org.joda.time.DateTimeConstants.MONDAY;
import static org.joda.time.DateTimeConstants.SUNDAY;

@RestController
public class GoogleController {
    private static String APP_NAME = "My-Business-Day";
    GoogleClientSecrets clientSecrets;
    GoogleAuthorizationCodeFlow flow;
    Credential credential;
    JsonFactory jsonFactory = JacksonFactory.getDefaultInstance();
    Logger LOG = LoggerFactory.getLogger(this.getClass());
    @Value("${google.client.client-id}")
    private String clientId;
    @Value("${google.client.client-secret}")
    private String clientSecret;
    @Value("${google.client.redirectUri}")
    private String redirectURL;
    private Set<Event> eventSet;
    private NetHttpTransport httpTransport;

    public Set<Event> getEventSet() {
        return eventSet;
    }

    public void setEventSet(Set<Event> eventSet) {
        this.eventSet = eventSet;
    }

    @RequestMapping(value = "/auth-google", method = RequestMethod.GET)
    public RedirectView googleConnectionStatus() throws GeneralSecurityException, IOException {
        return new RedirectView(authorize());
    }

    @RequestMapping(value = "/auth-google", method = RequestMethod.GET, params = "code")
    public String oauth2Callback(@RequestParam(value = "code") String code, HttpServletResponse response) {
        Events events;
        String message;
        try {
            TokenResponse tokenResponse = flow.newTokenRequest(code).setRedirectUri(redirectURL).execute();
            credential = flow.createAndStoreCredential(tokenResponse, "userID");
            message = credential.getAccessToken();
            //Cookie cookie = new Cookie("google-access-key", tokenResponse.getAccessToken());
            //response.addCookie(cookie);
            response.addCookie(new Cookie("google-access-key", tokenResponse.getAccessToken()));
            //System.out.println(cookie.getValue());
            Calendar calendar = getCalendar();
            response.sendRedirect("http://localhost:3333/settings");

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
    public CalendarList getAllCalendar(@RequestParam("access_token") String access_token) throws IOException, GeneralSecurityException {
        LOG.info("Getting a list of calendars");
        saveAuthToken(access_token);
        authorize();
        Calendar calendar = getCalendar();
        CalendarList execute = calendar.calendarList().list().execute();
        System.out.println(calendar);
        //TokenResponse response = flow.newTokenRequest(code).setRedirectUri("http://localhost:8080/auth-google").execute();
        //Calendar calendar = new Calendar.Builder(httpTransport, jsonFactory, credential).setApplicationName("App-Name").build();
        //return calendar.calendarList();
        return execute;
    }

    private void saveAuthToken(String access_token) {
        if (credential == null) {
            credential = new GoogleCredential().setAccessToken(access_token);
        }
    }

    @RequestMapping("/google/events")
    public Events events(@RequestParam("access_token") String access_token, @RequestParam("calendar_id") String calendarId) throws GeneralSecurityException, IOException {
        LOG.info("Getting all events for calendar id: " + calendarId);
        saveAuthToken(access_token);
        authorize();
        Calendar calendar = getCalendar();
        final DateTime minDate = new DateTime(org.joda.time.DateTime.now().withDayOfWeek(MONDAY).toDate());
        final DateTime maxDate = new DateTime(org.joda.time.DateTime.now().withDayOfWeek(SUNDAY).toDate());
        return calendar.events().list(calendarId).setTimeMin(minDate).setTimeMax(maxDate).execute();
    }

    private Calendar getCalendar() {
        return new Calendar.Builder(httpTransport, jsonFactory, credential).setApplicationName(APP_NAME).build();
    }

    private String authorize() throws GeneralSecurityException, IOException {
        LOG.info("Try to authenticate user...");
        AuthorizationCodeRequestUrl authorizationCodeRequestUrl;
        if (flow == null) {
            GoogleClientSecrets.Details web = new GoogleClientSecrets.Details()
                    .setClientId(clientId)
                    .setClientSecret(clientSecret);
            clientSecrets = new GoogleClientSecrets().setWeb(web);
            httpTransport = GoogleNetHttpTransport.newTrustedTransport();
            flow = new GoogleAuthorizationCodeFlow.Builder(httpTransport, jsonFactory, clientSecrets, List.of(CalendarScopes.CALENDAR)).build();
        }
        authorizationCodeRequestUrl = flow.newAuthorizationUrl().setRedirectUri(redirectURL);
        System.out.println("URL: " + authorizationCodeRequestUrl);
        return authorizationCodeRequestUrl.build();
    }
}
