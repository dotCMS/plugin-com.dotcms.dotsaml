# dotCMS SAML Plugin Installation (beta)

dotCMS SAML plugin takes interaction between three entities:
- Server Administrator: Usually is Linux or Windows, meaning where the dotCMS is running)
- dotCMS Administrator: The administrator of the dotCMS application.
- idP Administrator: The person in charge of the idP.

Terminology:

| Acronym 				| Meaning 																	|
|-----------------------|---------------------------------------------------------------------------|
| idP					| Identity Provider															|
| SP					| Service Provider(on our case dotCMS instance)								|
| SP Issuer URL			| dotCMS URL where SAML will be authenticating								|
| SP Endpoint Hostname	| Hostname where dotCMS will publishes the Services without **htpps://**	|

**IMPORTANT:** Please notice this plugin works on *dotCMS 5.1.6* and *Tomcat 8.5.32*

## Installing the plugin
For dotCMS cloud clients this part is done by dotCMS support team.
#### 1. Get the right version
Download the required version of the plugin from [the repo](https://github.com/dotCMS/plugin-com.dotcms.dotsaml). Checkout the latest [tag](https://github.com/dotCMS/plugin-com.dotcms.dotsaml/tree/5.x).

#### 2. Deploy plugin
* Copy the **plugin** tarball to `/plugins` in the dotCMS installation
* Uncompress the file (no need to change plugin’s folder name)
* Re-deploy the plugins (stop, deploy plugins and start dotCMS)

#### 3. Add the SAML Configuration portlet
* Log into de backend
* Go to System>Roles & Tools
* Search for and select “CMS Administrator”
* Go to “Tools” tab
* Click on “System”
* Search for and select “SAML Configuration” in the “Tool” drop down
* Click on “Add” and then click on “Save”
* Make sure the portlet System > SAML Configuration is available

## Configure SAML

The configuration of SAML have to be done by the dotCMS Administrator, cooperating with the idP Administrator.

#### Add SAML Configuration:

First step is to fill up the configuration in order to Generate the SP Metadata which it will be used by the idP Administrator to get back with the idP Metadata file, so basically:
1. dotCMS Amin ---provides--> **SP Metadata** -----to--> idP Admin
2. idP Admin -------provides--> **idP Metadata** -----to--> dotCMS Admin

##### Step 1
Here is the information required by the configuration and the ownership on Step 1, on In the **SAML Configuration** portlet, click on **Add New SAML Configuration**:

+ ###### Configuration Name
	- Value: any

+ ###### Enabled?
	- Value: Yes/no According dotCMS admin

+ ###### SP Issuer URL
	- Value: dotCMS URL where SAML will be authenticating (typically the server's hostname)

+ ###### SP Endpoint Hostname
	- Value: This is going to be the hostname for the URLs where our SP publishes services available on dotCMS side for the IdP to communicate with dotCMS. The hostname does not need to be a site hosted in dotCMS, but it needs to point to the dotCMS instance, do not add the **“https://”** it will be added automatically. 

+ ###### Private Key and Public Cert
	- Value: Generate the cert and ley running the following command on Linux like OS: 

		`openssl req -x509 -newkey rsa:4096 -sha256 -nodes -keyout <key file name> -out <certificate file name> -days 3650`

+ ###### Validation Type
	- Value: Use **Response and Assertion** or change according idP Admin.

+ ###### Override the properties
	- Value: left empty or change as idP Admin requires. For example, default properties can be:
```
nameidpolicy.format=urn:oasis:names:tc:SAML:2.0:nameid-format:transient
protocol.binding=urn:oasis:names:tc:SAML:2.0:bindings:HTTP-POST
verify.assertion.signature=false
```

+ ###### Sites
	- Value: Don't associate any Site for now.

+ ###### Share SP Metadata
	- Save the configuration and click on **Download SP Metadata** to get the SP Metadata link, provide that link to the idP Admin and wait for the idP Metadata.


##### Step 2
Once that the idP admin has provided the idP Metadata, proceed to following steps by click on **Edit** on the SAML Configuration added before

+ ###### idP Metadata File
	- Value: Get the IdP metadata link or file from the IdP administrator (This may not be available until after you have shared the SP metadata file with the IdP administrator, in previous step) and upload to **IdP metadata file**, .

+ ###### Sites
	- Value: Add the dotCMS sites that will be using this SAML Configuration to authenticate users

+ ###### Enabled
	- Value: Change **Enabled?** to **Yes**

+ ###### Save the configuration
	Edit any other setup as idP admin has required, then save configuration and test.

## Test
* From a different browser session, try to log into the backend via hostname of one of the sites you associated to the SAML (did this previously)
* Instead of being redirected to the dotCMS default login page, you should hit the IdP login page
* Monitor `dotcms-saml.log` (inside dotCMS’s log directory) for specific messages related to SAML authentication

---

## Troubleshooting SAML Authentication Issues  
- **Unable to login after enabling SAML:** Something went wrong with the IdP configuration and I can’t log into the backend anymore. Access still can be granted by adding `?native=true` to the url when accessing the backend. For example: https://dotcms.com/dotAdmin?native=true
