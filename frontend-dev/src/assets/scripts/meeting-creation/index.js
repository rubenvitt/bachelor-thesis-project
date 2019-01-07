import * as form from './form-sending-handler';
import * as URLS from "../constants/urls";
import * as cookie from "../cookie";

if (document.getElementById("newMeeting-chooseMeetingType")) {
    $('#meeting-creation-intelligent-duration-time').find('a').click((evt) => {
        const targetLink = $(evt.target);
        $('#meeting-creation-intelligent-duration-btn').text(targetLink.text())
    });

    $('#meeting-creation-submitButton').click(function () {
        form.sendForm();
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


    $.ajax({
        url: `${URLS.apiUrl}/rooms/equipments`,
        data: {
            user_id: cookie.getUserID()
        }
    }).done(function (content) {
        let html = '';
        content.forEach(item => {
            html += `<option data-content="${item.id}">${item.name}</option>`
        });
        //TODO not working
        $('#meeting-creation-equipment-select').html(html);
    });

    $('#meeting-creation-equipment-select').html();

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