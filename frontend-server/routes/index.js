var express = require('express');
var debug = require('debug')('frontend-server:server');
const apiDebug = require('debug')('frontend-server:server:api');
var router = express.Router();
const showLoginIfNecessary = require('../modules/validator').loginNecessary;

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

router.get("/rooms", function (req, res) {
    if (!showLoginIfNecessary(req)) {
        debug("I know you!");
        debug("TEEEST");
        res.render('rooms');
    } else {
        debug("You are new! Redirecting to login...");
        res.redirect("/login");
    }
});

router.get("/calendar", function (req, res) {
    if (!showLoginIfNecessary(req)) {
        debug("I know you!");
        debug("TEEEST");
        res.render('calendar');
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

router.get("/callback", function (req, res, next) {
    let subject = req.query.subject;
    res.send(`Event was (maybe) created by this application. Subject is: ${subject}`);
});

router.get('index.html', function (req, res) {
    res.redirect("/")
});

router.get("/login", function (req, res) {
    if (showLoginIfNecessary(req)) {
        res.render('signin');
    } else
        res.redirect("../");
});

router.apiFunction = function (req, res, next) {
    apiDebug(`checking if client is authenticated for ${req.path}`);
    if (showLoginIfNecessary(req)) {
        res.sendStatus(403);
        apiDebug(`client not authenticated for: ${req.path}`);
    } else {
        apiDebug(`client is authenticated for: ${req.path}`);
        next();
    }
};

module.exports = router;
