var express = require('express');
var debug = require('debug')('frontend-server:server');
var crypto = require('crypto');
var router = express.Router();

var sessions = {};
sessions.keys = [];
const cookieNamePrivateKey = 'PRIVATE-TOKEN';

/* GET home page. */
router.get('/', function (req, res, next) {
    if (!showLoginIfNecessary(req)) {
        debug("I know you!");
        res.render('index');
    } else {
        debug("You are new!");
        var entry = {key: req.session.id, val: crypto.randomBytes(64).toString('hex')};
        sessions.keys.push(entry);
        res.cookie(cookieNamePrivateKey, entry.val, {httpOnly: true});
        res.render("signin");
    }
});

router.get('/abc', function (req, res) {
    res.render("forms");
});

router.get("/test", function (req, res) {
    //res.sendFile(path.join(__dirname, '../public/signin.html'));
    res.render('signin');
});

function showLoginIfNecessary(req) {
    debug("sessionID: " + req.session.id);
    //read cookie
    const cookieValue = req.cookies[cookieNamePrivateKey];
    debug("Read cookie: " + cookieValue);

    //get list and check, if session exist already...
    let keyValuePair;
    sessions.keys.forEach(value => keyValuePair = value.key === req.session.id ? value : keyValuePair);
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
