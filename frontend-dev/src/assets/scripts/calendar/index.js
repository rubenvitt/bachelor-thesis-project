import $ from 'jquery';
import * as url from '../constants/urls'
import * as cookie from "../cookie";

function getEventsForCalendar(calendar) {
    $.ajax({
        url: url.apiUrl,
        data: JSON.stringify({user_id: "", calendar_id: ""})
    })
}

/**
 * @typedef {Object} calendarEntity
 * @property {string} name          name of the calendar
 * @property {number} id            id of the calendar
 * @property {boolean} activated    is calendar activated?
 */


/**
 * Get a list of all calenders and then call a function
 * @param {string} provider
 * @param {function} handler function to call after downloading, accepting calendarEntity[]
 */
function getAllCalendars(provider, handler) {
    switch (provider) {
        case 'google':
            $.ajax({
                url: `${url.apiUrl}/google/calendar`,
                data: {
                    user_id: cookie.getUserID()
                }
            }).done(function (content) {
                handler(mapCalendarEntities(content));
            });
            break;
        case 'office':
            //some demo data:
            const entities = [
                {name: "Microsoft Nummer 1", id: 1, activated: false},
                {name: "Microsoft Nummer 2", id: 2, activated: false},
                {name: "Microsoft Nummer 3", id: 3, activated: false},
                {name: "Microsoft Nummer 4", id: 4, activated: false}
            ];
            handler(entities);
            break;
    }
}

/**
 * Get a list of all active calenders and then call a function
 * @param {string} provider
 * @param {function} handler function to call after downloading, accepting calendarEntity[]
 */
function getAllActiveCalendars(provider, handler) {
    switch (provider) {
        case 'google':
            $.ajax({
                url: `${url.apiUrl}/google/calendar/active`,
                data: {
                    user_id: cookie.getUserID()
                }
            }).done(function (content) {
                handler(mapCalendarEntities(content));
            });
            break;
        case 'office':
            //some demo data:
            const entities = [
                {name: "Dummy-Calendar", id: 1, activated: true},
            ];
            handler(entities);
            break;
    }
}

/**
 *
 * @param content array of calendars
 * @param content[].calendarID id of calendar
 * @param content[].calendarName name of calendar
 * @param content[].activated activated-state of calendar
 */
function mapCalendarEntities(content) {
    console.log(content);
    return content.map(item => ({
        name: item.calendarName,
        id: item.calendarID,
        activated: item.activated
    }));
}

export {
    getAllCalendars,
    getAllActiveCalendars
};