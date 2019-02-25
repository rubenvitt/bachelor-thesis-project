import * as colors from '../constants/colors';

global.jQuery = require('jquery');
require('jquery-clock-timepicker');

/**
 * Creates a clockPicker for an input element
 * @param {$()} clockPicker jquery-input
 * @param {function} handler to call when value was changed
 */
function createClockPicker(clockPicker, handler) {
    clockPicker.clockTimePicker({
        precision: 5,
        onChange: handler,
        buttonTextColor: colors.COLORS["deep-orange-500"],
        popupHeaderBackgroundColor: colors.COLORS["deep-orange-500"],
        selectorColor: colors.COLORS["deep-orange-500"],
        required: true,
    });
}

/**
 * Fills an input if it's empty with time + 15 (max 59 min's!)
 * @param {$()} endInput
 * @param {number} value
 * @param {number} offset
 */
function fillInputWithTime(endInput, value, offset) {
    const regexMinutes = /(?<=:)(\d)+/g;
    const regexHours = /(\d)+(?=:)/g;
    let minutesResult = Number.parseInt(regexMinutes.exec(value)[0]);
    let hoursResult = Number.parseInt(regexHours.exec(value)[0]);
    if (endInput.val() === "" || endInput.val() === '00:00')
        endInput.val(`${pad(minutesResult + offset >= 60 ? ++hoursResult : hoursResult)}:${pad((minutesResult = minutesResult + offset) >= 60 ? minutesResult - 60 : minutesResult)}`);

    function pad(num) {
        const s = "0" + num;
        return s.substr(s.length - 2);
    }
}

export {
    createClockPicker,
    fillInputWithTime
}