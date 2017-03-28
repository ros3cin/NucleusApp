var express = require('express');
var url     = require('url');
var http    = require('http');
var bodyParser = require('body-parser')
var app     = express();
var myPort=1026;

function createElements(){
  var elements = [];
  for(var i = 1; i <= 100; i++){
    elements = elements.concat({
      contextElement : {
        id : i+"",
        attributes : [{value:"2,3"},{value:"1.2"},{value: "broken"}]
      }
    });
  }
  return elements;
}

var postJSON = {contextResponses : createElements()};

app.use(bodyParser.urlencoded({
  extended: true
}));
app.use(bodyParser.json());

app.get('/', function (req, res) {
  res.send("Root ok!");
});

app.get('/v1/contextEntities/:entityId/attributes/:attributeName', function (req, res) {
  res.send({attributes : ['1','2']});
});

app.post('/v1/queryContext/', function (req, res) {
  console.log(req.body);
  res.send(postJSON);
});



app.listen(myPort, function () {
  console.log('Example app listening on port 1026!');
});