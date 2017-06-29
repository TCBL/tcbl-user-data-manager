# OpenID Connect via Spring to log in on the Gluu Server.

This is a sample Spring Boot application that uses the Gluu Server to log in and display user info.
This will be our TCBL User Data Management application!

The code and documentation are based on [this sample](https://github.com/spring-projects/spring-security/tree/master/samples/boot/oauth2login)
and [this how-to](https://www.drissamri.be/blog/java/enable-https-in-spring-boot/).

## Building

Building the code is trivial using maven. Or open the `pom.xml` as a project in your IDE.

## Configuring

### 1. Get a SSL certificate.
Two options here: a self-signed certificate or a certificate from a Certificate Authority.

#### a. Self-signed

Use `keytool` (shipped with a JDK or JRE) to generate a certificate and store it in a keystore.
You can choose the alias (`tomcat`) and the name of the keystore (`keystore.p12`) as you whish, but remember them for later.

```
keytool -genkey -alias tomcat -storetype PKCS12 -keyalg RSA -keysize 2048 -keystore keystore.p12 -validity 3650
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
TODO

### 2. Configure the application

Open `OpenIDConnectTest/src/main/resources/application.yml`. Example:

```yaml
server:
  port: 8443
  ssl:
    key-store: /home/ghaesen/projects/TCBL/test_project/OpenIDConnectTest/cert/keystore.p12
    key-store-password: secret
    key-store-type: PKCS12
    key-alias: tomcat

logging:
  level:
    root: info
    org.springframework.web: debug
    org.springframework.security: debug

spring:
  thymeleaf:
    cache: false

security:
  oauth2:
    client:
      tcbl-manager-dev:
        client-id: "@!4F1B.EBA3.75E2.F47A!0001!EF35.6902!0008!1B4C.7A50.7F55.50D7"
        client-secret: averysecrativesecret
        client-authentication-method: post
        authorized-grant-type: authorization_code
        redirect-uri: "https://ravel.elis.ugent.be:8443/oauth2/authorize/code/tcbl_manager_dev"
        scopes: openid, user_name
        authorization-uri: "https://honegger.elis.ugent.be/oxauth/seam/resource/restv1/oxauth/authorize"
        token-uri: "https://honegger.elis.ugent.be/oxauth/seam/resource/restv1/oxauth/token"
        user-info-uri: "https://honegger.elis.ugent.be/oxauth/seam/resource/restv1/oxauth/userinfo"
        user-info-converter: "org.springframework.security.oauth2.client.user.converter.UserInfoConverter"
        client-name: TCBL_manager_dev
        client-alias: tcbl-manager-dev
```

First, take a look at the `server` section.
This config starts a server listening at port 8443. Then, in subsection `ssl`, configure the keystore according to the set-up
in the previous section.

Then, take a look at the `security:oauth2:client` section. Here is where the OpenID Connect client (RP) settings are defined.
In this case, there is one client, with settings to use the Gluu Server on honegger (our temporary dev machine).

## Running

### 1. Using maven
TODO

### 2. In IntelliJ IDEA
IntelliJ IDEA supports Spring Boot apps out of the box. Navigate to `be.ugent.idlab.tcbl.OAuth2LoginApplication`, right-click on
the class name or the `main` function and create an application. Ready to run!
