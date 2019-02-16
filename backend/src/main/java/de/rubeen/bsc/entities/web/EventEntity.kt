package de.rubeen.bsc.entities.web


import org.joda.time.DateTime

class EventEntity {
    var subject: String? = null
    var startTime: DateTime? = null
    var endTime: DateTime? = null

    constructor(subject: String?, startTime: DateTime, endTime: DateTime) {
        this.subject = subject
        this.startTime = startTime
        this.endTime = endTime
    }

    constructor()
}

