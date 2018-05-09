# Activity logging.

## Endpoint
The TCBL user manager logs activities to the activity logging endpoint defined in `application.yml`.

## Platform
Platform name: `USERMANAGER`

## Log types and the extra data sent 

| Log type | Extra data |
| -------- | ---------- |
| `usermanager.user/registration.mailsent` | - |
| `usermanager.user/registration.completed` | All relevant user properties and their values, except user name, password, first name, last name; pictureURL is replaced by an indication whether a picture was given or not. |
| `usermanager.user/login` | - |
| `usermanager.user/logout` | - |
| `usermanager.user/profile.updated` | `updates`: a list of strings of names of all relevant updated user properties and for all except first name and last name, followed by the new value between brackets; `pictureURL` is replaced by `picture`, followed by `(added)`, `(updated)` or `(deleted)`. |
| `usermanager.user/resetpassword.mailsent` | - |
| `usermanager.user/resetpassword.completed` | - |

## Examples

In the examples below, the contents of the http POST request are shown as text.

### User registration mail sent

```json
{"userID":"martin.vanbrabanttest2@gmail.com","type":"usermanager.user/registration.mailsent"}
```

### User registration completed

```json
{"userID":"martin.vanbrabanttest2@gmail.com","type":"usermanager.user/registration.completed","data":{"hasPicture":true,"subscribedNL":true,"acceptedPP":true,"allowedMon":true}}
```

### User login

```json
{"userID":"martin.vanbrabanttest2@gmail.com","type":"usermanager.user/login"}
```

### User logout

```json
{"userID":"martin.vanbrabanttest2@gmail.com","type":"usermanager.user/logout"}
```

### User profile updated

#### Maximum

```json
{"userID":"martin.vanbrabanttest1@gmail.com","type":"usermanager.user/profile.updated","data":{"updates":["firstName","lastName","picture (added)","subscribedNL (true)","acceptedPP (true)","allowedMon (true)"]}}
```

#### Minimum

```json
{"userID":"martin.vanbrabanttest1@gmail.com","type":"usermanager.user/profile.updated","data":{"updates":["subscribedNL (false)"]}}
```

### Reset password mail sent

```json
{"userID":"martin.vanbrabanttest2@gmail.com","type":"usermanager.user/resetpassword.mailsent"}
```

### Reset password completed 

```json
{"userID":"martin.vanbrabanttest2@gmail.com","type":"usermanager.user/resetpassword.completed"}
```

