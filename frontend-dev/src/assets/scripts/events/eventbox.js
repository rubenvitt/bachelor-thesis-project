import * as $ from 'jquery';
import * as url from '../constants/urls'
import * as cookie from '../cookie'

const listGroup = $('#dashboard-todayMeetings-listGroup');
if (listGroup.length === 1) {
    console.log(listGroup);
    $.ajax({
        url: url.apiUrl + "/calendar/events",
        data: {
            user_id: cookie.getUserID()
        }
    }).done(
        /**
         *  Get a list of events
         * @param content list of events
         * @param content[].subject event-subject
         * @param content[].startTime joda-startTime-object
         * @param content[].startTime.millis milliseconds of this date
         * @param content[].startTime.hourOfDay hour of the day
         * @param content[].startTime.minuteOfHour minute of the hour
         * @param content[].endTime joda-endTime-object
         * @param content[].endTime.millis milliseconds of this date
         * @param content[].endTime.hourOfDay hour of the day
         * @param content[].endTime.minuteOfHour minute of the hour
         */
        function (content) {
            let items = "";
            content.forEach(event => {
                let dateStart = new Date(event.startTime.millis);
                let dateEnd = new Date(event.endTime.millis);
                let state = (dateStart < Date.now()
                    ? (dateEnd < Date.now()
                        ? EventState.past
                        : EventState.active)
                    : EventState.upcoming);
                items += generateListItem(
                    event.subject,
                    `${(event.startTime.hourOfDay).pad()}:${(event.startTime.minuteOfHour).pad()}`,
                    `${(event.endTime.hourOfDay).pad()}:${(event.endTime.minuteOfHour).pad()}`,
                    state);
            });
            listGroup.html(items);
        });
}


function generateListItem(subject, from, until, state) {
    return `<button class="btn btn-outline-primary list-group-item list-group-item-action ${state}" type="button">
${from} - ${until}<br>${subject}
</button>`;
}

const EventState = Object.freeze({"upcoming": "", "active": "active", "past": "disabled"});

Number.prototype.pad = function () {
    let s = String(this);
    while (s.length < 2)
        s = "0" + s;
    return s;
};