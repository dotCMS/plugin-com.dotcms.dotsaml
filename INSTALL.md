# dotCMS SAML Plugin Installation (beta)

## Get the right version
Download the required version of the plugin from [the repo](https://github.com/dotCMS/plugin-com.dotcms.dotsaml). This plugin is currently available for dotCMS 4.3.2+, but not for the 5.x series. Checkout the latest [tag](https://github.com/dotCMS/plugin-com.dotcms.dotsaml/tree/4.0-4.3.x-beta) and the latest [branch](https://github.com/dotCMS/plugin-com.dotcms.dotsaml/tree/4.0-4.3.x). The latest branch is a work in progress but has the latest **hotfixes**.

## Deploy plugin
* Copy the **plugin** tarball to `/plugins` in the dotCMS installation
* Uncompress the file (no need to change plugin’s folder name)
* There's a known-issue with a jar that needs to be placed into tomcat's lib directory:

```bash
# Inside plugin's root directory
mv dotserver/tomcat-8.0.18/webapps/ROOT/WEB-INF/lib/slf4j-api-1.7.25.jar ${DOTCMS_HOME}/dotserver/tomcat-8.0.18/lib/
```

* Re-deploy the plugins (stop, deploy plugins and start dotCMS)

## Configure SAML Tool
* Log into de backend
* Go to System>Roles & Tools
* Search for and select “CMS Administrator”
* Go to “Tools” tab
* Click on “System”
* Search for and select “SAML Configuration” in the “Tool” drop down
* Click on “Add” and then click on “Save”
* Make sure the portlet System>SAML Configuration is available

## Add SAML Configuration:
* SP configuration
* Create Private Key/Public Certificate for the SAML Configuration, example:

```bash
openssl req -x509 -newkey rsa:4096 -sha256 -nodes -keyout <key file name> -out <certificate file name> -days 3650
```

* In the “SAML Configuration” portlet, click on “Add New SAML Configuration”
* Enter a configuration name
* Keep the value “Enabled? = No”
* Enter SP Issuer URL
* This is going to be the SP’s entityID (typically the server’s hostname)
* Enter SP Endpoint Hostname
* This is going to be the hostname for the URLs where our SP publishes services available on our side for the IdP to 10. communicate with dotCMS. Enter a hostname (it does not need to be a site hosted in dotCMS, but it needs to point to the dotCMS instance); “https://” will be added automatically by the plugin on the SP metadata file.
* Upload the private key file
* Upload the public certificate file
- Keep “Validation Type = Response and Assertion”
* Override the properties depending on what’s required by the IDP (properties list)
* Save the configuration
* Generate our SP metadata file by clicking on “Download SP Metadata”
* This will not actually download the metadata file, but it will take you to a separate browser tab where the metadata will be shown.
* Share the SP metadata link or the metadata file with the IdP administrator
* Get the IdP metadata link or file from the IdP administrator (This may not be available until after you have shared the SP metadata file with the IdP administrator, previous step)
* Upload the IdP metadata file, on the SAML Configuration needed.
* Add the dotCMS sites that will be using this SAML Configuration to authenticate users
* Set “Enabled? = Yes”
* Save the configuration

## Test
* From a different browser session, try to log into the backend via hostname of one of the sites you associated to the SAML (did this previously)
* Instead of being redirected to the dotCMS default login page, you should hit the IdP login page
* Monitor `dotcms-saml.log` (inside dotCMS’s log directory) for specific messages related to SAML authentication


# Troubleshooting SAML Authentication Issues  
## Unable to login after enabling SAML
Something went wrong with the IdP configuration and I can’t log into the backend anymore. Access still can be granted by adding `?native=true` to the url when accessing the backend. For example: https://dotcms.com/dotAdmin?native=true
