import * as checkboxController from '../checkbox-in-list-with-badge'
/*
 <form id="settings-workingHours">
 <div class="form-group">
 <label>Working hours</label>
 <div class="input-group">
 <input type="time" class="form-control"
 placeholder="8:00">
 until
 <input type="time" class="form-control"
 placeholder="12:30">
 <div class="btn-group" role="group">
 <button type="button" class="btn btn-outline-primary">Mon</button>
 <button type="button" class="btn btn-outline-primary">Tue</button>
 <button type="button" class="btn btn-outline-primary">Wed</button>
 <button type="button" class="btn btn-outline-primary">Thr</button>
 <button type="button" class="btn btn-outline-primary">Fri</button>
 <button type="button" class="btn btn-outline-primary">Sat</button>
 <button type="button" class="btn btn-outline-primary">Sun</button>
 </div>
 </div>
 <small class="form-text text-muted">
 How much time, do you think, your meeting will take?
 </small>
 </div>

 */

if (document.getElementById("settings-workingHours")) {
    //this page is a settings-page
    /*$("button").click(function () {
        alert($(this).html());
    });*/
    $(".workingday").find("button").click(function () {
        $(this).toggleClass("btn-success");
        $(this).toggleClass("btn-outline-primary")
    });

    $("#provider-google").hover(function () {
        $(this).attr('src', 'assets/static/images/google-signin-pressed.png')
    }, function () {
        $(this).attr('src', 'assets/static/images/google-signin-normal.png');
    });

    $("#account-settings-remove-microsoft-access-token-btn").click(function () {
        $("#removal-name").text("Office 365");
        addModalListener("microsoft-access-key");
        $("#are-you-sure-modal").modal();
    });

    $("#account-settings-remove-google-access-token-btn").click(function () {
        $("#removal-name").text("Google");
        addModalListener("google-access-key");
        $("#are-you-sure-modal").modal();
    });
    loadAccessKeyBoxes();
    loadCalendars($('#microsoft-access-token').val(), $('#google-access-token').val())
}

function addModalListener(cookie) {
    $("#are-you-sure-modal .btn-danger").click(function () {
        console.log("removing cookie: " + cookie);
        removeCookie(cookie);
        $("#are-you-sure-modal").modal('hide');
        $(this).prop("onclick", null).off("click");
    });
}

function loadCalendars(microsoft, google) {
    if (microsoft) {
        let string = "";
        string += getListItemFor(1, "Microsoft Nummer 1");
        string += getListItemFor(2, "Microsoft Nummer 2");
        string += getListItemFor(3, "Microsoft Nummer 3");
        string += getListItemFor(4, "Microsoft Nummer 4");
        const list = $('#settings-list-group-office-cal-list');
        //list.html(string);
        const inputList = list.children().children("input");
        checkboxController.initCheckboxList(inputList);
    }
    if (google) {
        let string = "";
        $.ajax({
            url: "http://localhost:8080/google/calendar",
            data: {
                access_token: google
            }
        }).done(function (content) {
            content.items.forEach(item => {
                string += getListItemFor(item.id, item.summary);
            });
            const googleList = $('#settings-list-group-google-cal-list');
            googleList.html(string);
            const inputList = googleList.children().children('input');
            checkboxController.initCheckboxList(inputList);
        });
    }
}

const checkboxes = $('label').find('input[type=checkbox]');

function setAllCheckboxVisibility() {
    console.log("Setting all checkbox-visibilities");
    checkboxes.each(function () {
        setCheckboxVisibility(this);
    })
}

function setCheckboxVisibility(checkbox) {
    const parent = $(checkbox).parent();
    parent.css("background", "red");
    parent.find('i').css('visibility', checkbox.checked ? 'visible' : 'hidden');
    if (checkbox.checked) {
        parent.addClass('active');
    } else {
        parent.removeClass('active');
    }
}

function loadAccessKeyBoxes() {
    if (getCookie("microsoft-access-key") !== undefined) {
        $("#microsoft-access-token").val(getCookie("microsoft-access-key"));
    } else {
        $("#account-settings-remove-microsoft-access-token-btn").css("display", "none");
    }
    if (getCookie("google-access-key") !== undefined) {
        $('#google-access-token').val(getCookie("google-access-key"));
    } else {
        $("#account-settings-remove-google-access-token-btn").css("display", "none");
    }
}

function getCookie(name) {
    const match = document.cookie.match(new RegExp('(^| )' + name + '=([^;]+)'));
    if (match) return match[2];
}

function removeCookie(cname) {
    const d = new Date("2000");
    const expires = "expires=" + d.toUTCString();
    document.cookie = cname + "=;" + expires + ";path=/";
    location.reload();
}

function getListItemFor(id, name) {
    return `
<label class="list-group-item list-group-item-action d-flex justify-content-between align-items-center">
    <input data-content="${id}" type="checkbox" class="form-check-input">
    ${name}
    <i class="fa fa-check badge badge-primary badge-pill"></i>
</label>`;
}