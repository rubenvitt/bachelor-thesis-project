package de.rubeen.bsc.controller;

import com.google.api.services.calendar.model.FreeBusyResponse;
import de.rubeen.bsc.entities.web.CalendarEntity;
import de.rubeen.bsc.service.provider.CalendarProvider;
import de.rubeen.bsc.service.provider.GoogleProviderService;
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
import java.io.IOException;
import java.security.GeneralSecurityException;
import java.util.List;

import static java.text.MessageFormat.format;

// TODO: 19.12.2018 cleanup & implement google service

@RestController
public class GoogleController {
    private final Logger LOG = LoggerFactory.getLogger(this.getClass());

    private final GoogleProviderService googleProviderService;

    @Value("${webapp.url}")
    String webAppUrl;

    @Autowired
    public GoogleController(GoogleProviderService googleProviderService) {
        this.googleProviderService = googleProviderService;
    }

    @RequestMapping(value = "/auth-google", method = RequestMethod.GET)
    public RedirectView googleConnectionStatus() throws GeneralSecurityException, IOException {
        return new RedirectView(googleProviderService.authorize());
    }

    @RequestMapping(value = "/auth-google", method = RequestMethod.GET, params = "code")
    public ResponseEntity oauth2Callback(@RequestParam(value = "code") String code, HttpServletRequest request, HttpServletResponse response) throws IOException {
        String message;
        Cookie cookie = null;
        for (Cookie thisCookie : request.getCookies()) {
            if (thisCookie.getName().equals("USER-ID"))
                cookie = thisCookie;
        }
        if (cookie == null) {
            response.sendError(HttpServletResponse.SC_NOT_ACCEPTABLE, "No Cookie USER-ID found");
            return null;
        }
        try {
            googleProviderService.createCredentialFromCallback(code, cookie.getValue());
            response.sendRedirect(format("{0}/settings", webAppUrl));
        } catch (IOException e) {
            message = format("Exception while handling OAuth2 callback ({0}). Redirecting to google connection status page.", e.getMessage());
            LOG.error(message);
        }
        response.setStatus(200);
        return ResponseEntity.ok().build();
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
    public List<CalendarEntity> getAllCalendar(@RequestParam("user_id") String user_id, HttpServletResponse response) throws IOException, GeneralSecurityException {
        LOG.info("Getting a list of calendars");
        try {
            return googleProviderService.getAllCalendars(user_id);
        } catch (CalendarProvider.CalendarException e) {
            response.setStatus(401);
            response.setHeader("auth-url", googleProviderService.authorize());
            return null;
        }
    }

    @RequestMapping("/google/calendar/active")
    public List<CalendarEntity> getActiveCalendar(@RequestParam("user_id") String user_id, HttpServletResponse response) throws IOException, GeneralSecurityException {
        LOG.info("Getting a list of all activated calendars");
        try {
            return googleProviderService.getAllActiveCalendars(user_id);
        } catch (CalendarProvider.CalendarException e) {
            response.setStatus(401);
            response.setHeader("auth-url", googleProviderService.authorize());
            return null;
        }
    }
}