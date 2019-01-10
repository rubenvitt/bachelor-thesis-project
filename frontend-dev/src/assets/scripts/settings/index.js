import * as checkboxController from '../checkbox-in-list-with-badge'
import * as URLS from '../constants/urls'
import * as cookie from '../cookie'
import * as calendar from '../calendar'

if (document.getElementById("settings-workingHours")) {
    //this page is a settings-page
    $(".workingday").find("button").click(function () {
        $(this).toggleClass("btn-success");
        $(this).toggleClass("btn-outline-primary")
    });

    $("#provider-google").hover(function () {
        $(this).attr('src', 'assets/static/images/google-signin-pressed.png')
    }, function () {
        $(this).attr('src', 'assets/static/images/google-signin-normal.png');
    });

    $("#account-settings-remove-microsoft-access-token-btn").click(function () {
        $("#removal-name").text("Office 365");
        addModalListener("microsoft-access-key");
        $("#are-you-sure-modal").modal();
    });

    $("#account-settings-remove-google-access-token-btn").click(function () {
        $("#removal-name").text("Google");
        addModalListener("google-access-key");
        $("#are-you-sure-modal").modal();
    });
    calendar.getAllCalendars('office', showOfficeCalendars);
    calendar.getAllCalendars('google', showGoogleCalendars);

    $('#office-provider-link').click(function () {
        window.location = `${URLS.apiUrl}/auth-office?user_id=${cookie.getUserID()}`;
    });

    $('#google-provider-link').click(function () {
        window.location = `${URLS.apiUrl}/auth-google?user_id=${cookie.getUserID()}`;
    });
}

//TODO cleanup
function addModalListener(cookie) {
    /*$("#are-you-sure-modal .btn-danger").click(function () {
        console.log("removing cookie: " + cookie);
        removeCookie(cookie);
        $("#are-you-sure-modal").modal('hide');
        $(this).prop("onclick", null).off("click");
    });*/
}

/**
 * @typedef {Object} calendarEntity
 * @property {string} name          name of the calendar
 * @property {number} id            id of the calendar
 * @property {boolean} activated    is calendar activated?
 */

/**
 * handler for office calendars
 * @param {calendarEntity[]} calendars
 */
function showOfficeCalendars(calendars) {
    const list = $('#settings-list-group-office-cal-list');
    list.html(getHtmlFromCalendarEntities(calendars));
    const inputList = list.children().children('input');
    checkboxController.initCheckboxList(inputList, function (checked) {
        console.log(checked);
    });
}

/**
 * handler for google calendars
 * @param {calendarEntity[]} calendars
 */
function showGoogleCalendars(calendars) {
    const list = $('#settings-list-group-google-cal-list');
    list.html(getHtmlFromCalendarEntities(calendars));
    const inputList = list.children().children('input');
    checkboxController.initCheckboxList(inputList, function (checked, id) {
        console.log("AJAX: " + checked + " --- " + id);
        $.ajax({
            url: `${URLS.apiUrl}/calendar/activate?${jQuery.param({
                "calendar_id": id,
                "user_id": cookie.getUserID(),
                "activated": checked
            })}`,
            method: 'post'
            // data: {
            //     calendar_id: id,
            //     user_id: cookie.getUserID(),
            //     activated: checked
            // }
        }).done(function () {

        })
    });
}

/**
 * create a html list
 * @param {calendarEntity[]} calendars
 * @return {string}
 */
function getHtmlFromCalendarEntities(calendars) {
    let html = '';
    calendars.forEach(item => html += getListItemFor(item.id, item.name, item.activated));
    return html;
}

function getListItemFor(id, name, activated) {
    return `
<label class="list-group-item list-group-item-action d-flex justify-content-between align-items-center">
    <input data-content="${id}" type="checkbox" ${activated ? "checked" : ""} class="form-check-input">
    ${name}
    <i class="fa fa-check badge badge-primary badge-pill"></i>
</label>`;
}