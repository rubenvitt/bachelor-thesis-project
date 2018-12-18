import $ from 'jquery';
import * as url from '../constants/urls'

function getEventsForCalendar(calendar) {
    $.ajax({
        url: url.apiUrl,
        data: JSON.stringify({user_id: "", calendar_id: ""})
    })
}