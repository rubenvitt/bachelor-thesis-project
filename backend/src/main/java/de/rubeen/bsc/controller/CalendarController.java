package de.rubeen.bsc.controller;

import de.rubeen.bsc.entities.web.EventEntity;
import de.rubeen.bsc.service.CalendarService;
import org.joda.time.DateTime;
import org.joda.time.Hours;
import org.joda.time.Minutes;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.util.Date;
import java.util.List;

@RestController
@RequestMapping("/calendar")
public class CalendarController {
    private final Logger LOG = LoggerFactory.getLogger(this.getClass());
    private final CalendarService calendarService;

    public CalendarController(CalendarService calendarService) {
        this.calendarService = calendarService;
    }

    @RequestMapping(value = "/activate", method = RequestMethod.POST)
    public ResponseEntity activateCalendar(@RequestParam("calendar_id") String calendarID,
                                           @RequestParam("user_id") String userID,
                                           @RequestParam("activated") Boolean activated) {
        LOG.info("Updating calendar: {} for {} to activated={}", calendarID, userID, activated);
        calendarService.setCalendarState(calendarID, userID, activated);
        return ResponseEntity.ok().build();
    }

    @RequestMapping(value = "/events", method = RequestMethod.GET)
    public List<EventEntity> allEventsOfActivatedCalendars(@RequestParam("user_id") String userID) {
        return List.of(
                new EventEntity("Past Test-Event", DateTime.now().minus(Hours.hours(2)), DateTime.now().minus(Hours.hours(1))),
                new EventEntity("Actual Test-Event", DateTime.now(), DateTime.now().plus(Minutes.minutes(30))),
                new EventEntity("Upcoming Test-Event", DateTime.now().plus(Hours.hours(1)), DateTime.now().plus(Hours.hours(2))));
    }
}
