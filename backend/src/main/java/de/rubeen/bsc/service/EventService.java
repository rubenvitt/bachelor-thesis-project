package de.rubeen.bsc.service;

import de.rubeen.bsc.entities.provider.CalendarEvent;
import de.rubeen.bsc.entities.web.*;
import de.rubeen.bsc.helper.EventComparatorFactory;
import de.rubeen.bsc.service.provider.CalendarProvider;
import org.apache.commons.lang3.NotImplementedException;
import org.joda.time.*;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.sql.SQLException;
import java.util.*;
import java.util.stream.Collectors;

import static de.rubeen.bsc.entities.db.Tables.APPUSER;
import static de.rubeen.bsc.entities.db.Tables.CALENDAR;
import static de.rubeen.bsc.service.TimeCalculationService.*;
import static java.text.MessageFormat.format;
import static java.util.Objects.isNull;

@Service
public class EventService extends LoggableService {
    private final ProviderService providerService;
    private final LoginService loginService;
    private final RoomService roomService;
    private final DatabaseService databaseService;
    private final UserService userService;
    private final CalendarService calendarService;
    private final TimeCalculationService timeCalculationService;

    @Autowired
    public EventService(ProviderService providerService, LoginService loginService, RoomService roomService, DatabaseService databaseService, UserService userService, CalendarService calendarService, TimeCalculationService timeCalculationService) throws SQLException {
        this.providerService = providerService;
        this.loginService = loginService;
        this.roomService = roomService;
        this.databaseService = databaseService;
        this.userService = userService;
        this.calendarService = calendarService;
        this.timeCalculationService = timeCalculationService;
    }

    public List<EventEntity> getAllEventsForToday(String userMail) {
        return this.getAllEventsForUser(userMail, getBeginOfToday().getMillis(), getEndOfToday().getMillis());
    }

    public List<EventEntity> getAllEventsForWeekNumber(Integer weekNumber, String userMail) {
        DateTime begin = getBeginOfWeek(weekNumber);
        DateTime end = getEndOfWeek(weekNumber);
        return this.getAllEventsForUser(userMail, begin.getMillis(), end.getMillis());
    }

    public List<EventEntity> getAllEventsForUser(String userMail, Long startMillis, Long endMillis) {
        final Interval searchInterval = new Interval(startMillis, endMillis);
        LOG.debug("Getting events for {} between {}", userMail, searchInterval);
        final Integer userID = loginService.getUserID(userMail);
        final List<String> calendars = databaseService.getContext()
                .select(CALENDAR.CALENDARID)
                .from(CALENDAR)
                .innerJoin(APPUSER).onKey()
                .where(APPUSER.ID.eq(userID))
                .and(CALENDAR.ACTIVATED.eq(true)).fetch(CALENDAR.CALENDARID);
        final List<CalendarEvent> calendarEvents = calendars.parallelStream()
                .map(calendarId -> {
                    try {
                        return providerService.getCalendarProvider(calendarId, userMail).getEventsBetween(searchInterval, userMail, calendarId);
                    } catch (CalendarProvider.CalendarException e) {
                        LOG.error("Can't get events for user: {} - calendar: {}", userMail, calendarId, e);
                        return null;
                    }
                }).filter(Objects::nonNull)
                .flatMap(List::stream)
                .collect(Collectors.toList());

        List<EventEntity> eventEntities = calendarEvents.parallelStream()
                .map(calendarEvent -> new EventEntity(calendarEvent.getSubject(), calendarEvent.getStartDateTime(), calendarEvent.getEndDateTime()))
                .collect(Collectors.toList());
        eventEntities.sort(EventComparatorFactory.getDateComparator());
        return eventEntities;
    }

    public CalendarWebEventEntity createCalendarEvent(NewEventEntity newEventEntity, String userMail, String calendarId) throws CalendarProvider.CalendarException {
        CalendarProvider calendarProvider = providerService.getCalendarProvider(calendarId, userMail);
        if (newEventEntity.getAutoTime())
            return new CalendarWebEventEntity(createAutoCalendarEvent(newEventEntity, userMail, calendarId, calendarProvider));
        else
            return new CalendarWebEventEntity(createManualCalendarEvent(newEventEntity, userMail, calendarId, calendarProvider));
    }

    private CalendarEvent createManualCalendarEvent(final NewEventEntity newEventEntity, final String userMail, final String calendarId,
                                   final CalendarProvider calendarProvider) throws CalendarProvider.CalendarException {
        LOG.info("using calendarProvider: {} to create an event for {} - calendarId: {} - event: {}",
                calendarService, userMail, calendarId, newEventEntity);

        //manual event:
        // check room Auto-Mode
        // create event for all attendees
        AppUserEntity appUserEntity = userService.getAppUser(userMail);
        CalendarEvent.Attendee creator = new CalendarEvent.Attendee(appUserEntity.getName(), appUserEntity.getMail());
        List<CalendarEvent.Attendee> attendees = getEventAttendees(newEventEntity.getAttendees());

        if (newEventEntity.getAutoRoom()) {
            throw new NotImplementedException("AutoRoom was not implemented, yet");
        } else {
            LOG.info("#1/2: Getting room...");
            // TODO: 2019-01-28 getRoom-CalendarID & provider to get freeTimes
            RoomEntity room = roomService.getRoomById(newEventEntity.getRoomId());
            LOG.debug("Got room {} for event {}", room, newEventEntity);

            LOG.info("#2/2: create manual-time-manual-room event for {}", userMail);
            DateTime startDateTime = DateTime.parse(newEventEntity.getManTimeDateStart() + "T" + newEventEntity.getManTimeTimeStart());
            DateTime endDateTime = DateTime.parse(newEventEntity.getManTimeDateEnd() + "T" + newEventEntity.getManTimeTimeEnd());

            CalendarEvent calendarEvent = new CalendarEvent(newEventEntity.getSubject(), newEventEntity.getDescription(),
                    room.getName(), calendarId, startDateTime, endDateTime, attendees, creator);
            {
                LOG.info("getting roomEquipments as string");
                final String roomEquipments = roomService.getEquipments(room.getId()).stream()
                        .map(equipmentEntity -> "- " + equipmentEntity.getName())
                        .collect(Collectors.joining("\n"));
                calendarEvent.setDescription(calendarEvent.getDescription() + "\n------------------------------\nRoom-equipment:\nSize for "+room.getSize()+" people\n" + roomEquipments);
            }
            LOG.debug("Creating event: {}", calendarEvent);

            return calendarEvent;

/*            calendarProvider.createEvent(calendarEvent, userMail);
            providerService.getRoomCalendarProvider(room.getId()).createEvent(calendarEvent, String.valueOf(room.getId()));*/
        }
    }

    private CalendarEvent createAutoCalendarEvent(final NewEventEntity newEventEntity, final String userMail, final String calendarId,
                                 final CalendarProvider calendarProvider) throws CalendarProvider.CalendarException {
        LOG.info("using calendarProvider: {} to create an event for {} - calendarId: {} - event: {}",
                calendarProvider, userMail, calendarId, newEventEntity);
        //INIT... get ID's
        LOG.info("Initializations...");
        LOG.debug("Get all attendees of {}", newEventEntity);
        List<CalendarEvent.Attendee> attendees = getEventAttendees(newEventEntity.getAttendees());
        LOG.debug("Got {} attendees: {}", attendees.size(), attendees);
        AppUserEntity creatorAppUser = userService.getAppUser(userMail);

        final RoomEntity room;
        if (newEventEntity.getAutoRoom()) {
            room = roomService.getBestRoomFor(newEventEntity.getRoomValues(), newEventEntity.getAttendees().size() + 1);
            LOG.info("Got best room: {}", room);

//            throw new NotImplementedException("Auto room was not implemented, yet");
        } else {
            //#1: get room from provider
            LOG.info("Manual room -> use fix room!");
            LOG.info("#1/4: Getting room...");
            // TODO: 2019-01-28 getRoom-CalendarID & provider to get freeTimes
            room = roomService.getRoomById(newEventEntity.getRoomId());
            room.setEquipments(roomService.getEquipments(room.getId()));
            LOG.debug("Got room {} for event {}", room, newEventEntity);
        }

        LOG.info("#2/4: Get workingHours and busyTimes for all event-attendees");
        LOG.debug("Generating collection of all attendees");
        Collection<UserTimeEntity> attendeeUserTimeEntities =
                newEventEntity.getAttendees().parallelStream()
                        .map(UserTimeEntity::new)
                        .collect(Collectors.toCollection(HashSet::new));
        attendeeUserTimeEntities.add(new UserTimeEntity(userMail));


        final HashSet<Collection<Interval>> collect = attendeeUserTimeEntities.parallelStream()
                .map(userTimeEntity -> userTimeEntity.getFreeTimesForEvent(newEventEntity))
                .collect(Collectors.toCollection(HashSet::new));
        final Collection<Interval> unionOfTimeIntervals = timeCalculationService.getUnionOfAttendeeFreeTimes(collect);
        LOG.info("Union of timeIntervals: {}", unionOfTimeIntervals);


        Interval timeSlot = timeCalculationService.searchTimeSlot(unionOfTimeIntervals, newEventEntity);
        if (timeSlot == null) {
            LOG.warn("No timeSlot found for {} - {}", userMail, newEventEntity);
            throw new CalendarProvider.CalendarException("Unable to find timeSlot for event.", null);
        } else
            LOG.debug("got timeSlot {} for {} - {}", timeSlot, userMail, newEventEntity);
        LOG.info("#4/4: create auto-time-manual-room event for {}", userMail);
        CalendarEvent calendarEvent = new CalendarEvent(newEventEntity.getSubject(), newEventEntity.getDescription(),
                room.getName(), calendarId, timeSlot, attendees,
                new CalendarEvent.Attendee(creatorAppUser.getName(), creatorAppUser.getMail()));

        {
            LOG.info("getting roomEquipments for {} as string", room);
            final String roomEquipments = room.getEquipments().stream()
                    .map(equipmentEntity -> "- " + equipmentEntity.getName())
                    .collect(Collectors.joining("\n"));
            calendarEvent.setDescription(calendarEvent.getDescription() + "\n------------------------------\nRoom-equipment:\nSize for "+room.getSize()+" people\n" + roomEquipments);
        }

        return calendarEvent;
    }

    public void addEvent(CalendarWebEventEntity webEventEntity, String userMail) throws CalendarProvider.CalendarException {
        CalendarEvent calendarEvent = new CalendarEvent(webEventEntity);
        Collection<CalendarEvent.Attendee> attendees = calendarEvent.getAttendees();
        RoomEntity room = roomService.getRoomByName(calendarEvent.getRoom());
        CalendarProvider calendarProvider = providerService.getCalendarProvider(calendarEvent.getCalendarId(), userMail);


        LOG.debug("Creating event: {}", calendarEvent);
        calendarProvider.createEvent(calendarEvent, userMail);
        attendees.parallelStream().forEach(attendee -> {
            AppUserEntity appUser = userService.getAppUser(attendee.getMail());
            //check, if user is appUser
            if (appUser != null) {
                LOG.info("Creating event for attendee: {}", attendee);
                CalendarEntity defaultCalendar = providerService.getDefaultCalendar(attendee.getMail());
                LOG.info("Default-calender for {} is {}", attendee, defaultCalendar);
                if (isNull(defaultCalendar)) {
                    LOG.warn("{} has no defaultCalendar - creating no event.", attendee);
                } else {
                    try {
                        providerService.getCalendarProvider(defaultCalendar.getCalendarID(), attendee.getMail())
                                .createEvent(calendarEvent.withCalendarId(defaultCalendar.getCalendarID()), attendee.getMail());
                    } catch (CalendarProvider.CalendarException e) {
                        LOG.error("Unable to create event {} for {}", calendarEvent, attendee);
                    }
                }
            }
        });
        LOG.info("{} -- {}", providerService, room);
        providerService.getRoomCalendarProvider(room.getId()).createEvent(calendarEvent, String.valueOf(room.getId()));
    }

    private List<Interval> getAllBusyTimes(String userMail, NewEventEntity newEventEntity) {
        return providerService.getAllCalendarEntities(userMail).parallelStream()
                .filter(Objects::nonNull)
                .map(calendarEntity -> {
                    try {
                        LOG.info("Logging some values for {}: eventEntity: {} --- calendarEntity: {}", userMail, newEventEntity, calendarEntity);
                        List<Interval> busyTimes = providerService.getCalendarProvider(calendarEntity.getCalendarID(), userMail)
                                .getBusyTimes(userMail, newEventEntity);
                        LOG.info("busyTimes for {} ({}) are: {}", userMail, calendarEntity, busyTimes);

                        return busyTimes;
                    } catch (CalendarProvider.CalendarException e) {
                        LOG.error("Unable to get busy-Times for {}", userMail, e);
                    }
                    return null;
                })
                .filter(Objects::nonNull)
                .flatMap(Collection::parallelStream)
                .collect(Collectors.toList());
    }

    private List<CalendarEvent.Attendee> getEventAttendees(Collection<Integer> attendees) {
        return attendees.parallelStream()
                .map(userService::getAppUser)
                .filter(Objects::nonNull)
                .map(appUserEntity -> new CalendarEvent.Attendee(appUserEntity.getName(), appUserEntity.getMail()))
                .collect(Collectors.toList());
    }

    public long getUserQualityValue(int userId, String startDate, String startTime, String endDate, String endTime) {
        // TODO: 2019-02-05 implementation: 100: user completely free || 0: user has NO time between
        final DateTime startDateTime = DateTime.parse(startDate + "T" + startTime);
        final DateTime endDateTime = DateTime.parse(endDate + "T" + endTime);
        final String userMail = userService.getAppUser(userId).getMail();

        LOG.info("Getting quality-times for {} between {} and {}", userMail, startDateTime, endDateTime);

        // TODO: 2019-03-04 add implementation in frontend & here:
        /* this is better, but doesn't work, if duration is unknown.
        //2: get workingHours & busyTimes
        List<LoginHoursEntity> workingHours = userService.getWorkingHours(userMail);
        LOG.debug("Got workingHours {} for user {}", workingHours, userMail);

        NewEventEntity newEventEntity = new NewEventEntity("", "", true, false, startDate, endDate, new Duration(startDateTime, endDateTime).toStandardMinutes().getMinutes(), "minutes", Collections.emptyList());

        List<Interval> busyTimes = (getAllBusyTimes(userMail, newEventEntity));
        LOG.debug("Got busyTimes {} for user {}", busyTimes, userMail);

        //3: calculate free-times
        LOG.info("calculate time-slot for meeting for user {}", userMail);
        Collection<Interval> freeTimes =
                calendarService.getFreeTimes(busyTimes.parallelStream(), workingHours.parallelStream(),
                        getBeginOfDay(DateTime.parse(newEventEntity.getAutoTimeDateStart())),
                        getEndOfDay(DateTime.parse(newEventEntity.getAutoTimeDateEnd())));

        long sumOfFreeTimes = freeTimes.parallelStream()
                .mapToLong(value -> value.toDuration().getStandardMinutes())
                .sum();

        LOG.info("sum of freeTimes for {} = {} || need sum of: {}", userMail, sumOfFreeTimes, new Duration(startDateTime, endDateTime).toStandardMinutes().getMinutes());

        if (sumOfFreeTimes < new Duration(startDateTime, endDateTime).toStandardMinutes().getMinutes())
            return 0;
        if (sumOfFreeTimes < new Duration(startDateTime, endDateTime).toStandardMinutes().getMinutes() / 2)
            return 50;
        */

        // TODO: 2019-02-05 better calculations
        final List<EventEntity> allEventsForUser = getAllEventsForUser(userMail, startDateTime.getMillis(), endDateTime.getMillis());
        double duration = allEventsForUser.parallelStream()
                .mapToDouble(eventEntity -> {
                    assert eventEntity.getEndTime() != null && eventEntity.getStartTime() != null;
                    final double dur = eventEntity.getEndTime().getMillis() - eventEntity.getStartTime().getMillis();
                    LOG.info("{} has a duration of {} --- start: {} || end: {}", eventEntity.getSubject(),
                            new Duration(eventEntity.getStartTime().getMillis(), eventEntity.getEndTime().getMillis()),
                            eventEntity.getStartTime(), eventEntity.getEndTime());
                    LOG.info("calculating duration of {} ({})", eventEntity, dur);
                    return dur;
                })
                .sum();
        final long result = (long) Math.max(0, 100 - (duration / (endDateTime.getMillis() - startDateTime.getMillis()) * 100));
        LOG.info("calculating with {} and {} - result: {}", duration, (endDateTime.getMillis() - startDateTime.getMillis()), result);
        return result;
    }

    private class UserTimeEntity {
        private final String userMail;

        UserTimeEntity(String userMail) {
            LOG.debug("Creating new userTimeEntity for user with mail {}", userMail);
            this.userMail = userMail;
        }

        UserTimeEntity(Integer userId) {
            this(userService.getAppUser(userId).getMail());
        }

        Collection<Interval> getFreeTimesForEvent(NewEventEntity newEventEntity) {
            //2: get workingHours & busyTimes
            List<LoginHoursEntity> workingHours = userService.getWorkingHours(userMail);
            LOG.debug("Got workingHours {} for user {}", workingHours, userMail);

            List<Interval> busyTimes = (getAllBusyTimes(userMail, newEventEntity));
            LOG.debug("Got busyTimes {} for user {}", busyTimes, userMail);

            //3: calculate free-times
            LOG.info("calculate time-slot for meeting for user {}", userMail);

            Collection<Interval> freeTimes =
                    timeCalculationService.getFreeTimes(busyTimes.parallelStream(), workingHours.parallelStream(),
                            getBeginOfDay(DateTime.parse(newEventEntity.getAutoTimeDateStart())),
                            getEndOfDay(DateTime.parse(newEventEntity.getAutoTimeDateEnd())));
            LOG.debug("got freeTimes {} for {}", freeTimes, userMail);

            return freeTimes;
        }

        @Override
        public String toString() {
            return format("(userMail: {0})", userMail);
        }
    }
}
