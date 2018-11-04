const weekday = new Array(7);
weekday[0] = "Sunday";
weekday[1] = "Monday";
weekday[2] = "Tuesday";
weekday[3] = "Wednesday";
weekday[4] = "Thursday";
weekday[5] = "Friday";
weekday[6] = "Saturday";

if ($('dashboard-todayMeetings-dayDate') !== undefined) {
    setInterval(function () {
        const now = new Date();
        $('#dashboard-todayMeetings-dayName').text(weekday[now.getDay()]);
        $('#dashboard-todayMeetings-dayDate').text(now.getFullYear() + "-"
            + (now.getMonth() < 10 ? ("0" + now.getMonth()) : now.getMonth()) + "-"
            + (now.getDate() < 10 ? ("0" + now.getDate()) : now.getDate()));
        $('#dashboard-todayMeetings-time').text(`${now.getHours()}:${now.getMinutes()}`);
    }, 2000);
}