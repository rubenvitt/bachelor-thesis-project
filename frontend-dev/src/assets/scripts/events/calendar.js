import * as $ from "jquery";
import * as urls from "../constants/urls";
import * as cookie from "../cookie";

/**
 *
 * @param jsonEvents
 * @param jsonEvents[].subject
 * @param jsonEvents[].startTime
 * @param jsonEvents[].startTime.millis
 * @param jsonEvents[].endTime
 * @param jsonEvents[].endTime.millis
 */
function fillCalendar(jsonEvents) {
    const events = jsonEvents.map(jsonEvent =>
        new CalendarEvent(
            jsonEvent.subject,
            new Date(jsonEvent.startTime.millis),
            new Date(jsonEvent.endTime.millis),
            "TEST", 'success'));
    $('#full-calendar').fullCalendar({
        events,
        height: 650,
        defaultView: 'agendaWeek',
        eventLimit: 4,
        scrollTime: "07:00:00",
        eventLimitClick: 'day',
        editable: false,
        header: {
            left: 'listWeek,month,agendaWeek,agendaDay',
            center: 'title',
            right: 'today prev,next',
        },
        timeFormat: 'H:mm',
        weekNumbers: true,
        weekNumberCalculation: "ISO",
        weekNumbersWithinDays: true,
    });

    window.dispatchEvent(new Event('load'));
}

class CalendarEvent {
    constructor(title, start, end, desc, bullet, className) {
        this.title = title;
        this.start = start;
        this.end = end;
        this.desc = desc;
        this.bullet = bullet;
        this.className = className;
    }
}

if ($('#full-calendar').length > 0) {
    console.log("Creating full-cal data");
    $.ajax({
        url: urls.apiUrl + "/calendar/events/week",
        data: {user_id: cookie.getUserID()},
    }).done(function (content) {
        fillCalendar(content);
    });
}