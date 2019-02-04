import * as checkboxController from '../checkbox-in-list-with-badge'
import * as URLS from '../constants/urls'
import * as cookie from '../cookie'
import * as calendar from '../calendar'
import * as workingHours from '../workingHours'
import * as clockPicker from '../clockpicker';

if (document.getElementById("settings-workingHours")) {
    //this page is a settings-page
    $("#provider-google").hover(function () {
        $(this).attr('src', 'assets/static/images/google-signin-pressed.png')
    }, function () {
        $(this).attr('src', 'assets/static/images/google-signin-normal.png');
    });

    /*
     * @typedef {Object} workingHours
 * @property {number} id
 * @property {string} startTime
 * @property {string} endTime
 * @property {boolean} monday
 * @property {boolean} tuesday
 * @property {boolean} wednesday
 * @property {boolean} thursday
 * @property {boolean} friday
 * @property {boolean} saturday
 * @property {boolean} sunday
     */
    $('#settings-save-working-hours').click(function () {
        /**
         *
         * @type {workingHour[]}
         */
        const data = [];

        $('#settings-workingHours').find('.input-group').each(function () {
            const buttons = $(this).find('.workingday').children('.btn');

            const id = $(this).attr('itemid') === 'undefined' ? null : $(this).attr('itemid');
            const startTime = $(this).find('.clockpicker input').first().val();
            const endTime = $($(this).find('.clockpicker input').get(1)).val();
            const monday = $(buttons.get(0)).hasClass('btn-success');
            const tuesday = $(buttons.get(1)).hasClass('btn-success');
            const wednesday = $(buttons.get(2)).hasClass('btn-success');
            const thursday = $(buttons.get(3)).hasClass('btn-success');
            const friday = $(buttons.get(4)).hasClass('btn-success');
            const saturday = $(buttons.get(5)).hasClass('btn-success');
            const sunday = $(buttons.get(6)).hasClass('btn-success');

            data.push({
                id: id, startTime: startTime, endTime: endTime, monday: monday, tuesday: tuesday,
                wednesday: wednesday, thursday: thursday, friday: friday, saturday: saturday, sunday: sunday
            });
        });
        workingHours.sendWorkingHours(data);
    });

    $('#settings-add-working-hour-row').click(function () {
        const container = $('#settings-workingHours');
        $(container).html(container.html() + createWorkingHourLine({
            startTime: '08:00', endTime: '16:00',
            monday: true, tuesday: true, wednesday: true, thursday: true, friday: true, saturday: false, sunday: false
        }));
        $(".workingday").find("button").click(function () {
            $(this).toggleClass("btn-success");
            $(this).toggleClass("btn-outline-primary")
        });
        clockPicker.createClockPicker($('.clockpicker input'), undefined);
    });

    calendar.getAllCalendars('office', showCalendars);
    calendar.getAllCalendars('google', showCalendars);

    workingHours.getWorkingHours(fillWorkingHours);

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
    list.html(getHtmlFromCalendarEntities(calendars));
    const inputList = list.children().children('input');
    checkboxController.initCheckboxList(inputList, undefined);
}

/**
 * handler for google calendars
 * @param {calendarEntity[]} calendars
 */
function showCalendars(list, calendars) {
    list.html(getHtmlFromCalendarEntities(calendars));
    const inputList = list.children().children('input');
    checkboxController.initCheckboxList(inputList, function (checked, id) {
        $.ajax({
            url: `${URLS.apiUrl}/calendar/activate?${jQuery.param({
                "calendar_id": id,
                "user_id": cookie.getUserID(),
                "activated": checked
            })}`,
            method: 'post'
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

/**
 *
 * @param {workingHour} item
 * @return {string} html element
 */
function createWorkingHourLine(item) {
    return `
<div class="form-group">
    <div class="input-group" itemid="${item.id}">
        <div class="input-group-prepend">
            <span class="input-group-text"><i class="fa fa-clock-o"></i></span>
        </div>
        <div class="clockpicker">
            <input type="text" class="form-control" value="${item.startTime}">
        </div>
        <div class="input-group-prepend">
            <span class="input-group-text">
                <i class="fa fa-hourglass-end"></i>
            </span>
        </div>
        <div class="clockpicker">
            <input type="text" class="form-control" value="${item.endTime}">
        </div>
        <div class="btn-group workingday" role="group">
            <button type="button" class="btn ${item.monday ? 'btn-success' : 'btn-outline-primary'}">Mon</button>
            <button type="button" class="btn ${item.tuesday ? 'btn-success' : 'btn-outline-primary'}">Tue</button>
            <button type="button" class="btn ${item.wednesday ? 'btn-success' : 'btn-outline-primary'}">Wed</button>
            <button type="button" class="btn ${item.thursday ? 'btn-success' : 'btn-outline-primary'}">Thr</button>
            <button type="button" class="btn ${item.friday ? 'btn-success' : 'btn-outline-primary'}">Fri</button>
            <button type="button" class="btn ${item.saturday ? 'btn-success' : 'btn-outline-primary'}">Sat</button>
            <button type="button" class="btn ${item.sunday ? 'btn-success' : 'btn-outline-primary'}">Sun</button>
        </div>
    </div>
</div>`;
}

/**
 *
 * @param {workingHour[]} workingHours
 */
function fillWorkingHours(workingHours) {
    //TODO multiple working hour-definitions
    let html = '';
    workingHours.forEach(item => {
        html += createWorkingHourLine(item);
    });
    $('#settings-workingHours').html(html);
    $(".workingday").find("button").click(function () {
        $(this).toggleClass("btn-success");
        $(this).toggleClass("btn-outline-primary")
    });
    clockPicker.createClockPicker($('.clockpicker input'), undefined);
}