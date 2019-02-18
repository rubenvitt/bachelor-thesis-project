package de.rubeen.bsc.entities.web

import com.google.api.services.calendar.model.CalendarListEntry
import de.rubeen.bsc.provider.office365.entities.Calendar

class CalendarEntity {
    var calendarName: String? = null
        private set
    var calendarID: String? = null
        private set
    var activated: Boolean = false
        private set
    var provider: String? = null
        private set

    constructor()

    constructor(calendarListEntry: CalendarListEntry, activated: Boolean) {
        this.calendarName = calendarListEntry.summary
        this.calendarID = calendarListEntry.id
        this.activated = activated
        this.provider = "google"
    }

    constructor(calendarName: String, calendarID: String, activated: Boolean, provider: String) {
        this.calendarName = calendarName
        this.calendarID = calendarID
        this.activated = activated
        this.provider = provider
    }

    constructor(calendar: Calendar, isActivated: Boolean) {
        this.calendarName = calendar.name
        this.calendarID = calendar.id
        this.activated = isActivated
        this.provider = "office"
    }
}
