package de.rubeen.bsc.entities.web

import com.google.api.services.calendar.model.CalendarListEntry
import de.rubeen.bsc.entities.provider.CalendarEvent
import de.rubeen.bsc.provider.office365.entities.Calendar

class CalendarWebEventEntity {
    var creator: CalendarEvent.Attendee? = null
    var subject: String? = null
    var description: String? = null
    var room: String? = null
    var calendarID: String? = null
    var startDate: Long? = null
    var endDate: Long? = null
    var attendees: List<CalendarEvent.Attendee>? = null

    constructor()

    constructor(creator: CalendarEvent.Attendee?, subject: String?, description: String?, room: String?, calendarID: String?, startDate: Long?, endDate: Long?, attendees: List<CalendarEvent.Attendee>?) {
        this.creator = creator
        this.subject = subject
        this.description = description
        this.room = room
        this.calendarID = calendarID
        this.startDate = startDate
        this.endDate = endDate
        this.attendees = attendees
    }

    constructor(calendarEvent: CalendarEvent) {
        this.creator = calendarEvent.creator
        this.subject = calendarEvent.subject
        this.description = calendarEvent.description
        this.room = calendarEvent.room
        this.calendarID = calendarEvent.calendarId
        this.attendees = calendarEvent.attendees
        this.startDate = calendarEvent.startDateTime.millis
        this.endDate = calendarEvent.endDateTime.millis
    }


    override fun toString(): String {
        return "CalendarWebEventEntity {creator: $creator, subject: $subject, description: $description, room: $room, calendarID: $calendarID, startTime: $startDate, endTime: $endDate, attendees: $attendees}"
    }
}
