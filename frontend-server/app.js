var express = require('express');
var path = require('path');
var cookieParser = require('cookie-parser');
var session = require('express-session');
var logger = require('morgan');
var engines = require('consolidate');

var indexRouter = require('./routes/index');
var usersRouter = require('./routes/users');

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

app.use(express.static(path.join(__dirname, '/public')));
app.set('views', __dirname + "/public");
app.engine("html", engines.mustache);
app.set("view engine", "html");

module.exports = app;
