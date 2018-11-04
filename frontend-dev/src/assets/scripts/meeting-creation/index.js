$('#new-meeting-dd-intelligentTimeDuration').find('a').click((evt) => {
    const targetLink = $(evt.target);
    $('#new-meeting-dd-intelligentTimeDuration-btn').text(targetLink.text())
});

$(function() {
    $('input[name="daterange"]').daterangepicker({
        opens: 'left'
    }, function(start, end, label) {
        console.log("A new date selection was made: " + start.format('YYYY-MM-DD') + ' to ' + end.format('YYYY-MM-DD'));
    });
});