import * as urls from '../constants/urls';
import {getUserID} from "../cookie";

function sendForm() {
    //manual time settings:
    $.ajax({
            url: `${urls.webappUrl}/api/calendar/events/create?user_id=${getUserID()}`,
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

    return result;
}

export {
    sendForm
}