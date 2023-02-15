# Device Secret Authenticator Plugin


[![Quality](https://img.shields.io/badge/quality-demo-red)](https://curity.io/resources/code-examples/status/)
[![Availability](https://img.shields.io/badge/availability-source-blue)](https://curity.io/resources/code-examples/status/)

An authenticator accepting a nonce issued by the token profile for the purpose of native SSO. It is made to work similar to [OpenID Connect Native SSO](https://openid.net/specs/openid-connect-native-sso-1_0.html), but using the Hypermedia Authentication API instead of token exchange. 

This is just a demo of the capability and is not production ready.

## Configuration

Configure the token endpoint to issue a nonce together with the issued tokens. Similar to following:

```javascript
var nonceIssuer = context.getNonceTokenIssuer('default');
var nonceAttributes = {};
nonceAttributes.subject = context.subjectAttributes();
nonceAttributes.context = context.contextAttributes();
nonceAttributes.created = accessTokenData.iat;
nonceAttributes.expires = refreshTokenData.exp;
```

## Building the Plugin

You can build the plugin by issue the command `./gradlew dist`. This will produce a folder in the `build` directory with the plugin JAR file and all the dependencies needed called `authenticators.device-secret`, which can be installed.

## Installing the Plugin

To install the plugin, copy the contents of the `authenticators.device-secret` folder into `${IDSVR_HOME}/usr/share/plugins` on each node, including the admin node. For more information about installing plugins, refer to the [curity.io/plugins](https://support.curity.io/docs/latest/developer-guide/plugins/index.html#plugin-installation). 
If you are installing on the same machine, make sure that `IDSVR_HOME` is correctly exported in your shell, and iss `./gradlew deploy` to build and copy the correct files.

## More Information

Please visit [curity.io](https://curity.io/) for more information about the Curity Identity Server.

