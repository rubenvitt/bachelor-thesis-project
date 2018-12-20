var express = require('express');
var path = require('path');
var cookieParser = require('cookie-parser');
var session = require('express-session');
var logger = require('morgan');
var engines = require('consolidate');
var proxy = require('http-proxy-middleware');
const urls = require('./modules/constants/urls');

var indexRouter = require('./routes/index');
var usersRouter = require('./routes/users');
var authRouter = require('./routes/auth');

var app = express();

app.use(logger('dev'));
app.use(express.json());
app.use(express.urlencoded({extended: false}));
app.use(cookieParser());
app.use(session({
    secret: '4DAxTM<G2A<*|1FXS_M}8fuOj4+E`LC64~|Liu"VT&+*zGKg,c-;1}z<L{&&Aw0',
    resave: false,
    saveUninitialized: true
}));

app.use('/', indexRouter);
app.use('/users', usersRouter);
app.use('/api', proxy(
    {
        target: 'https://localhost:8443',
        secure: false,
        changeOrigin: true,
        logLevel: 'debug',
        pathRewrite: {
            '^/api': '/'
        }
    }));
app.use('/controller', authRouter);

app.use(express.static(path.join(__dirname, '/public')));
app.set('views', __dirname + "/public");
app.engine("html", engines.mustache);
app.set("view engine", "html");

app.use(function (req, res, next) {
    res.status(404);

    if (req.accepts('html')) {
        res.render('404', {url: req.url});
        return;
    }
    if (req.accepts('json')) {
        res.send({error: 'Not found'});
        return;
    }
    res.type('txt').send('Not found.');
});


module.exports = app;
