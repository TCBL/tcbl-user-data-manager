# TCBL User Data Manager.

This application allows to manage user data as stored in a Gluu Server and a relational database.
It is a Java Spring Boot application that uses OpenID Connect 1.0 to authorize and SCIM 2.0 to manage the data.
This README describes how to build and configure it.

The code and documentation are based on [this sample](https://github.com/spring-projects/spring-security/tree/master/samples/boot/oauth2login)
and [this how-to](https://www.drissamri.be/blog/java/enable-https-in-spring-boot/).

## Building

Building the code is trivial using maven. Or open the `pom.xml` as a project in your IDE.

To create a runnable jar, open a console, go to the root of the project directory and type:

```
mvn package
```

This creates something like `target/UserDataManager-1.0-SNAPSHOT.jar`.

## Configuring

In this section, some configuration snippets are shown. They all apply to a client configuration file `application.yml`.
There's a **putting it all together** at the end of this section, where everything is, well, put together.

### 1. Basic Application Container configuration

Every Spring Boot app runs on a servlet container (either Tomcat or Jetty; Tomcat by default).
This requires configuration of the port the server will listen to. Besides that, our application
will be served with context `/usermanager`, meaning that this will be the root of our application on the URL, and that
redirect URL's are generated correctly (especially in a reverse proxy configuration). Configuration snippet (example):

```yaml
server:
  port: 443
  servlet:
    context-path: /usermanager
```

There are two options to let the outside world communicate with the application, and both have their configuration implications:
1. Direct communication (no reverse proxy). See **1.a Enable HTTPS for the application**. Typical for a development environment.
1. Via a reverse proxy. See **1.b. Configure a reverse proxy**. Typical for a production environment.

### 1.a Enable HTTPS for the application (optional)

***Only when run as a standalone application.***

In order to use OpenID Connect, the application needs to be able to receive https requests.
Two options here: using a self-signed certificate or a certificate from a Certificate Authority.

#### a. Self-signed

Use `keytool` (shipped with a JDK or JRE) to generate a certificate and store it in a keystore.
You can choose the alias (`tudm`) and the name of the keystore (`tudm.jks`)
as you wish, but remember them for later.

Execute the next command in a directory where you will (permanently) store the keystore file:
```
keytool -genkey -alias tudm -storetype PKCS12 -keyalg RSA -keysize 2048 -keystore tudm.jks -validity 3650
```

You don't *have* to provide much details in order for it to work. Just remember the password, you will need it later on.

```
Enter keystore password:
 Re-enter new password:
 What is your first and last name?
 [Unknown]:
 What is the name of your organizational unit?
 [Unknown]:
 What is the name of your organization?
 [Unknown]:
 What is the name of your City or Locality?
 [Unknown]:
 What is the name of your State or Province?
 [Unknown]:
 What is the two-letter country code for this unit?
 [Unknown]:
 Is CN=Unknown, OU=Unknown, O=Unknown, L=Unknown, ST=Unknown, C=Unknown correct?
 [no]: yes
```

#### b. From a CA

If you already have a certificate (chain), find the .crt file. Then import it into the keystore.

Execute the next command in a directory where you will (permanently) store the keystore file:

```
keytool -importcert -file the_certificate.crt -alias tudm -keystore tudm.jks

```

#### Configuration snippet

Whatever option you chose above, this requires the following configuration snippet (example):

```yaml
server:
  port: 443
  ssl:
    key-store: /home/ghaesen/projects/TCBL/config/tudm.jks
    key-store-password: secret
    key-store-type: PKCS12
    key-alias: tudm
  servlet:
    context-path: /usermanager
```

### 1.b Configure a reverse proxy (optional)

***Only when run in combination with an external HTTP server.***

In this scenario, you already have an HTTP server installed which provides the TLS certificate and serves some contents.

We have to configure that HTTP server to act as a reverse proxy, which means requests will be forwarded to another URL.

As an example, consider the Apache 2.4.18 HTTP server used inside the Gluu container (gluu-server-3.0.2).
It is very interesting to adapt that HTTP server, since it is listening to port 443 for HTTPS.
This allows our application to be accessible through this port on the same physical server.

Our application uses AJP in stead of HTTP for communication with the proxy because then it pretends to be part of the
HTTP (proxy) server. It is more efficient and automatically takes care of rewrites, header passing, etc.

The set-up is:
* Our app listens on `ajp://localhost:8445/usermanager`.
* Apache redirects requests originating from `/usermanager` to `ajp://localhost:8445/usermanager`

For example, on the test server, this means that requests to `https://tcblsso2.ilabt.imec.be/usermanager` reach our app.

#### First, configure Apache to act as a reverse proxy.

If you're adapting the Gluu container's Apache, first login to the gluu container.

Enable the necessary Apache module:
```bash
# a2enmod proxy_ajp
```

Add the following snippet to the bottom of the appropriate `<VirtualHost>` section in the appropriate Apache configuration file.
If you're adapting the Gluu container's Apache, it's the file `/etc/apache2/sites-available/https_gluu.conf`.
```
# --- IDLab begin

# Reverse proxy so that requests to /usermanager are forwarded via AJP to port 8445
ProxyPass        /usermanager ajp://localhost:8445/usermanager

# --- IDLab end
```

Restart Apache:
```bash
# service apache2 restart
```

#### The app requires some configuration too

Configuration snippet (example):
```yaml
server:
  port: 8444
  ajp:
    port: 8445
    scheme: https
    proxy-name: tcblsso2.ilabt.imec.be
    proxy-port: 443
    secure: true
  servlet:
    context-path: /usermanager
```

Mind the assignment of a free port number to server.port (which seems necessary to let spring work).

Here is what the AJP parameters do:
* **port**: port where the *application server* listens at (i.e. our application).
* **scheme**: the scheme of the incoming requests *on the proxy*, what the outside world uses.
* **proxy-name**: the name of the host as known by the outside world.
* **proxy-port**: the port reachable for the outside world.
* **secure**: do we trust requests to the application server? In this case we do, because the proxy uses https and runs
on the same host.

More info about these parameters [here](https://tomcat.apache.org/tomcat-8.5-doc/config/ajp.html).

### 2. Register the client on the server

**Before registering, make sure there is a scope `inum` with a claim `inum` created on the server!**

This is a manual registration because Spring Security Oauth2 doesn't support dynamic registration for OpenID Connect yet.
Here are the settings (example, adjust to correct hosts):

* Client Name: TCBL_manager
* Application Type: Web
* Pre-Authorization: True (this skips the authorization step, since it makes no sence for this application)
* Persist Client Authorizations: False
* Logo URI: https://tcblsso.ilabt.iminds.be/resources/logos/login-with-TCBL.png
* Subject Type: pairwise
* Authentication method for the Token Endpoint: client_secret_post
* Redirect Login URIs: https://ravel.elis.ugent.be:8444/usermanager/login/oauth2/code/tcbl_manager (or wherever the app lives, on the test server, this would be https://tcblsso2.ilabt.imec.be/usermanager/login/oauth2/code/tcbl_manager)
* Redirect Logout URIs: https://ravel.elis.ugent.be:8444/usermanager 
* Scopes: openid, inum
* Response Types: code
* Grant Types: authorization_code

For the client, this requires the following configuration snippet (example):

```yaml
spring:
  security:
    oauth2:
      client:
        registration:
          tcbl_manager:
            client-id: "@!4F1B.EBA3.75E2.F47A!0001!EF35.6902!0008!1B4C.7A50.7F55.50D7"
            client-secret: averysecrativesecret
            client-name: TCBL_manager
            client-authentication-method: post
            redirect-uri-template: "{baseUrl}/login/oauth2/code/{registrationId}"
            scope: openid, inum
            provider: gluu-honegger
            authorization-grant-type: authorization_code
        provider:
          gluu-honegger:
            authorization-uri: "https://honegger.elis.ugent.be/oxauth/seam/resource/restv1/oxauth/authorize"
            token-uri: "https://honegger.elis.ugent.be/oxauth/seam/resource/restv1/oxauth/token"
            user-info-uri: "https://honegger.elis.ugent.be/oxauth/seam/resource/restv1/oxauth/userinfo"
            jwk-set-uri: "https://honegger.elis.ugent.be/oxauth/seam/resource/restv1/oxauth/jwks"
```

### 3. SCIM configuration

Follow the instructions of [1. Preparations](https://git.datasciencelab.ugent.be/TCBL/internal-server-docs/wikis/scim-2-0-setup#1-preparations) of
chapter **Setting up a SCIM 2.0 client** from the Scim 2.0 wiki page. In this page, the client is this application.

Skip **2 Setting up the client application**.

Now you should have a client ID, a jks file (containing the certificate from the host running the Gluu Server) and the password of the jks file.
The client key id can be left empty since there is only one key in the store.

This requires the following configuration snippet (example):

```yaml
scim:
  domain: "https://honegger.elis.ugent.be/identity/seam/resource/restv1"
  meta-data-url: "https://honegger.elis.ugent.be/.well-known/uma-configuration"
  aat-client-id: "@!4F1B.EBA3.75E2.F47A!0001!EF35.6902!0008!224B.6C55"
  aat-client-jks-path: /home/ghaesen/projects/TCBL/config/scim-rp-honegger.jks
  aat-client-jks-password: secret
  aat-client-key-id:
```

### 4. Datasource configuration

Follow the instructions of [MariaDB](https://git.datasciencelab.ugent.be/TCBL/internal-server-docs/wikis/mariadb) of the
server installation to set up a database.

Now we need to create a user (and password) and a table. Become root (*not* just sudo) and log into the database:

```shell
sudo su -
mysql -u root
```

and issue the following commands (and replace 'ThePassword' with a real password):


```sql
create database usermanager;
create user 'tcbluser'@'localhost' identified by 'ThePassword';
grant all on usermanager.* to 'tcbluser'@'localhost';
```

Then we need to add this database as a data source in the `spring` section of the `application.yml` (of course with a better password):

```yaml
spring:

  # other config...

  # database config
  jpa:
    hibernate:
      ddl-auto: update
  datasource:
    url: "jdbc:mysql://localhost:3306/usermanager"
    username: tcbluser
    password: "ThePassword"
    driver-class-name: org.mariadb.jdbc.Driver
```

Hint: if you need to connect from your local machine to a database on another server, an SSH tunnel can help. E.g.:

```shell
ssh -NL 3307:localhost:3306 honegger.elis.ugent.be
```

You then just have to change the port to `3307` in the configuration above.

### 5. Limit the size of file uploads

The TCBL User Data Manager has a feature to allow profile picture uploads. Best practice is to limit the file size at the Spring level.

```yaml
spring:

  # other config...

  # Servlet settings
  servlet:
    # Limit file upload size
    multipart:
      max-file-size: 1MB
      max-request-size: 1MB
```

### 6. TCBL User Data Manager application specific settings

The TCBL User Data Manager application specific settings are grouped under `tudm`.

#### TCBL services

The TCBL services are maintained in a file outside the main configuration file,
but the name of that file is defined in the main configuration file.

An example of such a TCBL services file can be found at `tcbl-user-data-manager/src/main/resources/services.json.dist`.

Note that the published TCBL services can be refreshed after an update to the TCBL services file
without restarting the application by navigating to the application's TCBL Services endpoint,
with an additional refresh parameter set to true:
```
/usermanager/services?refresh=true
``` 

#### TCBL privacy URL

The TCBL privacy is declared on an external webpage, whose URL is defined in the main configuration file.

#### Synchronise user data

When set to `true`, it causes to copy all user data found in the Gluu Server to the user database (see below) when the
TCBL User Data Manager starts. This is intended as a one-time operation, so unless really necessary, leave this set to
`false`!

#### MailChimp

The TCBL User Data Manager application can communicate if a user wants to be subscribed to the TCBL newsletter, or the
other way around: if a user unsubscribes via MailChimp, these changes will propagate to the data manager application.

The main configuration file defines the name of a properties file that contains the actual settings for MailChimp.

That properties file **will be read every time** a request to the MailChimp API is made.
This way you can re-configure the settings without having to restart the application.
Use the file `tcbl-user-data-manager/src/main/resources/mailchimp.properties.dist` as a template and store it as `mailchimp.properties`
in the working directory. The file is self-explanatory.

To **disable** MailChimp communication, set the `mailchimp.filename` property to point to an non-existing file.

#### Activity logging

Application events can be sent to an activity logging **endpoint**.
At time of writing this is 'http://api.behaviourkit.tocker.iminds.be/logs'.
Will become https as soon as possible.

Access to that endpoint requires a **JWT key**, defined per platform.
At time of writing this key can be obtained at 'http://admin.behaviourkit.tocker.iminds.be/platforms'.
Our platform is named 'USERMANAGER' (during testing: 'USERMANAGER_TEST').

Activity logging is disabled if either of the above settings is not defined.

To avoid unwanted application delay in case of a slow responding logging endpoint, a **timeout** can be set.

All these parameters can be set in de main configuration file.
 
#### A configuration snippet example, grouping all TCBL User Data Manager application specific settings

```yaml
##
# TCBL User Data Manager application specific settings
##
tudm:
  tcbl-services:
    # name of the json file containing the descriptions of the TCBL services
    filename: services.json

  # url of the webpage containing the TCBL privacy declaration
  tcbl-privacy-url: "https://tcbl.eu/privacy"

  # should we copy the user data from the Gluu server to the local database at boot-time? Set this to false unless
  # this is necessary for some reason.
  sync-userdata-at-boot: false

  mailchimp:
    # name of the properties file containing the mailchimp settings
    filename: mailchimp.properties

  activity-logging:
    # activity logging will be disabled if endpoint and/or jwtkey below is missing
    # where to post our activity logging
    endpoint: "https://hostname-of-api-behaviourkit/logs"
    # JWT key of our platform
    jwtkey: "JWT key value obtained for the user manager platform in the admin pages of the behaviourkit"
    # optional timeout in milliseconds; avoids unnecessary waiting after posts
    timeout: 500
```


### 8. Putting it all together: the complete application configuration

**Putting it all together**, your client configuration file `application.yml` should look like `tcbl-user-data-manager/src/main/resources/application.yml.dist`.

Always start your copy from the contents found there.
 
Of course, change ports, host names and other details to your needs.

## Running

After you start the application, you will be able to enjoy it by browsing to its URL, for example:
`https://ravel.elis.ugent.be/usermanager`.

There are a few options to start the application:

### a. Using the packaged jar (production)

Go to the directory of the jar.

Put your specific `application.yml` in this directory.
 
Then run with:

```
java -jar UserDataManager-<version>.jar
```

### b. Using maven (development)

Go to the root directory of the project.

Put your specific `application.yml` in this directory.

Then run with:

```
mvn spring-boot:run
```

Remark: will also work if `application.yml` is in `src/main/resources` (next to `application.yml.dist`),
but this is descouraged, because it would lead to unwanted packaging of this `application.yml` into the jar file when building it!

### c. In IntelliJ IDEA

IntelliJ IDEA supports Spring Boot apps out of the box. Navigate to `be.ugent.idlab.tcbl.userdatamanager.TCBLUserDataManager`, right-click on
the class name or the `main` function and create an application.

Put your specific `application.yml` in the root directory of the project.

Ready to run!

Same remark as for running using maven.
