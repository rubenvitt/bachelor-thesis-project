var express = require('express');
var router = express.Router();
var debug = require('debug')('frontend-server:server');

router.post('/', function (req, res, next) {
    debug("auth/POST was accessed");
    debug(req.body);
    debug("user: " + req.body.email);
    debug("password: " + req.body.password);

    if (passwordCorrect(req.body.email, req.body.password))
        res.redirect('/');
    else
        res.render('403');
});

function passwordCorrect(email, pass) {
    return pass !== '123';
}

module.exports = router;
