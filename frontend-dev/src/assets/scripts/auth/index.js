if (document.getElementById('login-failure-alert') !== null) {
    const loginMail = document.getElementById('login-email');
    const loginPass = document.getElementById('login-password');
    const failureDiv = document.getElementById('login-failure-alert');

    $('#login-form').submit(function (evt) {
        evt.preventDefault();
        $.post('/controller', $(this).serialize())
            .done((req, evt, rawRequest) => {
                if (rawRequest.status === 202)
                    location.href = "/";
            })
            .fail(evt => {
                console.error(evt);
                if (evt.status === 401) {
                    $('#login-failure-alert').removeClass('d-none');
                } else {
                    location.pathname = "500.html";
                }
            });
    });
}


/*function testtesttest() {
    console.log("TEST SUCCESS");
}*/
