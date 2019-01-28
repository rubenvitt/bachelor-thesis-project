package de.rubeen.bsc.controller;

import de.rubeen.bsc.entities.web.CalendarEntity;
import de.rubeen.bsc.provider.office365.*;
import de.rubeen.bsc.provider.office365.entities.Calendar;
import de.rubeen.bsc.provider.office365.entities.Event;
import de.rubeen.bsc.provider.office365.entities.OutlookUser;
import de.rubeen.bsc.provider.office365.entities.PagedResult;
import de.rubeen.bsc.service.provider.CalendarProvider;
import de.rubeen.bsc.service.provider.OfficeProviderService;
import org.jooq.meta.derby.sys.Sys;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import javax.servlet.http.Cookie;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.servlet.http.HttpSession;
import java.io.IOException;
import java.security.GeneralSecurityException;
import java.util.LinkedList;
import java.util.List;
import java.util.UUID;

import static java.text.MessageFormat.format;

@RestController
public class Office365Controller {
    final OfficeProviderService officeProviderService;
    @Value("${webapp.url}")
    String webappUrl;
    Logger LOG = LoggerFactory.getLogger(this.getClass());

    public Office365Controller(OfficeProviderService officeProviderService) {
        this.officeProviderService = officeProviderService;
    }

    @RequestMapping(value = "/auth-office", method = RequestMethod.POST)
    public void authorize(
            @RequestParam("code") String code,
            @RequestParam("id_token") String idToken,
            @RequestParam("state") UUID state,
            HttpServletRequest request,
            HttpServletResponse response) throws IOException {
        // Get the expected state value from the session
        HttpSession session = request.getSession();
        UUID expectedState = (UUID) session.getAttribute("expected_state");
        UUID expectedNonce = (UUID) session.getAttribute("expected_nonce");

        // Make sure that the state query parameter returned matches
        // the expected state
        if (state.equals(expectedState)) {
            IdToken idTokenObj = IdToken.parseEncodedToken(idToken, expectedNonce.toString());
            if (idTokenObj != null) {
                TokenResponse tokenResponse = AuthHelper.getTokenFromAuthCode(code, idTokenObj.getTenantId());
                tokenResponse.setTokenTendantId(idTokenObj.getTenantId());
                String userId = (String) session.getAttribute("user_id");
                officeProviderService.saveToken(userId, tokenResponse);

                TokenResponse token = officeProviderService.getToken(userId);

                //session.setAttribute("tokens", tokenResponse);
                //session.setAttribute("userConnected", true);
                //session.setAttribute("userName", idTokenObj.getName());

                OutlookService outlookService = OutlookServiceBuilder.getOutlookService(token.getAccessToken(), null);
                OutlookUser user = outlookService.getCurrentUser().execute().body();
                LOG.info("got outlook-user: {}", user);
                response.sendRedirect(format("{0}/settings", webappUrl));
            } else {
                LOG.error("ID token failed validation.");
            }
        } else {
            LOG.error("Unexpected state returned from authority");
        }
    }

    @RequestMapping("/logout-office")
    public ResponseEntity<String> logout(HttpServletRequest request, HttpServletResponse response) {
        HttpSession session = request.getSession();
        session.invalidate();
        return new ResponseEntity<>(HttpStatus.OK);
    }

    @RequestMapping(value = "/auth-office", method = RequestMethod.GET)
    public String index(HttpServletRequest request, HttpServletResponse response, @RequestParam("user_id") String userId) throws IOException {
        UUID state = UUID.randomUUID();
        UUID nonce = UUID.randomUUID();

        // Save the state and nonce in the session so we can
        // verify after the controller process redirects back
        HttpSession session = request.getSession();
        session.setAttribute("user_id", userId);
        session.setAttribute("expected_state", state);
        session.setAttribute("expected_nonce", nonce);

        response.sendRedirect(AuthHelper.getLoginUrl(state, nonce));
        return AuthHelper.getLoginUrl(state, nonce);
    }

    @RequestMapping("/office/calendar/active")
    public List<CalendarEntity> getActiveCalendar(@RequestParam("user_id") String user_id, HttpServletResponse response) throws IOException, GeneralSecurityException {
        LOG.info("Getting a list of all activated calendars");
        try {
            return officeProviderService.getAllActiveCalendars(user_id);
        } catch (CalendarProvider.CalendarException e) {
            response.setStatus(401);
            return null;
        }
    }


///////// TESTS /////////////

    @RequestMapping("/office/events")
    public List<Event> events(Model model, HttpServletRequest request, HttpServletResponse response) throws IOException {
        HttpSession session = request.getSession();
        TokenResponse tokens = (TokenResponse) session.getAttribute("tokens");
        if (tokens == null) {
            LOG.error("User has to log in!");
            response.sendError(HttpServletResponse.SC_UNAUTHORIZED, "please log in");
        }

        String tenantId = (String) session.getAttribute("userTenantId");

        tokens = AuthHelper.ensureTokens(tokens, tenantId);

        String email = (String) session.getAttribute("userPrincipal");

        OutlookService outlookService = OutlookServiceBuilder.getOutlookService(tokens.getAccessToken(), email);

        // Sort by start time in descending order
        String sort = "start/dateTime DESC";
        // Only return the properties we care about
        String properties = "organizer,subject,start,end";
        // Return at most 10 events
        Integer maxResults = 10;

        try {
            PagedResult<Event> events = outlookService.getEvents(
                    sort, properties, maxResults)
                    .execute().body();
            return events.getValue();
        } catch (IOException e) {
            LOG.error("error: ", e);
            response.sendError(HttpServletResponse.SC_BAD_REQUEST, e.toString());
        }
        return null;
    }

    @RequestMapping("/office/calendar")
    public List<CalendarEntity> calendars(Model model, HttpServletRequest request, HttpServletResponse response,
                                          @RequestParam("user_id") String userId) throws IOException {
        try {
            return officeProviderService.getAllCalendars(userId);
        } catch (CalendarProvider.CalendarException e) {
            LOG.error("Calendar-Exception");
            response.sendError(HttpServletResponse.SC_BAD_REQUEST);
        }
        return null;
    }
}
