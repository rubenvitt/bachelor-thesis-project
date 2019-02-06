import * as URLS from '../constants/urls';
import {getUserID} from '../cookie'

/**
 * get actual logged in user object
 * @param {function} handler
 */
function getActualAppUser(handler) {
    $.ajax({
        url: `${URLS.apiUrl}/user`,
        data: {
            user_id: getUserID()
        }
    }).done(handler);
}


/**
 * @typedef appUser
 * @property {number} id
 * @property {String} name
 * @property {String} mail
 * @property {String} avatar (url)
 * @property {string} position
 */


/**
 * @param {string} filter for searching users
 * @param {function} handler
 */
function getOtherAppUser(filter, handler) {
    $.ajax({
        url: `${URLS.apiUrl}/user/list`,
        data: {
            filter: filter,
            user_id: getUserID()
        }
    }).done(handler);
}

let attendeeActivatedList = {
    getElement(key) {
        return this[key];
    },

    setElement(key, value) {
        this[key] = value;
    }
};

function insertAppUsers(filter, attendeeList) {
    getOtherAppUser(filter,
        /**
         * @param {appUser[]} appUserList}
         */
        function (appUserList) {
            //fill attendee-list
            const itemsToChangeDirection = 7;
            let html = "";
            appUserList.forEach(user => {
                if (attendeeActivatedList[user.id] === undefined)
                    attendeeActivatedList.setElement(user.id, false);
                html += `
                <a itemid="${user.id}" href="javascript:void {}"
                   class="attendee-item bds-n list-group-item ${appUserList.length >= itemsToChangeDirection ? 'col-lg-6' : ''} list-group-item-action container">
                    <div class="row">
                        <div class="col-3">
                            <img class="w-100p rounded-circle"
                                 src="${user.avatar}">
                        </div>
                        <div class="col-6">
                            <div class="container">
                                <div class="row">
                                    <h6 class="mb-0 user-name">${user.name}</h6>
                                </div>
                                <div class="row">
                                    <small>${user.position}</small>
                                </div>
                            </div>
                        </div>
                        <div class="spinner col-2 ta-c m-a m-0 vis-h"></div>
                    </div>
                </a>`;
            });
            attendeeList.html(html);
            if (appUserList.length <= itemsToChangeDirection) {
                attendeeList.removeClass('row');
            } else {
                attendeeList.addClass('row');
            }
            attendeeList.find("a").click(attendeeClickHandler);
            attendeeList.find("a").each(function () {
                attendeeClickHandler('auto', $(this));
            })
        });

    function attendeeClickHandler(state, uiElement) {
        let attendee;
        let userId;
        let userActivated;
        if (state === 'auto') {
            attendee = uiElement;
            userId = attendee.attr('itemid');
            userActivated = attendeeActivatedList.getElement(userId);
        } else {
            attendee = $(this);
            userId = attendee.attr('itemid');
            userActivated = !attendeeActivatedList.getElement(userId);
            attendeeActivatedList.setElement(userId, userActivated);
        }

        //if new state is activated, get information about user-quality at this date...
        if (userActivated) {
            //make spinner visible
            attendee.find('.spinner').removeClass('vis-h');
            attendee.find('.spinner').addClass('vis-v');

            const isAuto = $('#meeting-creation-time-intelligent-btn').hasClass('active');
            const dateStart = isAuto ? $('#meeting-creation-auto-date-start').val() : $('#meeting-creation-manual-date-start').val(),
                dateEnd = isAuto ? $('#meeting-creation-auto-date-end').val() : $('#meeting-creation-manual-date-end').val(),
                timeStart = isAuto ? '00:00' : $('#meeting-creation-manual-time-start').val(),
                timeEnd = isAuto ? '23:59' : $('#meeting-creation-manual-time-end').val();

            $.ajax({
                url: URLS.apiUrl + "/calendar/events/user_quality",
                data: {
                    user_id: attendee.attr('itemid'),
                    start_date: dateStart,
                    start_time: timeStart,
                    end_date: dateEnd,
                    end_time: timeEnd
                }
            }).done(function (content) {
                //maybe the user has clicked twice to deactivate, before ajax was completed...
                if (userActivated !== attendeeActivatedList.getElement(userId)) {
                    return;
                }
                console.log(`quality-value: ${content}`);
                switch (true) {
                    case (content <= 10):
                        attendee.addClass('bg-danger');
                        break;
                    case (content < 50):
                        attendee.addClass('bg-warning');
                        break;
                    case (content >= 50):
                        attendee.addClass('bg-success');
                        break;
                    default:
                        console.error("got wrong content");
                }
                attendee.find('.spinner').addClass('vis-h');
                attendee.find('.spinner').removeClass('vis-v');
            });
        } else {
            //deactivate user: give them normal behaviour...
            attendee.removeClass('bg-danger');
            attendee.removeClass('bg-warning');
            attendee.removeClass('bg-success');
            attendee.removeClass('active');
            attendee.find('.spinner').addClass('vis-h');
            attendee.find('.spinner').removeClass('vis-v');
        }
    }
}

export {
    getActualAppUser,
    insertAppUsers
}