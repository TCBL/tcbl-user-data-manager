#!/bin/bash
# Logging to the behaviourkit logging endpoint curl example
# More readable version, but basically the same as logit_golden.sh

# Host name of the behaviourkit API
HOST_NAME=api.behaviourkit.tocker.iminds.be
# Path of the logging endpoint
LOGGING_PATH=/logs

LOGGING_ENDPOINT=http://${HOST_NAME}${LOGGING_PATH}

# JWT keys of platforms: copy from http://admin.behaviourkit.tocker.iminds.be/platforms

JWTKEY_SSO=eyJ0eXAiOiJKV1QiLCJhbGciOiJIUzI1NiJ9.eyJwbGF0Zm9ybSI6IlNTTyIsInV1aWQiOiJiYTdiYTY3MC0wZmY1LTExZTgtYWNhNi0wZDM4MjYyMWE1NmIifQ.-gOJedAmc4fOSRGEvp0rpcgj-JbYfxri6bXRmmLG0dY

# a logging example
curl -H "authorization: Bearer ${JWTKEY_SSO}" -H "content-type: application/json" -d @- <<_EOF_ ${LOGGING_ENDPOINT}
{
  "type": "martin.test/curl/example",
  "userID": "martin.vanbrabant.ugent@gmail.com",
  "data": {
    "field1": "value1"
  }
}
_EOF_
