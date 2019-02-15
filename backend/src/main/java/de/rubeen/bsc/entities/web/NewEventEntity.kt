package de.rubeen.bsc.entities.web

class NewEventEntity {
    var subject: String? = null
    var description: String? = null
    var autoTime: Boolean = false
    var autoRoom: Boolean = false
    var manTimeDateStart: String? = null
    var manTimeDateEnd: String? = null
    var autoTimeDateStart: String? = null
    var autoTimeDateEnd: String? = null
    var meetingDuration: Int? = null
    var durationUnit: String? = null
    var manTimeTimeStart: String? = null
    var manTimeTimeEnd: String? = null
    var roomValues: List<String>? = null
    var roomId: Int? = null
    var attendees: List<Int>? = null

    constructor()

    constructor(subject: String, description: String, autoTime: Boolean, autoRoom: Boolean, autoTimeDateStart: String, autoTimeDateEnd: String, meetingDuration: Int?, durationUnit: String, attendees: List<Int>) {
        this.subject = subject
        this.description = description
        this.autoTime = autoTime
        this.autoRoom = autoRoom
        this.autoTimeDateStart = autoTimeDateStart
        this.autoTimeDateEnd = autoTimeDateEnd
        this.meetingDuration = meetingDuration
        this.durationUnit = durationUnit
        this.attendees = attendees
    }

    constructor(subject: String, description: String, autoTime: Boolean, autoRoom: Boolean,
                manTimeDateStart: String, manTimeDateEnd: String, autoTimeDateStart: String, autoTimeDateEnd: String, meetingDuration: Int?, durationUnit: String, manTimeTimeStart: String, manTimeTimeEnd: String,
                roomValues: List<String>, roomId: Int?, attendees: List<Int>) {
        this.subject = subject
        this.description = description
        this.autoTime = autoTime
        this.autoRoom = autoRoom
        this.manTimeDateStart = manTimeDateStart
        this.manTimeDateEnd = manTimeDateEnd
        this.autoTimeDateStart = autoTimeDateStart
        this.autoTimeDateEnd = autoTimeDateEnd
        this.meetingDuration = meetingDuration
        this.durationUnit = durationUnit
        this.manTimeTimeStart = manTimeTimeStart
        this.manTimeTimeEnd = manTimeTimeEnd
        this.roomValues = roomValues
        this.roomId = roomId
        this.attendees = attendees
    }

    override fun toString(): String {
        return "(subject: $subject, description: $description, autoTime: $autoTime, manTimeDateStart: $manTimeDateStart, manTimeDateEnd: $manTimeDateEnd, manTimeTimeStart: $manTimeTimeStart, manTimeTimeEnd: $manTimeTimeEnd)"
    }
}
