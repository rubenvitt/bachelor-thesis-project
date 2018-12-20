import * as urls from '../constants/urls';

function sendForm() {
    const subject = $('#meeting-creation-subject').val();
    const description = $('#meeting-creation-description').val();
    const autoTime = !$('#meeting-creation-time-manual-btn').hasClass('active');
    const manTimeDateStart = $('#meeting-creation-manual-date-start').val();
    const manTimeDateEnd = $('#meeting-creation-manual-date-end').val();
    const manTimeTimeStart = $('#meeting-creation-manual-time-start').val();
    const manTimeTimeEnd = $('#meeting-creation-manual-time-end').val();

    if (!autoTime) {
        //manual time settings:
        $.ajax({
                url: urls.webappUrl + "/api/calendar/events/create",
                type: 'POST',
                data:
                    JSON.stringify({
                        subject: subject,
                        description: description,
                        autoTime: autoTime,
                        manTimeDateStart: new Date(manTimeDateStart).toJSON(),
                        manTimeDateEnd: new Date(manTimeDateEnd).toJSON(),
                        manTimeTimeStart: manTimeTimeStart,
                        manTimeTimeEnd: manTimeTimeEnd
                    }),
                contentType: "application/json"
            }
        )

            .done(function () {
                console.log("Done creating new appointment");
            });
    } else {
        //automatic time settings:
    }
}

export {
    sendForm
}