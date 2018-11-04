const express = require('express');
const router = express.Router();
const debug = require('debug')('frontend-server:server');
const crypto = require('crypto');
const sessions = require('../modules/sessionsArray');

router.post('/', function (req, res, next) {
    debug("auth/POST was accessed");
    debug(req.body);
    debug("user: " + req.body.email);
    debug("password: " + req.body.password);

    if (passwordCorrect(req.body.email, req.body.password)) {
        const entry = {key: req.session.id, val: crypto.randomBytes(64).toString('hex')};
        sessions.sessionArray.keys.push(entry);
        res.cookie(sessions.cookie.cookieNamePrivateKey, entry.val, {httpOnly: true});
        res.status(202).send("AUTH OKAY");
    }
    else
        res.status(403).send("WRONG CREDENTIALS");
});

function passwordCorrect(email, pass) {
    return pass !== '123';
}

module.exports = router;
