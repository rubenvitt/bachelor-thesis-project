import * as urls from '../constants/urls';
import {getUserID} from "../cookie";
import {getRoomId} from '../localStorage';

function sendForm(calenderID, button) {
    button.text('Creating event...');
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
        button.text("Finished!");
        button.addClass('btn-success');
    }).fail(function (message) {
        console.error(message);
        button.text("Error");
        button.addClass('btn-danger');
    });
}

/**
 * @typedef {Object} formData
 * @property {string} subject           the subject
 * @property {string} description       the description
 * @property {boolean} autoTime         choose by system?
 * @property {string} autoTimeDateStart start of auto date
 * @property {string} autoTimeDateStart start of auto time
 * @property {number} meetingDuration   duration of an auto-time meeting
 * @property {string} durationUnit      unit of auto-time meetingDuration
 * @property {string} manTimeDateStart  start of manual date
 * @property {string} manTimeDateEnd    end of manual date
 * @property {string} manTimeTimeStart  start of manual time
 * @property {string} manTimeTimeEnd    end of manual time
 * @property {boolean} autoRoom         autoRoom enabled
 * @property {string[]} roomValues      values for room (equipments)
 * @property {number} roomId            id for room (manual mode)
 * @property {number[]} attendees       attendees of this meeting (id's for them)
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

    result.autoTimeDateStart = $('#meeting-creation-auto-date-start').val();
    result.autoTimeDateEnd = $('#meeting-creation-auto-date-end').val();
    result.meetingDuration = $('#meeting-creation-intelligent-duration').val();
    result.durationUnit = $('#meeting-creation-intelligent-duration-btn').text();

    result.manTimeDateStart = $("#meeting-creation-manual-date-start").val();
    result.manTimeDateEnd = $("#meeting-creation-manual-date-end").val();
    result.manTimeTimeStart = $("#meeting-creation-manual-time-start").val();
    result.manTimeTimeEnd = $("#meeting-creation-manual-time-end").val();

    result.autoRoom = $('#meeting-creation-room-automatic-btn').hasClass('active');
    result.roomValues = $('#meeting-creation-equipment-select').val();
    result.roomId = getRoomId();
    result.attendees = [];
    $('#newMeeting-attendee-list').children('a.active').each(function () {
        result.attendees.push($(this).attr('itemid'));
    });
    return result;
}

export {
    sendForm,
    getFormData
}