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

function getUserID() {
    return getCookie('USER-ID');
}

function removeUserID() {
    return removeCookie('USER-ID');
}

export {
    getUserID,
    removeUserID
}