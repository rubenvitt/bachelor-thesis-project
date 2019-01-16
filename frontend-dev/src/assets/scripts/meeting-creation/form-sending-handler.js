import * as urls from '../constants/urls';
import {getUserID} from "../cookie";
import * as localStorage from '../localStorage';
import {getRoomId} from "../localStorage";

function sendForm(calenderID) {
    //manual time settings:
    $.ajax({
            url: `${urls.webappUrl}/api/calendar/events/create?user_id=${getUserID()}&calendar_id=${calenderID}`,
            type: 'POST',
            data:
                JSON.stringify(getFormData()),
            contentType: "application/json"
        }
    ).done(function () {
        console.log("Done creating new appointment");
        window.location = '/finished';
    });
}

/**
 * @typedef {Object} formData
 * @property {string} subject           the subject
 * @property {string} description       the description
 * @property {boolean} autoTime         choose by system?
 * @property {string} manTimeDateStart  start of manual date
 * @property {string} manTimeDateEnd    end of manual date
 * @property {string} manTimeTimeStart  start of manual time
 * @property {string} manTimeTimeEnd    end of manual time
 * @property {boolean} autoRoom         autoRoom enabled
 * @property {string[]} roomValues      values for room (equipments)
 * @property {number} roomId            id for room (manual mode)
 */

/**
 * Create a form-data object from ui-form
 *
 * @return formData object
 *
 */
function getFormData() {
    const result = {};

    result.subject = $("#meeting-creation-subject").val();
    result.description = $("#meeting-creation-description").val();
    result.autoTime = $('#meeting-creation-time-intelligent-btn').hasClass('active');
    result.manTimeDateStart = $("#meeting-creation-manual-date-start").val();
    result.manTimeDateEnd = $("#meeting-creation-manual-date-end").val();
    result.manTimeTimeStart = $("#meeting-creation-manual-time-start").val();
    result.manTimeTimeEnd = $("#meeting-creation-manual-time-end").val();

    result.autoRoom = $('#meeting-creation-room-automatic-btn').hasClass('active');
    result.roomValues = $('#meeting-creation-equipment-select').val();
    result.roomId = getRoomId();
    return result;
}

export {
    sendForm,
    getFormData
}