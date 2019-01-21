package de.rubeen.bsc.controller;

import de.rubeen.bsc.entities.web.EventEntity;
import de.rubeen.bsc.entities.web.NewEventEntity;
import de.rubeen.bsc.service.CalendarService;
import de.rubeen.bsc.service.EventService;
import org.joda.time.DateTime;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.Mock;
import org.springframework.mock.web.MockHttpServletResponse;

import java.io.IOException;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.anyBoolean;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.*;
import static org.mockito.MockitoAnnotations.initMocks;

class CalendarControllerTest {
    @Mock
    static CalendarService calendarService;
    @Mock
    EventService eventService;
    private CalendarController calendarController;

    @BeforeEach
    void setup() {
        initMocks(this);
        calendarController = new CalendarController(calendarService, eventService);
    }

    @Test
    void activateCalendar() {
        doNothing().when(calendarService).setCalendarState(anyString(), anyString(), anyBoolean());

        assertThat(calendarController.activateCalendar("", "", false).getStatusCode().is2xxSuccessful())
                .isTrue();
    }

    @Test
    void allEventsOfActivatedCalendars() {
        final String userMail = "user-mail";
        final List<EventEntity> correctResultList = List.of(new EventEntity("Subject", DateTime.now(), DateTime.now().minus(10)));

        when(eventService.getAllEventsForToday(userMail))
                .thenReturn(correctResultList);

        assertThat(calendarController.allEventsOfActivatedCalendars(userMail))
                .isEqualTo(correctResultList);
        verify(eventService, only()).getAllEventsForToday(anyString());
    }

    @Test
    void allEventsOfWeek() {
        final int week = 2;
        final String userId = "userId";
        final List<EventEntity> correctResultList = List.of(new EventEntity("Subject", DateTime.now(), DateTime.now().minus(30)));

        when(eventService.getAllEventsForWeekNumber(week, userId))
                .thenReturn(correctResultList);

        assertThat(calendarController.allEventsOfWeek(week, userId))
                .isEqualTo(correctResultList);
        verify(eventService, only()).getAllEventsForWeekNumber(anyInt(), anyString());
    }


    /*
    String subject, String description, boolean autoTime, boolean autoRoom,
                          String manTimeDateStart, String manTimeDateEnd, String autoTimeDateStart, String autoTimeDateEnd, Integer meetingDuration, String durationUnit, String manTimeTimeStart, String manTimeTimeEnd,
                          List<String> roomValues, Integer roomId, List<Integer> attendees
     */
    @Test
    void createNewEvent() throws IOException {
        doNothing().when(eventService).addEvent(any(), anyString(), anyString());
        MockHttpServletResponse response = new MockHttpServletResponse();
        NewEventEntity eventEntity = new NewEventEntity(
                "Subject", "Description", true, true, "manTimeDateStart",
                "manTimeDateEnd", "autoTimeStart", "autoTimeEnd",
                30, "durationUnit", "manTimeStart", "manTimeEnd",
                List.of("roomValue", "roomValue2"), 1, List.of(1, 2, 3));
        String userId = "userId", calendarId = "calendarId";
        calendarController.createNewEvent(eventEntity, response, userId, calendarId);
        verify(eventService, only()).addEvent(any(), anyString(), anyString());
    }

    @Test
    void testIncorrectNewEvent() {
        //fail, if...
        //0) everything is empty
        NewEventEntity emptyEntity = new NewEventEntity();

        //1) no subject
        NewEventEntity entityWithoutSubject = getEntityWithoutSubject();

        //2) autoTime? -> no autoTimeStart & autoTimeEnd & meetingDuration
        NewEventEntity entityAutoWithoutStart = getAutoEntity("", "autoTimeEnd", 13);
        NewEventEntity entityAutoWithoutEnd = getAutoEntity("autoTimeStart", "", 13);
        NewEventEntity entityAutoWithoutDuration = getAutoEntity("autoTimeStart", "autoTimeEnd", 0);

        //3) manTime? -> no manTimeStart, manTimeEnd, manDateTimeStart, manDateTimeEnd
        NewEventEntity entityManWithoutStartTime = getManEntity("", "manTimeEnd", "manDateStart", "manDateEnd");
        NewEventEntity entityManWithoutStartDate = getManEntity("manTimeStart", "manTimeEnd", "", "manDateEnd");
        NewEventEntity entityManWithoutManTimeEnd = getManEntity("manTimeStart", "", "manDateStart", "manDateEnd");
        NewEventEntity entityManWithoutManDateEnd = getManEntity("manTimeStart", "manTimeEnd", "manDateStart", "");

        //incorrect values...
        //autoTime:
        NewEventEntity entityAutoWithWrongValues = getAutoEntity("xx", "xx", 0);
        NewEventEntity entityManWithWrongValues = getManEntity("xx", "xx", "xx", "xx");

        //some correct Entities
        List<NewEventEntity> correctEntities = List.of(
                getAutoEntity("2019-01-01", "2019-01-31", 13),
                getManEntity("11:00", "12:00", "2019-01-01", "2019-01-01")
        );
    }

    private NewEventEntity getManEntity(String timeStart, String timeEnd, String dateStart, String dateEnd) {
        return new NewEventEntity(
                "Subject", "Description", false, true, dateStart,
                dateEnd, "autoTimeStart", "autoTimeEnd",
                30, "durationUnit", timeStart, timeEnd,
                List.of("roomValue", "roomValue2"), 1, List.of(1, 2, 3));
    }


    private NewEventEntity getAutoEntity(String timeStart, String autoTimeEnd, int duration) {
        return new NewEventEntity(
                "Subject", "Description", true, true, "manTimeDateStart",
                "manTimeDateEnd", timeStart, autoTimeEnd,
                duration, "durationUnit", "manTimeStart", "manTimeEnd",
                List.of("roomValue", "roomValue2"), 1, List.of(1, 2, 3));
    }

    private NewEventEntity getEntityWithoutSubject() {
        return new NewEventEntity(
                "", "Description", true, true, "manTimeDateStart",
                "manTimeDateEnd", "autoTimeStart", "autoTimeEnd",
                30, "durationUnit", "manTimeStart", "manTimeEnd",
                List.of("roomValue", "roomValue2"), 1, List.of(1, 2, 3));
    }
}