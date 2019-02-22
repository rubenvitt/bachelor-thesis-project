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
    var default: Boolean = false
        private set

    constructor()

    constructor(calendarListEntry: CalendarListEntry, activated: Boolean, default: Boolean) {
        this.calendarName = calendarListEntry.summary
        this.calendarID = calendarListEntry.id
        this.activated = activated
        this.provider = "google"
        this.default = default
    }

    constructor(calendarName: String, calendarID: String, activated: Boolean, provider: String, default: Boolean) {
        this.calendarName = calendarName
        this.calendarID = calendarID
        this.activated = activated
        this.provider = provider
        this.default = default
    }

    constructor(calendar: Calendar, isActivated: Boolean, default: Boolean) {
        this.calendarName = calendar.name
        this.calendarID = calendar.id
        this.activated = isActivated
        this.provider = "office"
        this.default = default
    }

    override fun toString(): String {
        return "CalendarEntity [id=$calendarID, name=$calendarName, activated=$activated, provider=$provider, default=$default]"
    }
}
