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
import com.google.api.services.calendar.Calendar;
import com.google.api.services.calendar.CalendarScopes;
import com.google.api.services.calendar.model.Event;
import com.google.api.services.calendar.model.Events;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.PropertySource;
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

@RestController
public class GoogleController {
    GoogleClientSecrets clientSecrets;
    GoogleAuthorizationCodeFlow flow;
    Credential credential;
    Calendar calendar;
    JsonFactory jsonFactory = JacksonFactory.getDefaultInstance();

    @Value("${google.client.client-id}")
    private String clientId;
    @Value("${google.client.client-secret}")
    private String clientSecret;
    @Value("${google.client.redirectUri}")
    private String redirectURL;

    private Set<Event> eventSet;
    private NetHttpTransport httpTransport;

    private DateTime minDate = new DateTime(org.joda.time.DateTime.now().minusMonths(10).toDate());
    private DateTime maxDate = new DateTime(org.joda.time.DateTime.now().plusMonths(10).toDate());

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
            calendar = new Calendar.Builder(httpTransport, jsonFactory, credential).setApplicationName("Application-Name").build();
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

    private String authorize() throws GeneralSecurityException, IOException {
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
