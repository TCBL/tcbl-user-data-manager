// "Golden" reference logging in javascript, derived from mails with Jan Everaert
var http = require("http");
var options = {
  "method": "POST",
  "hostname": "api.behaviourkit.tocker.iminds.be",
  "port": null,
  "path": "/logs",
  "headers": {
    "authorization": "Bearer eyJ0eXAiOiJKV1QiLCJhbGciOiJIUzI1NiJ9.eyJwbGF0Zm9ybSI6IlNTTyIsInV1aWQiOiJiYTdiYTY3MC0wZmY1LTExZTgtYWNhNi0wZDM4MjYyMWE1NmIifQ.-gOJedAmc4fOSRGEvp0rpcgj-JbYfxri6bXRmmLG0dY",
    "content-type": "application/json"
  }
};

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

// argument in single quotes!
req.write('{"type": "martin.test/js/golden", "userID": "martin.vanbrabant.ugent@gmail.com", "data": {"field1": "value1"}}');
req.end();
