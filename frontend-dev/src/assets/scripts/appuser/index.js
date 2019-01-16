import * as URLS from '../constants/urls';
import {getUserID} from '../cookie'

/**
 * @typedef appUser
 * @property {number} id
 * @property {String} name
 * @property {String} mail
 * @property {String} avatar (url)
 */


/**
 * @param {function} handler
 */
function getOtherAppUser(handler) {
    $.ajax({
        url: `${URLS.apiUrl}/user/list`,
        data: {
            user_id: getUserID()
        }
    }).done(handler);
}

export {
    getOtherAppUser
}