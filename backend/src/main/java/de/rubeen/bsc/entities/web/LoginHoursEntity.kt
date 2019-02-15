package de.rubeen.bsc.entities.web

import org.joda.time.LocalTime
import java.text.MessageFormat

class LoginHoursEntity {
    var id: Int? = null
    var startTime: String? = null
        get() = if (field!!.length == 5) "${field!!}:00" else field
    var endTime: String? = null
        get() = if (field!!.length == 5) "${field!!}:00" else field
    var monday: Boolean? = null
    var tuesday: Boolean? = null
    var wednesday: Boolean? = null
    var thursday: Boolean? = null
    var friday: Boolean? = null
    var saturday: Boolean? = null
    var sunday: Boolean? = null

    constructor()

    constructor(id: Int?,startTime: String?, endTime: String?, monday: Boolean?, tuesday: Boolean?, wednesday: Boolean?, thursday: Boolean?, friday: Boolean?, saturday: Boolean?, sunday: Boolean?) {
        this.id = id
        this.startTime = startTime
        this.endTime = endTime
        this.monday = monday
        this.tuesday = tuesday
        this.wednesday = wednesday
        this.thursday = thursday
        this.friday = friday
        this.saturday = saturday
        this.sunday = sunday
    }

    override fun toString(): String {
        val start = LocalTime(this.startTime)
        val end = LocalTime(this.endTime)
        val dayString = (if (this.monday!!) "Mon " else "") +
                (if (this.tuesday!!) "Tue " else "") +
                (if (this.wednesday!!) "Wed " else "") +
                (if (this.thursday!!) "Thr " else "") +
                (if (this.friday!!) "Fri " else "") +
                (if (this.saturday!!) "Sat " else "") +
                if (this.sunday!!) "Sun " else ""
        return MessageFormat.format("{0} {1} - {2}", dayString, start, end)
    }
}
