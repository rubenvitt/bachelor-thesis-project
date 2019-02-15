package de.rubeen.bsc.controller;

import de.rubeen.bsc.entities.web.CalendarEntity;
import de.rubeen.bsc.entities.web.EventEntity;
import de.rubeen.bsc.entities.web.NewEventEntity;
import de.rubeen.bsc.service.CalendarService;
import de.rubeen.bsc.service.EventService;
import de.rubeen.bsc.service.ProviderService;
import de.rubeen.bsc.service.provider.CalendarProvider;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import javax.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.util.List;

import static com.google.common.base.Preconditions.checkArgument;

@RestController
@RequestMapping("/calendar")
public class CalendarController {
    private final Logger LOG = LoggerFactory.getLogger(this.getClass());
    private final CalendarService calendarService;
    private final EventService eventService;
    private final ProviderService providerService;

    public CalendarController(CalendarService calendarService, EventService eventService, ProviderService providerService) {
        this.calendarService = calendarService;
        this.eventService = eventService;
        this.providerService = providerService;
    }

    @RequestMapping(value = "/activate", method = RequestMethod.POST)
    public ResponseEntity activateCalendar(@RequestParam("calendar_id") String calendarID,
                                           @RequestParam("user_id") String userID,
                                           @RequestParam("activated") Boolean activated) {
        LOG.info("Updating calendar: {} for {} to activated={}", calendarID, userID, activated);
        calendarService.setCalendarState(calendarID, userID, activated);
        return ResponseEntity.ok().build();
    }

    @RequestMapping(value = "/events/today", method = RequestMethod.GET)
    public List<EventEntity> allEventsOfActivatedCalendars(@RequestParam("user_id") String userID) {
        return eventService.getAllEventsForToday(userID);
    }

    @RequestMapping(value = "/events/week", method = RequestMethod.GET)
    public List<EventEntity> allEventsOfWeek(@RequestParam(value = "week", required = false) Integer week,
                                             @RequestParam("user_id") String userID) {
        return eventService.getAllEventsForWeekNumber(week, userID);
    }

    @RequestMapping(value = "/events/create", method = RequestMethod.POST, consumes = "application/json")
    public void createNewEvent(@RequestBody NewEventEntity newEventEntity, HttpServletResponse response,
                               @RequestParam(value = "user_id") String userId,
                               @RequestParam(value = "calendar_id") String calendarId) throws IOException {
        try {
            LOG.info("got event entity: " + newEventEntity);
            checkNewEvent(newEventEntity);
            eventService.addEvent(newEventEntity, userId.replace("@", "%40"), calendarId);
        } catch (IllegalArgumentException ex) {
            response.sendError(HttpServletResponse.SC_BAD_REQUEST, ex.getMessage());
        } catch (CalendarProvider.CalendarException e) {
            response.sendError(HttpServletResponse.SC_BAD_REQUEST, "Event was not created - contact administrator.");
        }
    }

    @RequestMapping(value = "/active", method = RequestMethod.GET)
    public List<CalendarEntity> getActiveCalendars(@RequestParam("user_id") String user_id, HttpServletResponse response) {
        return providerService.getAllCalendarEntities(user_id);
    }

    private void checkNewEvent(NewEventEntity newEventEntity) {
        checkArgument(!newEventEntity.getSubject().isBlank(), "Subject can't be blank");
        //if manual time, manual date & time are required
        checkArgument(/* auto-time */ (newEventEntity.getAutoTime()
                /* man-time */ || !(newEventEntity.getManTimeDateEnd().isBlank() || newEventEntity.getManTimeDateStart().isBlank()
                || newEventEntity.getManTimeTimeEnd().isBlank() || newEventEntity.getManTimeTimeStart().isBlank())));
        checkArgument(/* auto-room // man-room */ newEventEntity.getAutoRoom() || newEventEntity.getRoomId() != null,
                "need a room for manual room");
    }

    @RequestMapping(value = "/events/user_quality", method = RequestMethod.GET)
    public long getUserQualityValueForEvent(@RequestParam(value = "user_id") int userId,
                                           @RequestParam(value = "start_date") String startDate,
                                           @RequestParam(value = "start_time") String startTime,
                                           @RequestParam(value = "end_date") String endDate,
                                           @RequestParam(value = "end_time") String endTime) {
        return eventService.getUserQualityValue(userId, startDate, startTime, endDate, endTime);
    }
}
