var express = require('express');
var path = require('path');
var favicon = require('serve-favicon');
var logger = require('morgan');
var cookieParser = require('cookie-parser');
var bodyParser = require('body-parser');

var routes = require('./routes/index');
var users = require('./routes/users');

var http = require('http');
var JWT = require('jsonwebtoken');

var app = express();

// para acessar UAA
var clientid = 'myapp'
var clientsecret = 'myappclientsecret'

// view engine setup
app.set('views', path.join(__dirname, 'views'));
app.set('view engine', 'jade');

// uncomment after placing your favicon in /public
//app.use(favicon(path.join(__dirname, 'public', 'favicon.ico')));
app.use(logger('dev'));
app.use(bodyParser.json());
app.use(bodyParser.urlencoded({ extended: false }));
app.use(cookieParser());
app.use(express.static(path.join(__dirname, 'public')));

app.use('/', routes);
app.use('/users', users);

app.get('/callback', function(req, res) {

  
  var code = req.query.code;
  var accessToken;
  var sub;
  var user_name;
  var email;
  var scope;
  
  getAccessToken(code, function(tokens) {
    console.log('getAccessToken callback');
    console.log('accessToken : ' + tokens.access_token);
    
    accessToken = tokens.access_token;
    
    getTokenkey(clientid, clientsecret, function(tokenkeyJson) {
      var idToken = JWT.verify(tokens.id_token,tokenkeyJson.value);
	  console.log('idToken:' + idToken);
	
	  sub = idToken.sub;
	  user_name = idToken.user_name;
	  email = idToken.email;
	  scope = idToken.scope;
	
	  console.log('sub:'+ sub);
	
      var txt = '<html><head>'
              + '<!-- Latest compiled and minified CSS -->'
              + '<link rel="stylesheet" href="https://maxcdn.bootstrapcdn.com/bootstrap/3.3.5/css/bootstrap.min.css" integrity="sha512-dTfge/zgoMYpP7QbHy4gWMEGsbsdZeCXz7irItjcC3sPUFtf0kuFbDz/ixG7ArTxmDjLXDmezHubeNikyKGVyQ==" crossorigin="anonymous">'
              + '<!-- Optional theme -->'
              + '<link rel="stylesheet" href="https://maxcdn.bootstrapcdn.com/bootstrap/3.3.5/css/bootstrap-theme.min.css" integrity="sha384-aUGj/X2zp5rLCbBxumKTCw2Z50WgIr1vs/PFN4praOTvYXWlVyh2UtNUU0KAUhAX" crossorigin="anonymous">'
              + '<!-- Latest compiled and minified JavaScript -->'
              + '<script src="https://maxcdn.bootstrapcdn.com/bootstrap/3.3.5/js/bootstrap.min.js" integrity="sha512-K1qjQ+NcF2TYO/eI3M6v8EiNYZfA95pQumfvcVrTHtwQVDG+aHRqLi/ETn2uB+1JqwYqVG3LIvdm9lj6imS/pQ==" crossorigin="anonymous"></script>'
              + '</head><body>'
              + '<div class="container-fluid">'
              + '<div class="panel panel-warning">'
              + '<div class="panel-heading"><h5><code>oauth/token</code> response</h3></div>'
              + '<div class="panel-body">'
              + '<div class="row">'
              + '<div class="col-sm-2"><span class="label label-primary">access_token</span></div><div class="col-sm-10">' + tokens.access_token + '</div>'
              + '</div>'
              + '<div class="row">'
              + '<div class="col-sm-2"><span class="label label-primary">token_type</span></div><div class="col-sm-10">' +  tokens.token_type + '</div>'
              + '</div>'
              + '<div class="row">'
              + '<div class="col-sm-2"><span class="label label-primary">id_token</span></div><div class="col-sm-10">' + tokens.id_token + '</div>'
              + '</div>'
              + '<div class="row">'
              + '<div class="col-sm-2"><span class="label label-primary">refresh_token</span></div><div class="col-sm-10">' + tokens.refresh_token + '</div>'
              + '</div>'
              + '<div class="row">'
              + '<div class="col-sm-2"><span class="label label-primary">expires_in</span></div><div class="col-sm-10">' + tokens.expires_in + '</div>'
              + '</div>'
              + '<div class="row">'
              + '<div class="col-sm-2"><span class="label label-primary">scope</span></div><div class="col-sm-10">' + tokens.scope + '</div>'
              + '</div>'
              + '<div class="row">'
              + '<div class="col-sm-2"><span class="label label-primary">jti</span></div><div class="col-sm-10">' + tokens.jti + '</div>'
              + '</div>'
              + '</div>'
              + '</div>'
              ;
        txt += '<hr>';
      
        txt +=  '<div class="panel panel-success">'
            + '<div class="panel-heading"><h5><code>id_token</code> parsed</h3></div>'
            + '<div class="panel-body">'
            + '<div class="row">'
            + '<div class="col-sm-2"><span class="label label-primary">id_token</span></div><div class="col-sm-10">' + new Buffer(tokens.id_token, 'base64') + '</div>'
            + '</div>'
            + '<div class="row">'
            + '<div class="col-sm-2"><span class="label label-primary">sub</span></div><div class="col-sm-10">' + sub + '</div>'
            + '</div>'
            + '<div class="row">'
            + '<div class="col-sm-2"><span class="label label-primary">user_name</span></div><div class="col-sm-10">' + user_name + '</div>'
            + '</div>'
            + '<div class="row">'
            + '<div class="col-sm-2"><span class="label label-primary">email</span></div><div class="col-sm-10">' + email + '</div>'
            + '</div>'
            + '<div class="row">'
            + '<div class="col-sm-2"><span class="label label-primary">scope</span></div><div class="col-sm-10">' + scope + '</div>'
            + '</div>'
            + '</div>'
            + '</div>'
            + '</div>'
            + '</body></html>'
            ;
      
        res.set('Content-type', 'text/html');
        res.send(txt);

    });
	
});
  
  /*
  res.redirect('http://localhost:8080/uaa/oauth/token?'+
    'client_id=myapp2&'+
    'client_secret=myapp2clientsecret&'+
    'code=' + code +
    '&grant_type=authorization_code');
  */
  
  console.log('get callback');
});

function getAccessToken(code, callback) {
  var req2 = http.get({
    host:'localhost',
    port:8080,
    path:'http://localhost:8080/uaa/oauth/token?'+
    //     'client_id=myapp2&'+
    //     'client_secret=myapp2clientsecret&'+
         'code=' + code +
         '&grant_type=authorization_code',
    headers: { 
      'Authorization' : 'Basic ' + new Buffer(clientid + ':' + clientsecret).toString('base64')
    }
  }, function (resp) {
    console.log('getAccessToken: http get callback');
    console.log('STATUS: ' + resp.statusCode);
    console.log('HEADERS: ' + JSON.stringify(resp.headers));
    resp.setEncoding('utf8');
    
    var jsontxt = '';
    resp.on('data', function (chunk) {
      console.log('BODY: ' + chunk);
      jsontxt += chunk;
    }); 
    
    resp.on('end', function () {
      var json = JSON.parse(jsontxt);
    
      callback(json);
    });    

  });
}

function getTokenkey(clientid, clientsecret, callback) {

  var req2 = http.get({
      host:'localhost',
      port:8080,
      path:'http://localhost:8080/uaa/token_key'
    //+
    //     'client_id=myapp2&'+
    //     'client_secret=myapp2clientsecret&'+
    //     'access_token=' + accessToken +
    //     '&grant_type=authorization_code',
    ,
    headers: { 
      'Authorization' : 'Basic ' + new Buffer(clientid + ':' + clientsecret).toString('base64')
    }
  }, function (resp) {
    console.log('getTokenkey: http get callback');
    console.log('STATUS: ' + resp.statusCode);
    console.log('HEADERS: ' + JSON.stringify(resp.headers));
    resp.setEncoding('utf8');
    
    var jsontxt = '';
    resp.on('data', function (chunk) {
      console.log('BODY: ' + chunk);
      jsontxt += chunk;
    }); 
    
    resp.on('end', function () {
      var json = JSON.parse(jsontxt);
    
      console.log('getTokenkey:'+ json);
      callback(json);
    });    

  });
  
}

/*
function getUserInfo(accessToken, callback) {

  var req2 = http.get({
    host:'localhost',
    port:8080,
    path:'http://localhost:8080/uaa/oauth/userinfo?'
    //+
    //     'client_id=myapp2&'+
    //     'client_secret=myapp2clientsecret&'+
    //     'access_token=' + accessToken +
    //     '&grant_type=authorization_code',
    ,
    headers: { 
      'Authorization' : 'Bearer ' + accessToken
    }
  }, function (resp) {
    console.log('getUserInfo: http get callback');
    console.log('STATUS: ' + resp.statusCode);
    console.log('HEADERS: ' + JSON.stringify(resp.headers));
    resp.setEncoding('utf8');
    
    var jsontxt = '';
    resp.on('data', function (chunk) {
      console.log('BODY: ' + chunk);
      jsontxt += chunk;
    }); 
    
    resp.on('end', function () {
      var json = JSON.parse(jsontxt);
    
      callback(json);
    });    

  });
  
}
*/

app.post('/callback', function(req, res) {
  //res.send('params:' + req.params);
  console.log('post callback');
});

app.get('/changemail', function(req, res) {
  console.log('changemail:' + req);
});

// catch 404 and forward to error handler
app.use(function(req, res, next) {
  var err = new Error('Not Found');
  err.status = 404;
  next(err);
});

// error handlers

// development error handler
// will print stacktrace
if (app.get('env') === 'development') {
  app.use(function(err, req, res, next) {
    res.status(err.status || 500);
    res.render('error', {
      message: err.message,
      error: err
    });
  });
}

// production error handler
// no stacktraces leaked to user
app.use(function(err, req, res, next) {
  res.status(err.status || 500);
  res.render('error', {
    message: err.message,
    error: {}
  });
});

module.exports = app;
