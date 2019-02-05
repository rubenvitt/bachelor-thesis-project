package de.rubeen.bsc.service;

import de.rubeen.bsc.entities.provider.CalendarEvent;
import de.rubeen.bsc.entities.web.EventEntity;
import de.rubeen.bsc.entities.web.LoginHoursEntity;
import de.rubeen.bsc.entities.web.NewEventEntity;
import de.rubeen.bsc.helper.EventComparatorFactory;
import de.rubeen.bsc.service.provider.CalendarProvider;
import de.rubeen.bsc.service.provider.GoogleProviderService;
import org.apache.commons.lang3.NotImplementedException;
import org.joda.time.DateTime;
import org.joda.time.Interval;
import org.joda.time.Period;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.sql.SQLException;
import java.util.Collection;
import java.util.LinkedList;
import java.util.List;
import java.util.Objects;
import java.util.stream.Collectors;

import static de.rubeen.bsc.entities.db.Tables.APPUSER;
import static de.rubeen.bsc.entities.db.Tables.CALENDAR;

@Service
public class EventService extends LoggableService {
    private final GoogleProviderService googleProviderService;
    private final ProviderService providerService;
    private final LoginService loginService;
    private final RoomService roomService;
    private final DatabaseService databaseService;
    private final UserService userService;
    private final CalendarService calendarService;

    @Autowired
    public EventService(GoogleProviderService googleProviderService, ProviderService providerService, LoginService loginService, RoomService roomService, DatabaseService databaseService, UserService userService, CalendarService calendarService) throws SQLException {
        this.googleProviderService = googleProviderService;
        this.providerService = providerService;
        this.loginService = loginService;
        this.roomService = roomService;
        this.databaseService = databaseService;
        this.userService = userService;
        this.calendarService = calendarService;
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
                        return providerService.getCalendarProvider(calendarId).getEventsBetween(new Interval(startMillis, endMillis), userMail, calendarId);
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

    public void addEvent(NewEventEntity newEventEntity, String userMail, String calendarId) throws CalendarProvider.CalendarException {
        CalendarProvider calendarProvider = providerService.getCalendarProvider(calendarId);
        if (newEventEntity.isAutoTime())
            createAutoEvent(newEventEntity, userMail, calendarId, calendarProvider);
        else
            throw new NotImplementedException("Manual events are not implemented yet");
    }

    private void createAutoEvent(final NewEventEntity newEventEntity, final String userMail, final String calendarId,
                                 final CalendarProvider calendarProvider) throws CalendarProvider.CalendarException {
        LOG.info("using calendarProvider: {} to create an event for {} - calendarId: {} - event: {}",
                calendarProvider, userMail, calendarId, newEventEntity);
        //INIT... get ID's
        LOG.info("Initializations...");
        LOG.debug("Get all attendees of {}", newEventEntity);
        List<CalendarEvent.Attendee> attendees = getEventAttendees(newEventEntity.getAttendees());
        LOG.debug("Got {} attendees: {}", attendees.size(), attendees);

        if (newEventEntity.isAutoRoom())
            throw new NotImplementedException("Auto room was not implemented, yet");
        else {
            //#1: get room from provider
            LOG.info("Manual room -> use fix room!");
            LOG.info("#1/4: Getting room...");
            // TODO: 2019-01-28 getRoom-CalendarID & provider to get freeTimes
            String room = roomService.getRoomById(newEventEntity.getRoomId());
            LOG.debug("Got room {} for event {}", room, newEventEntity);
            //2: get workingHours & busyTimes
            LOG.info("#2/4: Get workingHours and busyTimes");
            List<LoginHoursEntity> workingHours = userService.getWorkingHours(userMail);
            LOG.debug("Got workingHours {} for user {}", workingHours, userMail);
            List<Interval> busyTimes;
            busyTimes = (getAllBusyTimes(userMail, newEventEntity));
            LOG.debug("Got busyTimes {} for user {}", busyTimes, userMail);
            //3: calculate free-times
            LOG.info("#3/4: calculate time-slot for meeting");
            Collection<Interval> freeTimes =
                    calendarService.getFreeTimes(busyTimes.parallelStream(), workingHours.parallelStream(),
                            DateTime.parse(newEventEntity.getAutoTimeDateStart()),
                            DateTime.parse(newEventEntity.getAutoTimeDateEnd()));
            LOG.debug("got freeTimes {} for {}", freeTimes, userMail);
            Interval timeSlot = searchTimeSlot(freeTimes, newEventEntity);
            if (timeSlot == null) {
                LOG.error("No timeSlot found for {} - {}", userMail, newEventEntity);
                throw new CalendarProvider.CalendarException("Unable to find timeSlot for event.", null);
            } else
                LOG.debug("got timeSlot {} for {} - {}", timeSlot, userMail, newEventEntity);
            LOG.info("#4/4: create auto-time-manual-room event for {}", userMail);
            CalendarEvent calendarEvent = new CalendarEvent(newEventEntity.getSubject(), newEventEntity.getDescription(),
                    room, calendarId, timeSlot, attendees);
            LOG.debug("Creating event: {}", calendarEvent);
            calendarProvider.createEvent(calendarEvent, userMail);
        }
    }

    private List<Interval> getAllBusyTimes(String userMail, NewEventEntity newEventEntity) {
        return providerService.getAllCalendarEntities(userMail).parallelStream()
                .map(calendarEntity -> {
                    try {
                        return providerService.getCalendarProvider(calendarEntity.getCalendarID()).getBusyTimes(userMail, newEventEntity);
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

    private Interval searchTimeSlot(Collection<Interval> freeTimes, NewEventEntity newEventEntity) {
        final LinkedList<Interval> resultIntervals = new LinkedList<>();
        LOG.info("Looking for time-slots for {} in {}", newEventEntity, freeTimes);
        Period eventPeriod = getMeetingDuration(newEventEntity.getMeetingDuration(), newEventEntity.getDurationUnit());
        freeTimes.forEach(interval -> {
            Period timeSlotPeriod = interval.toPeriod();
            if (eventPeriod.getHours() < timeSlotPeriod.getHours()
                    || (eventPeriod.getHours() == timeSlotPeriod.getHours()
                    && eventPeriod.getMinutes() <= timeSlotPeriod.getMinutes())) {
                LOG.info("found timeSlot: {} ({}) - {} ({})",
                        interval.getStart().toLocalDate(), interval.getStart().toLocalTime(),
                        interval.getEnd().toLocalDate(), interval.getEnd().toLocalTime());
                resultIntervals.add(interval);
            }
        });
        if (resultIntervals.isEmpty())
            return null;
        Interval selectedInterval = selectPeriodForMeeting(resultIntervals, newEventEntity);
        return trimIntervalForMeeting(selectedInterval, eventPeriod);
    }

    private Interval trimIntervalForMeeting(Interval freeInterval, Period eventPeriod) {
        LOG.debug("Trimming interval {} for period of meeting: {}", freeInterval, eventPeriod);
        return new Interval(freeInterval.getStartMillis(), freeInterval.getStart()
                .plus(eventPeriod)
                .getMillis());
    }

    private Interval selectPeriodForMeeting(LinkedList<Interval> resultIntervals, NewEventEntity newEventEntity) {
        // TODO: 2019-01-28 select perfect interval for specific meeting
        //select first... :)
        LOG.debug("Selecting first interval from {} for {}", resultIntervals, newEventEntity);
        return resultIntervals.getFirst();
    }

    private Period getMeetingDuration(Integer meetingDuration, String durationUnit) {
        switch (durationUnit.toLowerCase()) {
            case "minutes":
            case "minute":
            case "min":
                return new Period().withMinutes(meetingDuration);
            case "hours":
            case "hour":
            case "h":
                return new Period().withHours(meetingDuration);
            default:
                throw new IllegalArgumentException("meetingDuration not specified correctly");
        }
    }

    private DateTime getBeginOfWeek(Integer weekNumber) {
        if (weekNumber == null)
            weekNumber = DateTime.now().getWeekOfWeekyear();
        return getBeginOfDay(new DateTime().withWeekOfWeekyear(weekNumber).dayOfWeek().withMinimumValue());
    }

    private DateTime getEndOfWeek(Integer weekNumber) {
        if (weekNumber == null)
            weekNumber = DateTime.now().getWeekOfWeekyear();
        return getEndOfDay(new DateTime().withWeekOfWeekyear(weekNumber).dayOfWeek().withMaximumValue());
    }

    private DateTime getEndOfToday() {
        return getEndOfDay(DateTime.now());
    }

    private DateTime getBeginOfToday() {
        return getBeginOfDay(DateTime.now());
    }

    private DateTime getBeginOfDay(DateTime day) {
        return day.withHourOfDay(0).withMinuteOfHour(0).withSecondOfMinute(0).withMillisOfSecond(0);
    }

    private DateTime getEndOfDay(DateTime day) {
        return day.withHourOfDay(23).withMinuteOfHour(59).withSecondOfMinute(59).withMillisOfSecond(999);
    }
}
