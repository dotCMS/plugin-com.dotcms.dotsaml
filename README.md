# plugin-dotcms-openSAML3

This plugin allows to modify the authentication process in DOTCMS
using the Open SAML 3 (Security Assertion Markup Language) protocols for
frontend, backend or both.

The plugin will add the user in dotcms if the user doesn't exist
and for every user logging from SAML the ROLEs will be reassigned if the roles 
are sent by the SAML response message. In addition, a system role will be assigned to the user (*SAML User* role), as well as any other role defined in the configuration (for further information, refer to *saml_user_role* usage in the [Configuration](#configuration) section)

The SAML Response should always send the user email, firstname and
lastname. The roles are optional.

##  <a name="how-to-use">HOW TO USE</a>

**Before installation:** Be sure your DB schema was previously initialized (dotCMS' DB tables were created). Having started the application at least once without the plugin is enough.

To use the plugin run the ./bin/deploy-plugins.sh command and restart your 
dotCMS instance.

Once the plugin is deployed, the SAML configuration can be set for each host through the application, using the SAML field created for this purpose (Go to System --> Sites --> Edit Host)

![Edit Host](https://github.com/dotCMS/plugin-dotcms-openSAML3/blob/master/images/edit-host-saml.png)

SAML properties must be configured using key=value pairs, for example:

~~~
dotcms.saml.service.provider.issuer=https://saml.test.dotcms.com
dotcms.saml.keystore.path=file:///Users/dotcms/dotcms_3.5/plugins/plugin-dotcms-openSAML3/conf/SPKeystore.jks
dotcms.saml.keystore.password=password
dotcms.saml.keyentryid=SPKey
dotcms.saml.keystore.entry.password=password
dotcms.saml.assertion.customer.endpoint.url=https://saml.test.dotcms.com/dotsaml3sp
dotcms.saml.idp.metadata.path=file:///Users/dotcms/dotcms_3.5/plugins/plugin-dotcms-openSAML3/conf/idp1-metadata.xml
dotcms.saml.want.assertions.signed=false
dotcms.saml.authn.requests.signed=true
dotcms.saml.assertion.resolver.handler.classname=com.dotcms.plugin.saml.v3.handler.HttpPostAssertionResolverHandlerImpl
dotcms.saml.protocol.binding=urn:oasis:names:tc:SAML:2.0:bindings:HTTP-POST
~~~


For more details about properties configuration, please refer to the [Configuration](#configuration) section
 
**Important Considerations:** 

1. The plugin needs several libraries to run, all of them have been renamed with a prefix called: "opensaml". In case you need to undeploy the plugin you have to manually remove these libraries from */dotserver/tomcat-8.0.18/webapps/ROOT/WEB-INF/lib*.
2. Any request from dotCMS will be redirected to the IdP Login Page, if the user is not already logged in. An exception to this rule can be set with this property *dotcms.saml.access.filter.values*.
3. A fallback host can be defined in order to use its configuration by default. It will only apply for those hosts whose SAML configuration field is empty. This can be set in the DOTCMS_plugin_path/conf/dotmarketing-config-ext.properties file, using the *saml.fallback.site* property, like this:

	~~~
	saml.fallback.site=saml.test.dotcms.com
	~~~


##  <a name="configuration">CONFIGURATION</a>

In this section we describe all the properties that can be set in your SAML configuration:

**dotcms.saml.protocol.binding**

By default, dotCMS uses *org.opensaml.saml.common.xml.SAMLConstants.SAML2_ARTIFACT_BINDING_URI*. The binding tells to the Idp how the SP is expecting the response.
The default one just wait for SAMLArt parameter with the Artifact Id to resolve the artifact via Artifact Resolver. We also have support for 
*urn:oasis:names:tc:SAML:2.0:bindings:HTTP-POST*, this one expects a SAMLResponse as part of a post-back with the Assertion response.

**dotcms.saml.identity.provider.destinationsso.url**

URL for the login page on the Shibboleth Server. By default, it gets the url from the idp-metadata (the file provided from the Shibboleth server); in case there is not any idp-metadata you can
edit this property and include the SSO url. (Note, if you set this property as well as the idp-metadata, the idp-metadata will be get by default).

**dotcms.saml.service.provider.issuer**

App Id for the dotCMS Service Provider. In case it is not provided, the default dotCMS site name will be set, using the https protocol. We encourage to use your url.com address, for instance: http://www.dotcms.com

**dotcms.saml.assertion.customer.endpoint.url**

URL used by the Idp (the Shibboleth server) to redirect to dotCMS when the login is made. We suggest to go to http://[domain]/dotsaml3sp.
If this value is unset, a default endpoint will be created using the *dotcms.saml.service.provider.issuer* and the *dotcms.saml.keyentryid*.

**saml_user_role**
Custom role, set to the logged user during authentication process. This property is optional, however, in case to be included, this role must exist in dotCMS with the proper permissions (at least to Grant Users), otherwise will be ignored.

**dotcms.saml.policy.allowcreate**

Allows to create users that do not exist on the IdP.

We advise to not create new users on the Idp, however you can change this behavior, turning on this property.

**dotcms.saml.policy.format**

SAML Name ID policy. By default we support TRANSIENT and PERSISTANCE formats, however if you want to overwrite it just add the values (comma separated).
See *org.opensaml.saml.saml2.core.NameIDType* for more details about the valid values.

**dotcms.saml.authn.comparisontype**

Comparison rule used to evaluate the specified authentication methods. By default we use a MINIMUM Authorization. Possible values are:

*MINIMUM*

The user could be authenticated by using password or any stronger method, such as smart card for instance.

*BETTER*

The user must be authenticated with a stronger method than password.

*EXACT*

The user will be authenticated with a specific method assigned for it, for instance: if it is password, the user will be authenticated only by password.

*MAXIMUM*

The user will use the strongest possible method.

**dotcms.saml.authn.context.class.ref**

Authentication context, which could be Kerberos, Internet protocol, password, etc. See *org.opensaml.saml.saml2.core.AuthnContext* for more details.

By default we use: *org.opensaml.saml.saml2.core.AuthnContext.PASSWORD_AUTHN_CTX*

**dotcms.saml.keystore.path**

Classpath or file system path for the keystore.

**dotcms.saml.keystore.password**

Password to access the keystore.

**dotcms.saml.keyentryid**

Key entry for the keystore. By default we use SPKey, you can overwrite it if needed.

**dotcms.saml.keystore.entry.password**

Key entry password for the keystore. By default we use "password", it can be overwritten as well.

**dotcms.saml.keystore.type**

By default, *java.security.KeyStore.getDefaultType()* implementation is used.

**dotcms.saml.signature.canonicalization.algorithm**

Default value *org.opensaml.xmlsec.signature.support.SignatureConstants.ALGO_ID_C14N_EXCL_OMIT_COMMENTS*.

**dotcms.saml.clock.skew and dotcms.saml.message.life.time**

For message lifetime validation purpose. By default the clock skew is 1000 and life time 2000.

**dotcms.saml.remove.roles.prefix**

Depending on your Identity Providers on the IdP, the roles may be returned on the assertion with a prefix, you can change this behavior by turning on *dotcms.saml.remove.roles.prefix*.

**dotcms.saml.email.attribute**

By default, "mail" is the field used to fetch the user email from the Idp response.

**dotcms.saml.firstname.attribute**

By default "givenName" is the field used to fetch the user name from the Idp response, however if you are using another one you can overwrite it.

**dotcms.saml.lastname.attribute**

By default, "sn" is the field used to fetch the last name from the Idp response.

**dotcms.saml.roles.attribute**

By default, "authorizations" is the field used to fetch the roles/groups from the Idp response, however if you are using another one you can overwrite it.

**dotcms.saml.initializer.classname**

By default, dotCMS uses: *DefaultInitializer*. It inits the Java Crypto, Saml Services and plugin configuration details. It can be overwritten by specifying a fully qualified class name.

**dotcms.saml.configuration.classname**

Used to manipulate the SAML plugin configuration
Default implementation: *com.dotcms.plugin.saml.v3.config.DefaultDotCMSConfiguration*.

**dotcms.saml.idp.metadata.path**

In case you have a *idp-metadata.xml* you can get it from the classpath or file system.
For the classpath, overwrite the property with the right path in your classpath.
If you want to get the XML from the file system use the prefix *file://*.

**dotcms.saml.idp.metadata.protocol**

Attribute name used to find the Idp Information on the *idp-metadata.xml* (the file provided from the Shibboleth server). 

Default value: *"urn:oasis:names:tc:SAML:2.0:protocol"*

**dotcms.saml.idp.metadata.parser.classname**

This class parses the idp-metadata and creates the sp-metadata from the runtime information.
By default dotCMS uses *DefaultMetaDescriptorServiceImpl*. However, it can overwritten by adding a fully qualified class name to this property.

**dotcms.saml.access.filter.values**

By default, dotCMS does not filter any url, however if you want to avoid to check open saml authentication over any URL please add (comma separated) the list of urls on this property.

**dotcms.saml.service.provider.custom.credential.provider.classname**

Used to set custom credentials for the Service Provider. This property expects a fully qualified class name.  Please see *com.dotcms.plugin.saml.v3.CredentialProvider*.

**dotcms.saml.id.provider.custom.credential.provider.classname**

Used to set custom credentials for the ID Provider. This property expects a fully qualified class name. Please see *com.dotcms.plugin.saml.v3.CredentialProvider*.

**dotcms.saml.want.assertions.signed**

Default: true. Overwrite this value if you do not want assertions signed.

**dotcms.saml.authn.requests.signed**

Default: true. Overwrite this value if you do not want authorization requests signed.

**dotcms.saml.sevice.provider.custom.metadata.path**

By default this is the URL to get the dotCMS Service Provider metadata: */dotsaml3sp/metadata.xml
*. However, if you want to use a different path, feel free to overwrite it.

**dotcms.saml.assertion.resolver.handler.classname**

By default we use this implementation: *com.dotcms.plugin.saml.v3.handler.HttpPostAssertionResolverHandlerImpl*.

To overwrite it, provide a fully qualified class name.

**dotcms.saml.include.roles.pattern**

Comma separated value, used to validate roles against the patterns provided. Only matching roles will be considered.

For instance:

~~~
"dotcms.saml.include.roles.pattern":"^www_,^xxx_"
~~~

The previous example will include only the roles from SAML that start with *www_* or *xxx_*. 


**dotcms.saml.include.path.values**

Comma separated values with the regex paths to be considered by the SAML plugin.

By default we include:

~~~
 ^/dotsaml3sp*$, ^/dotCMS/login.*$, ^/html/portal/login.*$, ^/c/public/login.*$,^/c/portal_public/login.*$,^/c/portal/logout.*$", 
~~~

Use this property in case you need to filter additional paths. For instance:

~~~
"dotcms.saml.include.path.values":"^/html/portal/login.*$,^/dotCMS/login.*$,^/c/,^/admin"
~~~

**dotcms.saml.verify.assertion.signature, dotcms.saml.verify.signature.profile and dotcms.saml.verify.signature.credentials**

For signature verification purpose. Default value: true.
