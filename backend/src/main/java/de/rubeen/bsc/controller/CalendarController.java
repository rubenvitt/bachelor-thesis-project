package de.rubeen.bsc.controller;

import de.rubeen.bsc.service.CalendarService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/calendar")
public class CalendarController {
    private final Logger LOG = LoggerFactory.getLogger(this.getClass());
    private final CalendarService calendarService;

    public CalendarController(CalendarService calendarService) {
        this.calendarService = calendarService;
    }

    @RequestMapping(value = "/activate", method = RequestMethod.PUT)
    public ResponseEntity activateCalendar(@RequestParam("calendar_id") String calendarID,
                                           @RequestParam("user_id") String userID,
                                           @RequestParam("activated") Boolean activated) {
        LOG.info("Updating calendar: {} for {} to activated={}", calendarID, userID, activated);
        calendarService.setCalendarState(calendarID, userID, activated);
        return ResponseEntity.ok().build();
    }
}
