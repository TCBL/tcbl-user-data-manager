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

### 1. Enable HTTPS for the application

In order to use OpenID Connect, the application needs to be able to receive https requests.
Two options here: using a self-signed certificate or a certificate from a Certificate Authority.

#### a. Self-signed

Use `keytool` (shipped with a JDK or JRE) to generate a certificate and store it in a keystore.
You can choose the alias (`tudm`) and the name of the keystore (`tudm.p12`) as you whish, but remember them for later.

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

If you already have a certificate (chain), find the .crt file. Then import it into the keystore:

```
/usr/lib/jvm/java-8-openjdk-amd64/bin/keytool -importcert -file the_certificate.crt -alias tudm -keystore tudm.jks

```

This results in the following configuration snippet (example):

```yaml
server:
  port: 8443
  ssl:
    key-store: /home/ghaesen/projects/TCBL/config/tudm.jks
    key-store-password: secret
    key-store-type: PKCS12
    key-alias: tudm
```

*The complete configuration can be viewed in "4. Configure the application"*

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
* Redirect Login URIs: https://ravel.elis.ugent.be:8443/oauth2/authorize/code/tcbl_manager (or wherever the app lives)
* Scopes: openid, inum
* Response Types: code
* Grant Types: authorization_code

For the client, this results in the following configuration snippet (example):

```yaml
security:
  oauth2:
    client:
      tcbl-manager:
        client-id: "@!4F1B.EBA3.75E2.F47A!0001!EF35.6902!0008!1B4C.7A50.7F55.50D7"
        client-secret: averysecrativesecret
        client-authentication-method: post
        authorized-grant-type: authorization_code
        redirect-uri: "https://ravel.elis.ugent.be:8443/oauth2/authorize/code/tcbl_manager"
        scopes: openid, inum
        authorization-uri: "https://honegger.elis.ugent.be/oxauth/seam/resource/restv1/oxauth/authorize"
        token-uri: "https://honegger.elis.ugent.be/oxauth/seam/resource/restv1/oxauth/token"
        user-info-uri: "https://honegger.elis.ugent.be/oxauth/seam/resource/restv1/oxauth/userinfo"
        user-info-converter: org.springframework.security.oauth2.client.user.converter.UserInfoConverter
        client-name: TCBL_manager
        client-alias: tcbl-manager
```

### 3. SCIM configuration

Follow the instructions of [1. Preparations](https://git.datasciencelab.ugent.be/TCBL/internal-server-docs/wikis/scim-2-0-setup#1-preparations) of
chapter **Setting up a SCIM 2.0 client** from the Scim 2.0 wiki page. In this page, the client is this application.

Skip **2 Setting up the client application**.

Now you should have a client ID, a jks file (containing the certificate from the host running the Gluu Server) and the password of the jks file.
The client key id can be left empty since there is only one key in the store.

This results in the following configuration snippet (example):

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

Putting it all together, the client congfiguration ashould look like in `tcbl-user-data-manager/src/main/resources/application.yml`. Example:

```yaml
####
#
# This is a sample configuration. Copy this and place next to the application jar file to override.
#
####

server:
  port: 8443
  ssl:
    key-store: /home/ghaesen/projects/TCBL/config/tudm.jks
    key-store-password: secret
    key-store-type: PKCS12
    key-alias: tudm

logging:
  level:
    root: info
    be.ugent.idlab: debug
    org.springframework.web: warn
    org.springframework.security: warn

spring:
  thymeleaf:
    cache: false

security:
  oauth2:
    client:
      # config for client on honegger
      tcbl-manager:
        client-id: "@!4F1B.EBA3.75E2.F47A!0001!EF35.6902!0008!1B4C.7A50.7F55.50D7"
        client-secret: averysecrativesecret
        client-authentication-method: post
        authorized-grant-type: authorization_code
        redirect-uri: "https://ravel.elis.ugent.be:8443/oauth2/authorize/code/tcbl_manager"
        scopes: openid, inum
        authorization-uri: "https://honegger.elis.ugent.be/oxauth/seam/resource/restv1/oxauth/authorize"
        token-uri: "https://honegger.elis.ugent.be/oxauth/seam/resource/restv1/oxauth/token"
        user-info-uri: "https://honegger.elis.ugent.be/oxauth/seam/resource/restv1/oxauth/userinfo"
        user-info-converter: org.springframework.security.oauth2.client.user.converter.UserInfoConverter
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

### 1. Using the packaged jar (production)

Put a (modified) `application.yml` file besides the jar. The run it with

```
java -jar UserDataManager-<verison>.jar
```

### 1. Using maven (development)
In the root directory of the project, type:

```
mvn spring-boot:run
```

It uses the configuration file in the source tree!

### 2. In IntelliJ IDEA
IntelliJ IDEA supports Spring Boot apps out of the box. Navigate to `be.ugent.idlab.tcbl.userdatamanager.TCBLUserDataManager`, right-click on
the class name or the `main` function and create an application. Ready to run!

It uses the configuration file in the source tree!
