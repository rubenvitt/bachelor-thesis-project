import * as cookie from '../cookie';
import * as $ from 'jquery'
import * as URLS from "../constants/urls";


/**
 * @typedef {Object} workingHour
 * @property {string} startTime
 * @property {string} endTime
 */

/**
 * normalize the time of an item
 * @param {workingHour} item
 * @return {workingHour} item
 */
function normalizeTime(item) {
    item.startTime = item.startTime.substr(0, 5);
    item.endTime = item.endTime.substr(0, 5);
    return item;
}

/**
 *
 * @param {function} handler
 */
function getWorkingHours(handler) {
    $.ajax({
        url: `${URLS.apiUrl}/user/workingHours?user_id=${cookie.getUserID()}`
    }).done(function (content) {
        console.log(content);
        content.forEach(item => normalizeTime(item));
        handler(content);
    });
}

export {
    getWorkingHours
}