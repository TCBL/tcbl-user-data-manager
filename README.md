# TCBL User Data Manager.

This application allows to manage user data as stored in a Gluu Server.
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

### 1. Basic Application Container configuration

Every Spring Boot app runs on a servlet container (either Tomcat or Jetty; Tomcat by default).
This requires configuration of the port the server will listen to. Besides that, our application
will be served with context `/usermanager`, meaning that this will be the root of our application on the URL, and that
redirect URL's are generated correctly (especially in a reverse proxy configuration).

```yaml
server:
  port: 8444
  servlet:
    context-path: /usermanager
```

There are two options to let the outside world communicate with the application, and both have their configuration implications:
1. Direct communication (no reverse proxy). See **1.a Enable HTTPS for the application**. Typical for testing.
1. Via a reverse proxy. See **1.b. Configure a reverse proxy**. Typical for a production environment.

### 1.a Enable HTTPS for the application (optional)

*Only when run as a standalone application. You can also run it in combination with a reverse proxy that provides a
certificate. In this case, see **1.b. Configure a reverse proxy**.*

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
  port: 8444
  servlet:
    context-path: /usermanager
  ssl:
    key-store: /home/ghaesen/projects/TCBL/config/tudm.jks
    key-store-password: secret
    key-store-type: PKCS12
    key-alias: tudm
```

*The complete configuration is discussed in "4. Configure the application"*

### 1.b Configure a reverse proxy (optional)
In this scenario, you already have an HTTP server installed which provides the TLS certificate and serves some contents.
We have to configure the HTTP server to act as a reverse proxy, which means requests will be forwarded to another URL.
We take Apache 2.4 as HTTP server, because that's the one used on the TCBL test and production servers.
Our application uses AJP in stead of HTTP for communication with the proxy because then it pretends to be part of the
HTTP (proxy) server. It is more efficient and automatically takes care of rewrites, header passing, etc.

The set-up is:
* Our app listens on `ajp://127.0.0.1:8445/usermanager`.
* Apache redirects requests originating from `/usermanager` to `http://127.0.0.1:8445/usermanager`

For example, on the test server, this means that requests to `https://tcblsso2.ilabt.imec.be:8443/usermanager` reach our app.

First, configure Apache on how to act as a reverse proxy, as described [here](https://git.datasciencelab.ugent.be/TCBL/internal-server-docs/wikis/apache#reverse-proxy).

The app requires some configuration too:

```yaml
server:
  port: 8444
  servlet:
    context-path: /usermanager
  ajp:
    port: 8445
    scheme: https
    proxy-name: tcblsso2.ilabt.imec.be
    proxy-port: 8443
    secure: true
```
Here is what the parameters do:
* **port**: port where the *application server* listens at (i.e. our application).
* **scheme**: the scheme of the incoming requests *on the proxy*, what the outside world uses.
* **proxy-name**: the name of the host as known by the outside world.
* **proxy-port**: the port reachable for the outside world.
* **secure**: do we trust requests to the application server? In this case we do, because the proxy uses https and runs
on the same host.

More info about these parameters [here](https://tomcat.apache.org/tomcat-8.5-doc/config/ajp.html).

*The complete configuration is discussed in "4. Configure the application"*

### 2. Register the client on the server

**Before registering, make sure there is a scope `inum` with a claim `inum` created on the server!**

This is a manual registration because Spring Security Oauth2 doesn't support dynamic registration for OpenID Connect yet.
Here are the settings (example, adjust to correct hosts):

* Client Name: TCBL_manager
* Application Type: Web
* Pre-Authorization: True (this skips the authorization step, since it makes no sence for this application)
* Persist Client Authorizations: False
* Logo URI: https://tcblsso.ilabt.iminds.be:8443/resources/logos/login-with-TCBL.png (though it won't be shown)
* Subject Type: pairwise
* Authentication method for the Token Endpoint: client_secret_post
* Redirect Login URIs: https://ravel.elis.ugent.be:8444/usermanager/oauth2/authorize/code/tcbl_manager (or wherever the app lives, on the test server, this would be https://tcblsso2.ilabt.imec.be:8443/usermanager/oauth2/authorize/code/tcbl_manager)
* Scopes: openid, inum
* Response Types: code
* Grant Types: authorization_code

For the client, this requires the following configuration snippet (example):

```yaml
security:
  oauth2:
    client:
      tcbl-manager:
        client-id: "@!4F1B.EBA3.75E2.F47A!0001!EF35.6902!0008!1B4C.7A50.7F55.50D7"
        client-secret: averysecrativesecret
        client-authentication-method: post
        authorized-grant-type: authorization_code
        redirect-uri: "https://ravel.elis.ugent.be:8444/usermanager/oauth2/authorize/code/tcbl_manager"
        scopes: openid, inum
        authorization-uri: "https://honegger.elis.ugent.be/oxauth/seam/resource/restv1/oxauth/authorize"
        token-uri: "https://honegger.elis.ugent.be/oxauth/seam/resource/restv1/oxauth/token"
        jwk-set-uri: "https://honegger.elis.ugent.be/oxauth/seam/resource/restv1/oxauth/jwks"
        user-info-uri: "https://honegger.elis.ugent.be/oxauth/seam/resource/restv1/oxauth/userinfo"
        client-name: TCBL_manager
        client-alias: tcbl-manager
```

### 3. SCIM configuration

Follow the instructions of [1. Preparations](https://git.datasciencelab.ugent.be/TCBL/internal-server-docs/wikis/scim-2-0-setup#1-preparations) of
chapter **Setting up a SCIM 2.0 client** from the Scim 2.0 wiki page. In this page, the client is this application.

Skip **2 Setting up the client application**.

Now you should have a client ID, a jks file (containing the certificate from the host running the Gluu Server) and the password of the jks file.
The client key id can be left empty since there is only one key in the store.

This requires the following configuration snippet (example):

```yaml
security:
  scim:
    domain: "https://honegger.elis.ugent.be/identity/seam/resource/restv1"
    meta-data-url: "https://honegger.elis.ugent.be/.well-known/uma-configuration"
    aat-client-id: "@!4F1B.EBA3.75E2.F47A!0001!EF35.6902!0008!224B.6C55"
    aat-client-jks-path: /home/ghaesen/projects/TCBL/config/scim-rp-honegger.jks
    aat-client-jks-password: secret
    aat-client-key-id:
```

### 4. Configure the application

Putting it all together, the client configuration file should look like `tcbl-user-data-manager/src/main/resources/application.yml.dist`. Example:

```yaml
####
#
# This is a sample configuration. Copy this into application.yml, adapt to your needs and place it into the working directory
# or next to the built jar file.
#
####

server:
  port: 8443
  # -- either
  ssl:
    key-store: /home/ghaesen/projects/TCBL/config/tudm.jks
    key-store-password: secret
    key-store-type: PKCS12
    key-alias: tudm
  # -- or
  ajp:
    port: 8445
    scheme: https
    proxy-name: ravel.elis.ugent.be
    proxy-port: 443
    secure: true

logging:
  level:
    root: info
    be.ugent.idlab: debug
    org.springframework.web: warn
    org.springframework.security: warn

spring:
  thymeleaf:
    # test environment:
    cache: false
    # production environment:
    # cache: true
    # this prefix setting has to do with https://github.com/spring-projects/spring-boot/issues/1744 !
    prefix: classpath:/templates

security:
  oauth2:
    client:
      # config for client on honegger
      tcbl-manager:
        client-id: "@!4F1B.EBA3.75E2.F47A!0001!EF35.6902!0008!1B4C.7A50.7F55.50D7"
        client-secret: averysecrativesecret
        client-authentication-method: post
        authorized-grant-type: authorization_code
        redirect-uri: "https://ravel.elis.ugent.be:8443/usermanager/oauth2/authorize/code/tcbl_manager"
        #test server:
        #redirect-uri: "https://tcblsso2.ilabt.imec.be:8443/usermanager/oauth2/authorize/code/tcbl_manager"
        scopes: openid, inum
        authorization-uri: "https://honegger.elis.ugent.be/oxauth/seam/resource/restv1/oxauth/authorize"
        token-uri: "https://honegger.elis.ugent.be/oxauth/seam/resource/restv1/oxauth/token"
        user-info-uri: "https://honegger.elis.ugent.be/oxauth/seam/resource/restv1/oxauth/userinfo"
        jwk-set-uri: "https://honegger.elis.ugent.be/oxauth/seam/resource/restv1/oxauth/jwks"
        client-name: TCBL_manager
        client-alias: tcbl-manager
  scim:
    domain: "https://honegger.elis.ugent.be/identity/seam/resource/restv1"
    meta-data-url: "https://honegger.elis.ugent.be/.well-known/uma-configuration"
    aat-client-id: "@!4F1B.EBA3.75E2.F47A!0001!EF35.6902!0008!224B.6C55"
    aat-client-jks-path: /home/ghaesen/projects/TCBL/config/scim-rp-honegger.jks
    aat-client-jks-password: secret
    aat-client-key-id:
```

Of course, change ports and host names accordingly.

## Running

After you start the application, you will be able to enjoy it by browsing to its URL, for example:
`https://ravel.elis.ugent.be:8443/usermanager`.

There are a few options to start the application:

### a. Using the packaged jar (production)

Go to the directory of the jar.

Copy src/main/resources/application.yml.dist into this directory. Then run with:

```
java -jar UserDataManager-<version>.jar
```

### b. Using maven (development)

Copy src/main/resources/application.yml.dist to src/main/resources/application.yml and adapt to your needs.

In the root directory of the project, type:

```
mvn spring-boot:run
```

### c. In IntelliJ IDEA

IntelliJ IDEA supports Spring Boot apps out of the box. Navigate to `be.ugent.idlab.tcbl.userdatamanager.TCBLUserDataManager`, right-click on
the class name or the `main` function and create an application.

Copy src/main/resources/application.yml.dist to src/main/resources/application.yml and adapt to your needs.

Ready to run!
