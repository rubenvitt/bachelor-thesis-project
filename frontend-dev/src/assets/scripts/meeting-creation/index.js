import * as URLS from "../constants/urls";
import * as cookie from "../cookie";
import * as calendar from '../calendar';
import * as checkboxController from "../checkbox-in-list-with-badge";
import * as formSender from './form-sending-handler';
import * as localStorage from '../localStorage';

function displayActiveCalendarsInModal(calendars) {
    function getListItemFor(id, name) {
        return `
<label class="list-group-item list-group-item-action d-flex justify-content-between align-items-center">
    <input data-content="${id}" type="checkbox" class="form-check-input">
    ${name}
    <i class="fa fa-check badge badge-primary badge-pill"></i>
</label>`;
    }

    function getHtmlFromCalendarEntities(calendars) {
        let html = '';
        calendars.forEach(item => html += getListItemFor(item.id, item.name));
        return html;
    }

    function getSelectedItem() {
        return list.children().children('input:checked').attr('data-content');
    }

    const list = $('#meeting-creation-dialog-list-group-google-cal-list');
    list.html(getHtmlFromCalendarEntities(calendars));
    const inputList = list.children().children('input');
    checkboxController.initRadioList(inputList, function (checked, id) {
        console.log("DEBUG: " + checked + " --- " + id);
    });

    $('#calendar-select-modal-yes').click(function () {
         formSender.sendForm(getSelectedItem());
    });
}

if (document.getElementById("newMeeting-chooseMeetingType")) {
    $('#meeting-creation-intelligent-duration-time').find('a').click((evt) => {
        const targetLink = $(evt.target);
        $('#meeting-creation-intelligent-duration-btn').text(targetLink.text())
    });

    $('#meeting-creation-submitButton').click(function () {
        $("#calendar-select-modal").modal();
        calendar.getAllActiveCalendars('google', displayActiveCalendarsInModal);
        //form.sendForm();
    });

    $('#meeting-creation-time-manual-btn').click(function () {
        $('#meeting-creation-automatic-box').addClass("d-none");
        $('#meeting-creation-manual-box').removeClass("d-none");
        $('#meeting-creation-time-intelligent-btn').removeClass("active");
        $(this).addClass("active");
    });
    $('#meeting-creation-time-intelligent-btn').click(function () {
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
        console.log("Created content");
        console.log(html);
        $('#meeting-creation-equipment-select').html(html).selectpicker('refresh');
        $('#meeting-creation-equipment-select').on('changed.bs.select', function (e, clickedIndex, isSelected, previousValue) {
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

    const attendeesLinks = $("#newMeeting-attendee-list").find("a");
    attendeesLinks.click(function () {
        $(this).toggleClass("active");
    });

    const attendeeSelectedLinks = $("#newMeeting-attendee-selected-list").find("a");
    attendeeSelectedLinks.click(function () {
        $(this).remove();
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
            });
            $(this).on('changeDate', () => {
                $(this).datepicker('hide');
                $("input:nth-of-type(2)", $(this).parent()).focus();
            });
        });

        $('.input-daterange').datepicker({});
        //$('.input-daterange input').datepicker().on('dateChanged', () => alert("test"));
    });
}