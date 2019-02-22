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
                handler($('#settings-list-group-google-cal-list'), mapCalendarEntities(content));
            });
            break;
        case 'office':
            $.ajax({
                url: `${url.apiUrl}/office/calendar`,
                data: {
                    user_id: cookie.getUserID()
                }
            }).done(function (content) {
                console.log(content);
                handler($('#settings-list-group-office-cal-list'), mapCalendarEntities(content));
            });
            break;
    }
}

/**
 * Get a list of all active calenders and then call a function
 * @param {string} provider
 * @param {function} handler function to call after downloading, accepting calendarEntity[]
 */
function getAllActiveCalendars(handler) {
    $.ajax({
        url: `${url.apiUrl}/calendar/active`,
        data: {
            user_id: cookie.getUserID()
        }
    }).done(function (content) {
        handler(mapCalendarEntities(content));
    });
}

/**
 *
 * @param content array of calendars
 * @param content[].calendarID id of calendar
 * @param content[].calendarName name of calendar
 * @param content[].activated activated-state of calendar
 * @param content[].provider cal-provider
 */
function mapCalendarEntities(content) {
    console.log(content);
    return content.map(item => ({
        name: item.calendarName,
        id: item.calendarID,
        activated: item.activated,
        provider: item.provider,
        default: item.default
    }));
}

export {
    getAllCalendars,
    getAllActiveCalendars
};