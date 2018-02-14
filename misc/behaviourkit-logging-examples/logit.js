// Logging to the behaviourkit logging endpoint javascript example
// More readable version, but basically the same as logit_golden.js

// Host name of the behaviourkit API
var HOST_NAME = "api.behaviourkit.tocker.iminds.be";
// Path of the logging endpoint
var LOGGING_PATH = "/logs";

// JWT keys of platforms: copy from http://admin.behaviourkit.tocker.iminds.be/platforms

var JWTKEY_SSO = "eyJ0eXAiOiJKV1QiLCJhbGciOiJIUzI1NiJ9.eyJwbGF0Zm9ybSI6IlNTTyIsInV1aWQiOiJiYTdiYTY3MC0wZmY1LTExZTgtYWNhNi0wZDM4MjYyMWE1NmIifQ.-gOJedAmc4fOSRGEvp0rpcgj-JbYfxri6bXRmmLG0dY";

// a logging example
var options = {
  "method": "POST",
  "hostname": HOST_NAME,
  "port": null,
  "path": LOGGING_PATH,
  "headers": {
    "authorization": "Bearer " + JWTKEY_SSO,
    "content-type": "application/json"
  }
};

var content = {
  "type": "martin.test/js/example",
  "userID": "martin.vanbrabant.ugent@gmail.com",
  "data": {
    "field1": "value1"
  }
};

var http = require("http");
var req = http.request(options, function (res) {
  var chunks = [];

  res.on("data", function (chunk) {
    chunks.push(chunk);
  });

  res.on("end", function () {
    var body = Buffer.concat(chunks);
    console.log(body.toString());
  });
});
req.write(JSON.stringify(content));
req.end();

