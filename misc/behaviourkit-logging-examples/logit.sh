#!/bin/bash
# Logging to the behaviourkit logging endpoint curl example
# More readable version, but basically the same as logit_golden.sh

#LOGGING_ENDPOINT=http://api.behaviourkit.tocker.iminds.be/logs
LOGGING_ENDPOINT=https://api.behaviourkit.duxis.io/logs

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
