package de.rubeen.bsc.controller;

import de.rubeen.bsc.entities.web.EventEntity;
import de.rubeen.bsc.entities.web.NewEventEntity;
import de.rubeen.bsc.service.CalendarService;
import de.rubeen.bsc.service.EventService;
import org.joda.time.DateTime;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/calendar")
public class CalendarController {
    private final Logger LOG = LoggerFactory.getLogger(this.getClass());
    private final CalendarService calendarService;
    private final EventService eventService;

    public CalendarController(CalendarService calendarService, EventService eventService) {
        this.calendarService = calendarService;
        this.eventService = eventService;
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

    /*@RequestMapping(value = "/events", method = RequestMethod.GET)
    public List<EventEntity> allEventsOfActivatedCalendars(@RequestParam("user_id") String userID,
                                                           @RequestParam("time-start") Long startMillis,
                                                           @RequestParam("time-end") Long endMillis) {
        return eventService.getAllEventsForUser(userID, startMillis, endMillis);
    }*/

    @RequestMapping(value = "/events/create", method = RequestMethod.POST, consumes = "application/json")
    public void createNewEvent(@RequestBody NewEventEntity newEventEntity) {
        LOG.info("got event entity: " + newEventEntity);
    }

    @RequestMapping(value = "/test", method = RequestMethod.GET)
    public DateTime testGetter(@RequestParam("week") int week, @RequestParam("t") int t) {
        return eventService.getTest(week, t);
    }
}
