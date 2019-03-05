import * as URLS from '../constants/urls';
import {getFormData} from "../meeting-creation/form-sending-handler";

if (document.getElementById('login-failure-alert') !== null) {
    const loginMail = document.getElementById('login-email');
    const loginPass = document.getElementById('login-password');
    const failureDiv = document.getElementById('login-failure-alert');
    const loginForm = $('#login-form');
    const registerForm = $('#register-form');

    loginForm.removeClass('d-none');
    registerForm.addClass('d-none');

    loginForm.submit(function (evt) {
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


    registerForm.submit(function (evt) {
        evt.preventDefault();
        const user = {
            mail: $('#register-email').val(),
            name: $('#register-name').val(),
            password: $('#register-password').val(),
            position: $('#register-position').val(),
            avatar: $('#register-avatar').val()
        };
        $.ajax({
            url: `${URLS.apiUrl}/user/register`,
            type: 'POST',
            data: JSON.stringify(user),
            contentType: 'application/json'
        }).done(function () {
            console.log('finished user-registering');
        }).fail(evt => {
            if (evt.status === 201) {
                console.log('finished user-registering');
                location.href = '/';
            }
            else
                $('#register-failure-alert').removeClass('d-none');
        });
    });

    $('#register-button').click(function () {
        loginForm.addClass('d-none');
        registerForm.removeClass('d-none');
        $('#register-failure-alert').addClass('d-none');
    });

    $('#register-cancel').click(function () {
        registerForm.addClass('d-none');
        loginForm.removeClass('d-none');
        $('#login-failure-alert').addClass('d-none');
    });
}
