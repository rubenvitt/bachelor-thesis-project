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

    if (getCookie("microsoft-access-key") !== null) {
        $("#microsoft-access-token").val(getCookie("microsoft-access-key"));
    }
}

function getCookie (name) {
    const match = document.cookie.match(new RegExp('(^| )' + name + '=([^;]+)'));
    if (match) return match[2];
}