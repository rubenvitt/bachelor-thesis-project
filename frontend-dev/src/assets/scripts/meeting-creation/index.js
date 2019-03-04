import * as URLS from "../constants/urls";
import * as cookie from "../cookie";
import * as calendar from '../calendar';
import * as checkboxController from "../checkbox-in-list-with-badge";
import * as formSender from './form-sending-handler';
import * as localStorage from '../localStorage';
import * as appUser from '../appuser'
import * as clockPicker from '../clockpicker';

const yesButton = $('#calendar-select-modal-yes');

function displayActiveCalendarsInModal(calendars) {
    function getIconForProvider(provider) {
        switch (provider) {
            case 'google':
                return 'fab fa-google';
            case 'office':
                return 'fab fa-microsoft';
        }
    }

    function getListItemFor(id, name, provider, isDefault) {
        return `
<label class="list-group-item list-group-item-action d-flex justify-content-between align-items-center">
    <i class="${getIconForProvider(provider)} c-black"></i>
    <input data-content="${id}" type="checkbox" ${isDefault ? 'checked="checked"' : ''} class="form-check-input">
    ${name}
    <i class="fa fa-check badge badge-primary badge-pill"></i>
</label>`;
    }

    /**
     * get listItems from calendarEntities
     * @param calendars[] list of calendars
     * @param calendars[].id id of calendar
     * @param calendars[].name id of calendar
     * @param calendars[].provider cal-provider for calender
     * @returns {string} html to insert
     */
    function getHtmlFromCalendarEntities(calendars) {
        let html = ``;
        calendars.forEach(item => html += getListItemFor(item.id, item.name, item.provider, item.default));
        return html;
    }

    function getSelectedItem() {
        return list.children().children('input[checked="checked"]').attr('data-content');
    }

    const list = $('#meeting-creation-dialog-list-group-cal-list');
    list.html(getHtmlFromCalendarEntities(calendars));

    const inputList = list.children().children('input');
    checkboxController.initRadioList(inputList, function (checked, id) {
        console.log("radio-changed");
        console.log("checked: " + checked);
        console.log("id: " + id);
        yesButton.removeAttr('disabled');
        yesButton.removeClass('btn-danger btn-success');

        yesButton.text('Choose calendar');
    });

    yesButton.removeClass('btn-danger btn-success');
    yesButton.removeAttr('disabled');
    yesButton.text('Select calendar');

    yesButton.click(function () {
        yesButton.attr('disabled', 'true');
        formSender.sendForm(getSelectedItem(), $(this));
        return false;
    });
}

function getAppUsers(filter) {
    appUser.insertAppUsers(filter, $('#newMeeting-attendee-list'));
}

function refreshAppUsers(filter) {
    getAppUsers(filter.val());
}

if (document.getElementById("newMeeting-chooseMeetingType")) {
    const autoDateBtn = $('#meeting-creation-time-intelligent-btn'),
        autoDateStart = $('#meeting-creation-auto-date-start'),
        autoDateEnd = $('#meeting-creation-auto-date-end'),
        manualDateStart = $('#meeting-creation-manual-date-start'),
        manualDateEnd = $('#meeting-creation-manual-date-end'),
        manualTimeStart = $('#meeting-creation-manual-time-start'),
        manualTimeEnd = $('#meeting-creation-manual-time-end'),
        filter = $('#attendee-filter');

    $('#meeting-creation-intelligent-duration-time').find('a').click((evt) => {
        const targetLink = $(evt.target);
        $('#meeting-creation-intelligent-duration-btn').text(targetLink.text())
    });

    $('#meeting-creation-submitButton').click(function () {
        yesButton.attr('disabled', true);
        yesButton.html('<i class="fa fa-spinner fa-spin mR-10"></i>Loading calendar');
        $("#calendar-select-modal").modal();
        calendar.getAllActiveCalendars(displayActiveCalendarsInModal);
        //form.sendForm();
    });

    $('#meeting-creation-time-manual-btn').click(function () {
        $('#meeting-creation-automatic-box').addClass("d-none");
        $('#meeting-creation-manual-box').removeClass("d-none");
        autoDateBtn.removeClass("active");
        $(this).addClass("active");
    });
    autoDateBtn.click(function () {
        $('#meeting-creation-manual-box').addClass("d-none");
        $('#meeting-creation-automatic-box').removeClass("d-none");
        $('#meeting-creation-time-manual-btn').removeClass("active");
        $(this).addClass("active");
    });

    //room tabs
    $('#meeting-creation-room-automatic-btn').click(function () {
        $('#meeting-creation-room-automatic-box').addClass('d-none');
        $('#meeting-creation-room-manual-box').removeClass('d-none');
        $('#meeting-creation-room-manual-btn').removeClass('active');
        $(this).addClass('active');
    });
    $('#meeting-creation-room-manual-btn').click(function () {
        $('#meeting-creation-room-manual-box').addClass('d-none');
        $('#meeting-creation-room-automatic-box').removeClass('d-none');
        $('#meeting-creation-room-automatic-btn').removeClass('active');
        $(this).addClass('active');
    });

    //attendees
    getAppUsers(undefined);

    filter.change(() => refreshAppUsers(filter));
    autoDateStart.change(() => refreshAppUsers(filter));
    autoDateEnd.change(() => refreshAppUsers(filter));
    manualDateStart.change(() => refreshAppUsers(filter));
    manualDateEnd.change(() => refreshAppUsers(filter));
    manualTimeStart.change(() => refreshAppUsers(filter));
    manualTimeEnd.change(() => refreshAppUsers(filter));

    $.ajax({
        url: `${URLS.apiUrl}/rooms/equipments`,
        data: {
            user_id: cookie.getUserID()
        }
    }).done(function (content) {
        let html = '';
        content.forEach(item => {
            html += `<option itemid="${item.id}">${item.name}</option>`
        });
        const equipmentSelect = $('#meeting-creation-equipment-select');
        equipmentSelect.html(html).selectpicker('refresh');
        equipmentSelect.on('changed.bs.select', function (e, clickedIndex, isSelected, previousValue) {
            console.log(formSender.getFormData());
        });
    });

    $.ajax({
        url: `${URLS.apiUrl}/rooms/all`,
        data: {
            user_id: cookie.getUserID()
        }
    }).done(
        /**
         * Set rooms to selectPicker
         * @param content html json content
         * @param content[].id id of the room
         * @param content[].name name of the room
         * @param content[].size size of the room (place for x people)
         */
        function (content) {
            let html = '';
            let equipments = '';
            content.forEach(item => html += `<option title="${item.name}" itemId="${item.id}">${item.name} (${item.size} people)</option>`);
            $('#meeting-creation-manual-room-select').html(html).selectpicker('refresh');

            $('#meeting-creation-actual-room-equipList').html(equipments);
        }
    );

    $('#meeting-creation-manual-room-select').on('changed.bs.select', function (e, clickedIndex, isSelected, previousValue) {
        const roomId = $(this.selectedOptions.item(0)).attr('itemId');
        localStorage.setRoomId(roomId);
        //get equipments for this room
        $.ajax({
            url: `${URLS.apiUrl}/rooms/equipments`,
            data: {
                user_id: cookie.getUserID(),
                room_id: roomId
            }
        }).done(function (content) {
            let html = '';
            content.forEach(item => html += `<li>${item.name}</li>`);
            $('#meeting-creation-actual-room-equipList').html(html);
        });
    });

    const meetingTypeLinks = $("#newMeeting-chooseMeetingType").find("a");
    meetingTypeLinks.click((evt) => {
        meetingTypeLinks.removeClass("active");
        $(evt.target).addClass("active");
        const settingBoxes = [$("#newMeeting-settingsBox-regular"), $("#newMeeting-settingsBox-phone"),
            $("#newMeeting-settingsBox-online"), $("#newMeeting-settingsBox-other")];
        for (let i = 0; i < meetingTypeLinks.length; i++) {
            if (meetingTypeLinks.eq(i).text() === $(evt.target).text())
                settingBoxes[i].removeClass("d-none");
            else
                settingBoxes[i].addClass("d-none");
        }
    });

    /*attendeesLinks.click((evt) => {
        console.log("A");
       $(evt.target).toggleClass("active");
    });*/

    //Date-Picker
    $(function () {
        $('.input-daterange input').each(function () {
            $(this).datepicker({
                calendarWeeks: true,
                assumeNearbyYear: true,
                todayHighlight: true,
                title: 'Select range for new meeting',
                weekStart: 1,
                startDate: '-0d'
            });
            $(this).on('changeDate', () => {
                $(this).datepicker('hide');
                $("input:nth-of-type(2)", $(this).parent()).focus();
            });
        });

        $('.input-daterange').datepicker({});
        //$('.input-daterange input').datepicker().on('dateChanged', () => alert("test"));
    });

    //clock-picker
    clockPicker.createClockPicker(manualTimeStart, function (value) {
        clockPicker.fillInputWithTime(manualTimeEnd, value, 15);
    });
    clockPicker.createClockPicker(manualTimeEnd, function () {
    });
}
