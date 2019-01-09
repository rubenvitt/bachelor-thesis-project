var express = require('express');
var debug = require('debug')('frontend-server:server');
var router = express.Router();
const sessions = require('../modules/sessionsArray');

/* GET home page. */
router.get('/', function (req, res, next) {
    if (!showLoginIfNecessary(req)) {
        debug("I know you!");
        res.render('index');
    } else {
        debug("You are new! Redirecting to login...");
        res.redirect("/login");
    }
});

router.get("/create-meeting", function (req, res) {
    if (!showLoginIfNecessary(req)) {
        debug("I know you!");
        debug("TEEEST");
        res.render('new_meeting');
    } else {
        debug("You are new! Redirecting to login...");
        res.redirect("/login");
    }
});

router.get('/finished', function (req, res, next) {
    if (!showLoginIfNecessary(req)) {
        debug("I know you!");
        debug("TEEEST");
        res.render('finished');
    } else {
        debug("You are new! Redirecting to login...");
        res.redirect("/login");
    }
});

router.get("/settings", function (req, res, next) {
    if (!showLoginIfNecessary(req)) {
        debug("I know you!");
        debug("TEEEST");
        res.render('settings');
    } else {
        debug("You are new! Redirecting to login...");
        res.redirect("/login");
    }
});

router.get('index.html', function (req, res) {
    res.redirect("/")
});

router.get('settings.html', function (req, res) {
    debug("SETTINGS");
    res.redirect("/index.html");
});

router.get('/abc', function (req, res) {
    res.render("forms");
});

router.get("/login", function (req, res) {
    if (showLoginIfNecessary(req)) {
        res.render('signin');
    } else
        res.redirect("../");
});

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

module.exports = router;
