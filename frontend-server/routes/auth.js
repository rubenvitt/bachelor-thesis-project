const express = require('express');
const router = express.Router();
const debug = require('debug')('frontend-server:server');
const crypto = require('crypto');
const sessions = require('../modules/sessionsArray');
const apiUrl = require('../modules/constants/urls').urls.apiUrl;
const fs = require('fs');

const jsdom = require("jsdom");
const {JSDOM} = jsdom;
const {window} = new JSDOM();
const {document} = (new JSDOM('')).window;
global.document = document;

var $ = jQuery = require('jquery')(window);

router.post('/', function (req, res, next) {
    debug("controller/POST was accessed");
    debug(req.body);
    debug("user: " + req.body.email);
    debug("password: " + req.body.password);

    passwordCorrect(req.body.email, req.body.password, function (error, statusCode, headers, body) {
        if (statusCode === 200) {
            const entry = {key: req.session.id, val: crypto.randomBytes(64).toString('hex')};
            sessions.sessionArray.keys.push(entry);
            res.cookie(sessions.cookie.cookieNameUserID, req.body.email);
            res.cookie(sessions.cookie.cookieNamePrivateKey, entry.val, {httpOnly: true});
            res.status(202).send("AUTH OKAY");
        } else
            res.status(401).send("WRONG CREDENTIALS");
    });
});

function passwordCorrect(email, pass, callback) {
    'use strict';

    const httpTransport = require('https');
    const responseEncoding = 'utf8';
    const httpOptions = {
        hostname: apiUrl,
        port: '8443',
        path: '/login',
        method: 'POST',
        headers: {"Content-Type": "application/json; charset=utf-8"},
        json: {
            email: email,
            password: pass
        },
        rejectUnauthorized: false
    };
    httpOptions.headers['User-Agent'] = 'node ' + process.version;

    const request = httpTransport.request(httpOptions, (res) => {
        let responseBufs = [];
        let responseStr = '';

        res.on('data', (chunk) => {
            if (Buffer.isBuffer(chunk)) {
                responseBufs.push(chunk);
            } else {
                responseStr = responseStr + chunk;
            }
        }).on('end', () => {
            responseStr = responseBufs.length > 0 ?
                Buffer.concat(responseBufs).toString(responseEncoding) : responseStr;

            callback(null, res.statusCode, res.headers, responseStr);
        });

    })
        .setTimeout(0)
        .on('error', (error) => {
            callback(error);
        });
    request.write(JSON.stringify({email: email, password: pass}));
    request.end();
}

module.exports = router;
