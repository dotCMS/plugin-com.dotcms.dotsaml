# plugin-dotcms-openSAML3

This plugin allows to modify the authentication process in DOTCMS
using the Open SAML 3 (Security Assertion Markup Language) protocols for
frontend, backend or both.

The plugin will add the user in dotcms if the user doesn't exist
and for every user logging from SAML the ROLEs will be reassigned if the roles 
are sent by the SAML response message.

The SAML Response should always send the user email, firstname and
lastname. The roles are optional


########################################
##  CONFIGURATION
########################################

1) Set in the DOTCMS_plugin_path/conf/dotmarketing-config-ext.properties file the
sites-config.json path service provider, it supports several sites and each site can be associated to an IDP. 
The Plugin includes some examples, however you can take a look to DOTCMS_plugin_path/src/com/dotcms/plugin/saml/v3/DotSamlConstants.java, there you can find
all the properties you can override in the configuration (we will explain all of them later).
All properties described below are for the sites-config.json per site, except those ones mark with a Note for dotmarketing-config.

Here an example of sites-config.json

~~~
{
  "config": [
    {
      "saml-test.dotcms.com" : {
        "default":"true",
        "dotcms.saml.service.provider.issuer": "https://saml-test-.dotcms.com/",
        "dotcms.saml.keystore.path":"file:///Users/dotcms/dotcms_3.5/plugins/plugin-dotcms-openSAML3/conf/SPKeystore.jks",
        "dotcms.saml.keystore.password":"password",
        "dotcms.saml.keyentryid":"SPKey",
        "dotcms.saml.keystore.entry.password":"password",
        "dotcms.saml.assertion.customer.endpoint.url":"https://saml-test.dotcms.com/dotsaml3sp",
        "dotcms.saml.idp.metadata.path":"file:///Users/dotcms/dotcms_3.5/plugins/plugin-dotcms-openSAML3/conf/idp1-metadata.xml",
        "dotcms.saml.want.assertions.signed":"false",
        "dotcms.saml.authn.requests.signed":"true",
        "dotcms.saml.assertion.resolver.handler.classname":"com.dotcms.plugin.saml.v3.handler.HttpPostAssertionResolverHandlerImpl",
        "dotcms.saml.protocol.binding":"urn:oasis:names:tc:SAML:2.0:bindings:HTTP-POST"
      }
    },
    {
      "site2.dotcms.com": {
        "dotcms.saml.service.provider.issuer": "https://so2.dotcms.com/",
        "dotcms.saml.keystore.path": "file:///Users/dotcms/dotcms_3.5/plugins/plugin-dotcms-openSAML3/conf/SPKeystore.jks",
        "dotcms.saml.keystore.password": "password",
        "dotcms.saml.keyentryid": "SPKey",
        "dotcms.saml.keystore.entry.password": "password",
        "dotcms.saml.artifact.resolution.service.url": "https://so2.localdomain:8443/idp/profile/SAML2/SOAP/ArtifactResolution",
        "dotcms.saml.assertion.customer.endpoint.url": "https://so2.dotcms.com/dotsaml3sp",
        "dotcms.saml.idp.metadata.path": "file:///Users/dotcms/dotcms_3.5/plugins/plugin-dotcms-openSAML3/conf/idp2-metadata.xml",
        "dotcms.saml.want.assertions.signed": "false",
        "dotcms.saml.authn.requests.signed": "true"
      }
    }
  ]
}
~~~

Here we have two sites (note the root level item is the servername), 
the first one is associated to the Idp 1 and it is expecting a Http Post back with a SAMLResponse. This is also set as a 
default configuration, which means that any other site without configuration will use the saml-test.dotcms.com

The second site is configurated to be used with an artifact resolved. The binding and handler are not necessary since they are the 
default ones.

2) By default we have included the SPKeystore.jks, however you should use/create your own key store file.
You should take in consideration that the keystore should have a certificate. Here you
could see and example of how you can create one
http://blog.tirasa.net/category/codeexp/security/create-a-new-keystore-to.html

In addition here is an example of the properties to override:

~~~
"dotcms.saml.keystore.path":"SPKeystore.jks",
"dotcms.saml.keystore.password":"password",
"dotcms.saml.keyentryid":"SPKey",
"dotcms.saml.keystore.entry.password":"password"
~~~

Keep in mind that the dotcms.saml.keystore.path, could be get from the app classpath or from the file system;
To include a file system just include the prefix file://

For instance:
~~~
"dotcms.saml.keystore.path":"file:///opt/keystores/myKeystore.jks"
~~~

3) Setting up more configuration:

3.1) dotcms.saml.protocol.binding

By default dotCMS used org.opensaml.saml.common.xml.SAMLConstants.SAML2_ARTIFACT_BINDING_URI, the binding tells to the Idp how the SP is expecting the response.
The default one just wait for SAMLArt parameter with the Artifact Id to Resolve the Artifact via artifact resolver, we have also support for 
urn:oasis:names:tc:SAML:2.0:bindings:HTTP-POST, this one expects a SAMLResponse as part of a post-back witht the Assertion response.

3.2) dotcms.saml.identity.provider.destinationsso.url

This is url for the login page on the Shibboleth Server, by default it gets url from the idp-metadata (the file provided from the Shibboleth server), but if it is not any idp-metadata you can
edit this property and include the SSO url. (Note, if you set this property and set the idp-metadata, the idp-metada will be get by default)

3.3) dotcms.saml.artifact.resolution.service.url

This is an optional property for the app and it is the SOAP URL for the Artifact Resolution Service (the one that gets the user information, the Assertion).
If you use HTTP-POST binding do not need to specified this value.

3.4) dotcms.saml.assertion.customer.endpoint.url

This is the URL where the Idp (the Shibboleth server) will be redirected to dotCMS when the login is made, we suggest to go to http://[domain]/dotsaml3sp.
If this value is not set, will be send a current request as a default, however keep in mind some Idp Server might not admit this behaviour.

3.5) dotcms.saml.service.provider.issuer

This is the App Id for the DotCMS Service Provider, by default we use this one: "com.dotcms.plugin.saml.v3.issuer", we recommend to use you url.com address, for instance:

http://www.dotcms.com, could be the dotCMS id.

3.6) dotcms.saml.policy.allowcreate

By default dotCMS plugin advise to not allow to create new user on the Idp, however you can advise the value you want (true or false) overriding the value in the properties file.

3.7) dotcms.saml.policy.format

By default we support TRANSIENT and PERSISTANCE formats, however if you want to override it just add the values (comma separated) in the properties file.
See org.opensaml.saml.saml2.core.NameIDType for more details about the valid values.

3.8) dotcms.saml.authn.comparisontype

By default we use a MINIMUM Authorization, But you can switch to another one; for instance:

dotcms.saml.authn.comparisontype=BETTER

MINIMUM

The user could be authenticated by using password or any stronger method, such as smart card for instance.

BETTER

The user must be authenticated with a stronger method than password.

EXACT

The user will be authenticated with a specific method assigment for it, for instance if it is password, the user will be authenticated by password, not anything else.

MAXIMUM

The user will use the strong possible method.

3.9) dotcms.saml.authn.context.class.ref

This is the authentication context, it could be Kerberos, it could be Internet protocol, password, etc. See org.opensaml.saml.saml2.core.AuthnContext for more details.
By default we use: org.opensaml.saml.saml2.core.AuthnContext.PASSWORD_AUTHN_CTX

3.10) dotcms.saml.keystore.path

Class path or file system path for the key store, we have a dummy KeyStore called SPKeystore.jks on the plugin however it is highly recommend to create/use your own store.

3.11) dotcms.saml.keystore.password

Password to access the key store

3.12) dotcms.saml.keyentryid

This is the key entry for the key store, by default we use SPKey, you can override it if needed.

3.13) dotcms.saml.keystore.entry.password

This is the key entry password for the key store, by default we use "password", you can override it if needed.

3.14) dotcms.saml.keystore.type

By default dotCMS use java.security.KeyStore.getDefaultType(), however if you key store is a type different, you can override it here.

3.15) dotcms.saml.signature.canonicalization.algorithm

By default we use  org.opensaml.xmlsec.signature.support.SignatureConstants.ALGO_ID_C14N_EXCL_OMIT_COMMENTS, you can override it if needed.

3.16) dotcms.saml.clock.skew and dotcms.saml.message.life.time

DotCMS does validation for the message lifetime, by default the clock skew is 1000 and life time 2000, in case you need a greater value feel free to override it.

3.17) dotcms.saml.remove.roles.prefix

Depending on your Identity providers on the IdP, the roles may be returned on the assertion with a prefix, you can remove it by setting it on the dotCMS properties.

3.18) dotcms.saml.email.attribute

By default "mail" is the field used to fetch the user email from the Idp response, however if you are using another one you can override it on the properties.

3.19) dotcms.saml.firstname.attribute

By default "givenName" is the field used to fetch the user name from the Idp response, however if you are using another one you can override it on the properties.

3.20) dotcms.saml.lastname.attribute

By default "sn" is the field used to fetch the last name from the Idp response, however if you are using another one you can override it on the properties.

3.21) dotcms.saml.roles.attribute

By default "authorisations" is the field used to fetch the roles/groups from the Idp response, however if you are using another one you can override it on the properties.

3.22) dotcms.saml.initializer.classname

NOTE: this property is for the dotmarketing-config.properties

By default dotcms use: DefaultInitializer it inits the Java Crypto, Saml Services and plugin stuff.
However if you have a custom implementation of Initializer, you can override by adding a full class name to this property.

3.23) dotcms.saml.configuration.classname

By default we use com.dotcms.plugin.saml.v3.config.DefaultDotCMSConfiguration to handle the plugin configuration,
However if you have a custom implementation of Configuration, you can override by adding a full class name to this property.

3.24) dotcms.saml.idp.metadata.path

In case you have a idp-metadata.xml you can get it from the classpath or file system.
For the classpath you overrides the property with the right path in your class path.
If you want to get the XML from the file system use the prefix; file://

3.25) dotcms.saml.idp.metadata.protocol

This is the attribute name to find the Idp Information on the idp-metadata.xml (the file provided from the Shibboleth server), the default used is
"urn:oasis:names:tc:SAML:2.0:protocol", probably you do not need to change it but if you can override it here if needed.

3.26) dotcms.saml.idp.metadata.parser.classname

By default dotCMS use DefaultMetaDescriptorServiceImpl, this class parse the idp-metadata and creates the sp-metadata from the runtime information.
However if you have a custom implementation of MetaDescriptorService, you can override by adding a full class name to this property.

3.27) dotcms.saml.access.filter.values

By default dotCMS does not filter any url, however if you want to avoid to check open saml authentication over any URL please add (comma separated) the list of
urls on the properties file.

3.28) dotcms.saml.service.provider.custom.credential.provider.classname

In case you need a custom credentials for the Service Provider (DotCMS) overrides the implementation class on the configuration properties.
Please see com.dotcms.plugin.saml.v3.CredentialProvider

3.29) dotcms.saml.id.provider.custom.credential.provider.classname

In case you need a custom credentials for the ID Provider (DotCMS) overrides the implementation class on the configuration properties.
Please see com.dotcms.plugin.saml.v3.CredentialProvider

3.30) dotcms.saml.want.assertions.signed

By default true, overrides it if you want the assertions signed or not (true or false).

3.31) dotcms.saml.authn.requests.signed

By default true, overrides it if you want the authorization requests signed or not (true or false).

3.32) dotcms.saml.sevice.provider.custom.metadata.path

By default this is the URL to get the dotCMS Service Provider metadata: "/dotsaml3sp/metadata.xml"
However if you want to use a different path, feel free to override it on the properties file.

3.33) dotcms.saml.assertion.resolver.handler.classname

By default we use the implementation com.dotcms.plugin.saml.v3.handler.SOAPArtifactAssertionResolverHandlerImpl
which is in charge of resolve the assertion using the SOAP artifact resolver based on the artifact id pass by the request.

If you want a different implementation please override with the class here.
We also offer: com.dotcms.plugin.saml.v3.handler.HttpPostAssertionResolverHandlerImpl which is in charge of processing a HTTP-POST witha SAMLResponse

3.34) dotcms.saml.sites.config.path

NOTE: this property is for the dotmarketing-config.properties

This contains the path to resolve the sites-config.jso with the configuration per site.

3.35) dotcms.saml.include.roles.pattern

This is an array comma separated, if this array is set. Any role from SAML that does not match with the list of include roles pattern, will be filtered.

For instance:

~~~
"dotcms.saml.include.roles.pattern":"^www_,^xxx_"
~~~

The previous example will include only the roles from SAML that starts with www_ or xxx_ 


3.36) dotcms.saml.include.path.values

By default we include:
~~~
 ^/dotsaml3sp*$, ^/dotCMS/login.*$, ^/html/portal/login.*$, ^/c/public/login.*$,^/c/portal_public/login.*$,^/c/portal/logout.*$", 
~~~

If you need to add more into the saml filter you can include the values comma separated.

For instance:
~~~
"dotcms.saml.include.path.values":"^/html/portal/login.*$,^/dotCMS/login.*$,^/c/,^/admin"
~~~


4) The plugin needs several libraries to run, all of them has been renamed with a prefix called: "opensaml". In case you need to undeploy the plugin you have to manually remove these libraries from the 
 /dotserver/tomcat-8.0.18/webapps/ROOT/WEB-INF/lib

########################################
##  HOW TO USE
########################################

To use the plugin run the ./bin/deploy-plugins.sh command and restart your 
dotCMS instance.

To see your service provider metadata by default generated by the plugin use this url:

~~~
https://<site.servername>/dotsaml3sp/metadata.xml
~~~

However you can override it for each site, on the sites-config.json
using the property: "dotcms.saml.sevice.provider.custom.metadata.path"

Any request on DotCMS will be redirect to the IdP Login Page, if the user is not already login.
The rule exception will the url's set on sites-config.json, with the property: "dotcms.saml.access.filter.values"
