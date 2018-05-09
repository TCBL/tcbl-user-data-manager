# Activity logging

## Endpoint
The TCBL user manager logs activities to the activity logging endpoint defined in `application.yml`.

## Platform
Platform name: `USERMANAGER`

## Conditions
Activity logging is done only if the user explicitly allows it in his user profile (field `allowedMon`).
Changes to this field are effective immediately.

## What is logged
Each logging record is about a certain user, identified by his user name in the system (the `userID` field in the logging record).

Each logging record is about a certain event, identified by its logging type (the `type` field in the logging record).

For some logging types, extra data is available in the optional `data` field in the logging record.
 
Below a list of all logging types. For all types, the examples show the *beautified* JSON contents of the http POST request sent to the logging endpoint.

### User registration mail sent
Example:
```json
{
  "userID": "martin.vanbrabanttest2@gmail.com",
  "type": "usermanager.user/registration.mailsent"
}
```

### User registration completed
Example:
```json
{
  "userID": "martin.vanbrabanttest2@gmail.com",
  "type": "usermanager.user/registration.completed",
  "data": {
    "hasPicture": false,
    "acceptedPP": true,
    "allowedMon": true,
    "subscribedNL": false
  }
}
```

The `data` node contains the values of all relevant user profile fields, except for `userName`, `firstName` and `lastName`,
left out for privacy reasons.

The `pictureURL` user profile field is replaced by boolean `hasPicture`, because `pictureURL` is either null (`hasPicture = false`)
or has a fixed username-dependent value (`hasPicture = true`). The fixed username-dependent value is of no interest for activity logging.

### User login
Example:
```json
{
  "userID": "martin.vanbrabanttest2@gmail.com",
  "type": "usermanager.user/login"
}
```

### User profile updated
Example:
```json
{
  "userID": "martin.vanbrabanttest2@gmail.com",
  "type": "usermanager.user/profile.updated",
  "data": {
    "hasPicture": true,
    "acceptedPP": true,
    "allowedMon": true,
    "subscribedNL": true,
    "updates": [
      "firstName",
      "lastName",
      "hasPicture",
      "subscribedNL"
    ]
  }
}
```

The `data` node is an extension of the one in the *user registration completed* case: an `updates` array is added.
This array contains the actual user profile updates. It can contain the following strings:
* `firstName`
* `lastName`
* `hasPicture`
* `picture`
* `acceptedPP`
* `allowedMon`
* `subscribedNL`

Most of these strings correspond to user profile field names, except for
* `hasPicture`: refers to the pseudo field with this name, described in *User registration completed* above
* `picture`: indicates that the actual picture was updated (not the `pictureURL, see why in *User registration completed* above) 

This example illustrates an update of the picture:
```json
{
  "userID": "martin.vanbrabanttest2@gmail.com",
  "type": "usermanager.user/profile.updated",
  "data": {
    "hasPicture": true,
    "acceptedPP": true,
    "allowedMon": true,
    "subscribedNL": true,
    "updates": [
      "picture"
    ]
  }
}
```

And this example illustrates the deletion of the picture:
```json
{
  "userID": "martin.vanbrabanttest2@gmail.com",
  "type": "usermanager.user/profile.updated",
  "data": {
    "hasPicture": false,
    "acceptedPP": true,
    "allowedMon": true,
    "subscribedNL": true,
    "updates": [
      "hasPicture"
    ]
  }
}
```

### User logout
Example:
```json
{
  "userID": "martin.vanbrabanttest2@gmail.com",
  "type": "usermanager.user/logout"
}

```

### Reset password mail sent
Example:
```json
{
  "userID": "martin.vanbrabanttest2@gmail.com",
  "type": "usermanager.user/resetpassword.mailsent"
}
```

### Reset password completed 
Example:
```json
{
  "userID": "martin.vanbrabanttest2@gmail.com",
  "type": "usermanager.user/resetpassword.completed"
}
```