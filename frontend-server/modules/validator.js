const sessions = require('./sessionsArray');
const debug = require('debug')('frontend-server:auth-validator');

const ex = module.exports = {};

function showLoginIfNecessary(req) {
    debug("sessionID: " + req.session.id);
    //read cookie
    const cookieValue = req.cookies[sessions.cookie.cookieNamePrivateKey];
    debug("Read cookie: " + cookieValue);

    //get list and check, if session exist already...
    let keyValuePair;
    sessions.sessionArray.keys.forEach(value => keyValuePair = value.key === req.session.id ? value : keyValuePair);
    if (keyValuePair === undefined)
        debug("It is a new session!");
    else
        debug("It is not a new session!");

    //create a null-object for session
    keyValuePair = keyValuePair === undefined ? {key: "abc", val: 11} : keyValuePair;
    debug("key: " + keyValuePair.key + " contains: " + keyValuePair.val + " === " + cookieValue + "?");
    //return if user is logged in...
    return keyValuePair.val !== cookieValue;
}

ex.loginNecessary = showLoginIfNecessary;
