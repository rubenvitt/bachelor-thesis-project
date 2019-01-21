import * as appUser from '../appuser';
import * as $ from 'jquery';

if ($('div.header-container').length) {
    //page with topbar...
    appUser.getActualAppUser(/**
     * @param {appUser} content
     */
    function (content) {
        $('#profile_image').attr('src', content.avatar);
        $('#profile_username').text(content.name);
    })
}