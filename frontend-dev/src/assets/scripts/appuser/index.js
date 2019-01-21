import * as URLS from '../constants/urls';
import {getUserID} from '../cookie'

/**
 * get actual logged in user object
 * @param {function} handler
 */
function getActualAppUser(handler) {
    $.ajax({
        url: `${URLS.apiUrl}/user`,
        data: {
            user_id: getUserID()
        }
    }).done(handler);
}


/**
 * @typedef appUser
 * @property {number} id
 * @property {String} name
 * @property {String} mail
 * @property {String} avatar (url)
 */


/**
 * @param {string} search-filter
 * @param {function} handler
 */
function getOtherAppUser(filter, handler) {
    $.ajax({
        url: `${URLS.apiUrl}/user/list`,
        data: {
            filter: filter,
            user_id: getUserID()
        }
    }).done(handler);
}

export {
    getOtherAppUser,
    getActualAppUser
}