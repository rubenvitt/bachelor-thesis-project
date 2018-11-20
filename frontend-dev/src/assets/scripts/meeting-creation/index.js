$('#new-meeting-dd-intelligentTimeDuration').find('a').click((evt) => {
    const targetLink = $(evt.target);
    $('#new-meeting-dd-intelligentTimeDuration-btn').text(targetLink.text())
});

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