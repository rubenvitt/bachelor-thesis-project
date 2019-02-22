package de.rubeen.bsc.service;

import de.rubeen.bsc.entities.provider.CalendarEvent;
import de.rubeen.bsc.entities.web.*;
import de.rubeen.bsc.service.provider.CalendarProvider;
import de.rubeen.bsc.service.provider.GoogleProviderService;
import de.rubeen.bsc.service.provider.PrototypeRoomProviderService;
import de.rubeen.bsc.service.provider.TestProviderImplementation;
import org.joda.time.DateTime;
import org.joda.time.Interval;
import org.joda.time.LocalTime;
import org.jooq.SQLDialect;
import org.jooq.impl.DSL;
import org.jooq.tools.jdbc.MockConnection;
import org.jooq.tools.jdbc.MockDataProvider;
import org.jooq.tools.jdbc.MockExecuteContext;
import org.jooq.tools.jdbc.MockResult;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.mockito.Mock;

import java.sql.SQLException;
import java.util.Collection;
import java.util.Collections;
import java.util.List;
import java.util.Set;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.when;
import static org.mockito.MockitoAnnotations.initMocks;

class EventServiceTest {

    EventService eventService;
    @Mock
    GoogleProviderService googleProviderService;
    @Mock
    ProviderService providerService;
    @Mock
    LoginService loginService;
    @Mock
    RoomService roomService;
    @Mock
    DatabaseService databaseService;
    @Mock
    UserService userService;
    @Mock
    CalendarService calendarService;

    @BeforeEach
    void setupEach() throws SQLException {
        initMocks(this);
        MockDataProvider provider = new MockDataProvider() {
            @Override
            public MockResult[] execute(MockExecuteContext mockExecuteContext) throws SQLException {
                System.out.println(mockExecuteContext);
                return new MockResult[0];
            }
        };
        MockConnection connection = new MockConnection(provider);
        //mock database
        when(databaseService.getContext()).thenReturn(DSL.using(connection, SQLDialect.POSTGRES));
        eventService = new EventService(googleProviderService, providerService, loginService, roomService,
                databaseService, userService, calendarService);
    }

    @Test
    void getAllEventsForToday() {
        //-> getAllEventsForUser
    }

    @Test
    void getAllEventsForWeekNumber() {
        //-> getAllEventsForUser()
    }

    @Test
    void getAllEventsForUser() {
        //-> databaseService, get Calendars
        //-> googleService
        // TODO: 2019-01-28 should not use googleService
    }

    @Test
    @DisplayName("Auto-Time / man-room event should be created successfully")
    void addEvent() throws CalendarProvider.CalendarException {
        final String subject = "test-subject", description = "test-description", autoTimeDateStart = "2019-01-01", autoTimeDateEnd = "2019-01-08", durationUnit = "hours";
        final int meetingDuration = 2, roomId = 123;
        final List<Integer> attendeeIds = List.of(1, 2, 3, 4, 5);

        final Integer workingHour_Id = 1;
        final String workingHour_startTime = "08:00", workingHour_endTime = "16:00";
        final boolean workingHour_monday = false, workingHour_tuesday = false, workingHour_wednesday = false,
                workingHour_thursday = false, workingHour_friday = true, workingHour_saturday = true,
                workingHour_sunday = false;

        final Integer[] eventsCreated = {0};

        //generate new appUser with given id in all parameters
        when(userService.getAppUser(anyInt())).then(invocationOnMock -> {
            int userId = invocationOnMock.getArgument(0);
            return new AppUserEntity(userId, "username-" + userId, "user-mail" + userId,
                    "avatar-" + userId, "position-" + userId);
        });

        when(userService.getAppUser(anyString())).then(invocationOnMock -> new AppUserEntity(1, "user-" + invocationOnMock.getArgument(0), "user-mail-" + invocationOnMock.getArgument(0), "avatar", "position"));
        when(providerService.getDefaultCalendar(anyString())).thenReturn(new CalendarEntity("calendar-name", "calendar-id", true, "test-provider", true));

        when(userService.getWorkingHours(anyString())).thenReturn(
                List.of(
                        new LoginHoursEntity(workingHour_Id, workingHour_startTime, workingHour_endTime,
                                workingHour_monday, workingHour_tuesday, workingHour_wednesday, workingHour_thursday,
                                workingHour_friday, workingHour_saturday, workingHour_sunday)
                )
        );
        //Stream<Interval> busyTimePeriods, Stream<LoginHoursEntity> workingHours, DateTime start, DateTime end
        when(roomService.getRoomById(roomId)).thenReturn(new RoomEntity(roomId, "test-room-name", 2, Collections.emptyList()));
        when(calendarService.getFreeTimes(any(), any(), any(), any())).thenReturn(
                List.of(
                        new Interval(DateTime.parse("2019-01-01"), DateTime.parse("2019-01-04").withTime(LocalTime.parse("09:00"))),
                        new Interval(DateTime.parse("2019-01-04").withTime(LocalTime.parse("15:00")), DateTime.parse("2019-01-08").withTime(LocalTime.parse("23:59")))
                )
        );
        when(providerService.getCalendarProvider(anyString(), anyString())).thenReturn(new CalendarProvider() {
            @Override
            public boolean createEvent(CalendarEvent calendarEvent, String userId) throws CalendarException {
                eventsCreated[0]++;
                return true;
            }

            @Override
            public List<CalendarEntity> getAllCalendars(String user_id) throws CalendarException {
                return null;
            }

            @Override
            public List<CalendarEntity> getAllActiveCalendars(String user_id) throws CalendarException {
                return null;
            }

            @Override
            public List<CalendarEvent> getEventsBetween(Interval interval, String userId, String calendarId) throws CalendarException {
                return null;
            }

            @Override
            public List<Interval> getBusyTimes(String userId, NewEventEntity eventEntity) throws CalendarException {
                return List.of(new Interval(0, 1));
            }

            @Override
            public CalendarEntity getCalendar(String calendarId, String userMail, boolean isActivated, boolean isDefault) {
                return null;
            }
        });

        when(providerService.getRoomCalendarProvider(roomId)).thenReturn(new PrototypeRoomProviderService(databaseService));

        NewEventEntity newEventEntity = new NewEventEntity(subject, description, true, false, autoTimeDateStart,
                autoTimeDateEnd, meetingDuration, durationUnit, attendeeIds);
        newEventEntity.setRoomId(roomId);

        eventService.addEvent(newEventEntity, "user@mail", "cal-id");

        assertThat(eventsCreated[0])
                .isEqualTo(6);
    }

    @Test
    @DisplayName("TimeInterval-Union for attendees should be calculated correctly")
    void unionForAttendeeTimeIntervalsTest() {
        Collection<Interval> user1 = List.of(
                Interval.parse("2019-02-18T10:00:00.000+01:00/2019-02-18T16:00:00.000+01:00"),
                Interval.parse("2019-02-21T08:00:00.000+01:00/2019-02-21T16:00:00.000+01:00"),
                Interval.parse("2019-02-19T09:00:00.000+01:00/2019-02-19T16:00:00.000+01:00")
        );
        Collection<Interval> user2 = List.of(
                Interval.parse("2019-02-18T08:00:00.000+01:00/2019-02-18T16:00:00.000+01:00"),
                Interval.parse("2019-02-21T08:00:00.000+01:00/2019-02-21T16:00:00.000+01:00")
        );
        Interval resultInterval = Interval.parse("2019-02-18T10:00:00.000+01:00/2019-02-18T16:00:00.000+01:00");
        Set<Collection<Interval>> freeTimesPerAttendee = Set.of(user1, user2);
        Collection<Interval> unionOfAttendeeFreeTimes = eventService.getUnionOfAttendeeFreeTimes(freeTimesPerAttendee);
        assertThat(unionOfAttendeeFreeTimes)
                .hasSize(1)
                .contains(resultInterval);
    }

    @Test
    @DisplayName("Event-times should be calculated successfully with calendarService")
    void addEventWithRealCalendarService() throws SQLException {
        eventService = new EventService(googleProviderService, providerService, loginService, roomService, databaseService, userService,
                new CalendarService(loginService, databaseService));
        when(providerService.getCalendarProvider(anyString(), anyString())).thenReturn(new TestProviderImplementation());

        /*
        public NewEventEntity(String subject,
                      String description,
                      boolean autoTime,
                      boolean autoRoom,
                      String autoTimeDateStart,
                      String autoTimeDateEnd,
                      Integer meetingDuration,
                      String durationUnit,
                      List<Integer> attendees)
         */

    }
}