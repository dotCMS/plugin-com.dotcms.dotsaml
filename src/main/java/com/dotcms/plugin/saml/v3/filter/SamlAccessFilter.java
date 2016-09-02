package com.dotcms.plugin.saml.v3.filter;

import com.dotcms.plugin.saml.v3.*;
import com.dotcms.plugin.saml.v3.config.Configuration;
import com.dotcms.plugin.saml.v3.init.DefaultInitializer;
import com.dotcms.plugin.saml.v3.init.Initializer;
import com.dotcms.repackage.com.google.common.annotations.VisibleForTesting;
import com.dotcms.repackage.org.apache.commons.io.IOUtils;
import com.dotcms.repackage.org.apache.commons.lang.StringUtils;
import com.dotmarketing.cms.factories.PublicEncryptionFactory;
import com.dotmarketing.cms.login.factories.LoginFactory;
import com.dotmarketing.util.Config;
import com.dotmarketing.util.Logger;
import com.dotmarketing.util.WebKeys;
import com.liferay.portal.model.User;
import com.liferay.util.InstancePool;
import org.opensaml.core.xml.io.MarshallingException;
import org.opensaml.saml.saml2.metadata.EntityDescriptor;

import javax.servlet.*;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.servlet.http.HttpSession;
import javax.xml.parsers.ParserConfigurationException;
import javax.xml.transform.TransformerException;
import java.io.IOException;
import java.io.Writer;
import java.lang.reflect.InvocationTargetException;
import java.util.Collections;

/**
 * Access filter for SAML plugin, it does the autologin and also redirect to the
 * IDP if the user is not logged in.
 * In addition prints out the metadata.xml information for the dotCMS SP.
 * @author jsanca
 */
public class SamlAccessFilter implements Filter {

    private final SamlAuthenticationService samlAuthenticationService;
    private final Initializer initializer;
    private final MetaDataXMLPrinter metaDataXMLPrinter;

    public SamlAccessFilter() {

        this(new OpenSamlAuthenticationServiceImpl(),
                InstanceUtil.newInstance(Config.getStringProperty(
                        DotSamlConstants.DOT_SAML_INITIALIZER_CLASS_NAME, null
                ), DefaultInitializer.class));
    }

    @VisibleForTesting
    public SamlAccessFilter(final SamlAuthenticationService samlAuthenticationService,
                            final Initializer initializer) {

        this.samlAuthenticationService = samlAuthenticationService;
        this.initializer               = (null == initializer)?
                new DefaultInitializer():
                initializer;
        this.metaDataXMLPrinter = new MetaDataXMLPrinter();
    }

    @Override
    public void init(final FilterConfig filterConfig) throws ServletException {

        Logger.info(this, "Going to call the Initializer: " + this.initializer);

        if (!this.initializer.isInitializationDone()) {

            this.initializer.init(Collections.EMPTY_MAP);
        } else {

            Logger.info(this, "The initializer was already init: " + this.initializer);
        }
    } // init.

    /**
     * This method checks if some path does not wants to be treatment by the {@link SamlAccessFilter}
     * An example of exception might be the destroy.jsp, so on.
     * @param uri {@link String}
     * @param filterPaths {@link String} array
     * @return boolean
     */
    private boolean checkAccessFilters (String uri, final String [] filterPaths) {

        boolean filter = false;

        if (null != filterPaths) {

            for (String filterPath : filterPaths) {

                filter |= uri.contains(filterPath); //("saml3/metadata/dotcms_metadata.xml")
            }
        }

        return filter;
    } // checkAccessFilters.

    @Override
    public void doFilter(final ServletRequest req,
                         final ServletResponse res,
                         final FilterChain chain) throws IOException, ServletException {

        final HttpServletResponse response = (HttpServletResponse) res;
        final HttpServletRequest  request  = (HttpServletRequest) req;
        final HttpSession         session  = request.getSession(false);
        final Configuration configuration  = (Configuration) InstancePool.get(Configuration.class.getName());
        String redirectAfterLogin = null;

        // First, check if the current request is the SP metadata xml.
        if (request.getRequestURI().contains(configuration.getServiceProviderCustomMetadataPath())) {

            // if its, so print it out in the response and return.
            this.printMetaData(request, response, configuration);
            return;
        }

        // check if there is any exception filter path, to avoid to apply all the logic.
        if (!this.checkAccessFilters(request.getRequestURI(), configuration.getAccessFilterArray())) {

            // if it is an url to apply the Saml access logic, determine if the autoLogin is possible
            // the autologin will works if the SAMLArt (Saml artifact id) is in the request query string.
            if (!this.autoLogin(request, response, session)) {

                return; // no continue. Usually no continue when there is a sendRedirect done.
            }

            // if the auto login couldn't loggged the user, then send it to the IdP login page.
            if (null == session || null == session.getAttribute(WebKeys.CMS_USER)) {

                // this is safe, just to make a redirection when the user get's logged.
                redirectAfterLogin = request.getRequestURI() +
                        ((null != request.getQueryString())? "?" + request.getQueryString():
                                     StringUtils.EMPTY);

                Logger.warn(this.getClass(),
                        "Doing Saml Login Redirection when request: " +
                                redirectAfterLogin);

                //if we don't have a redirect yet
                if (null != session) {

                    session.setAttribute(WebKeys.REDIRECT_AFTER_LOGIN,
                            redirectAfterLogin);
                }

                // this will redirect the user to the IdP Login Page.
                this.samlAuthenticationService.authentication(request, response);
                return;
            }
        }

        chain.doFilter(request, response);
    } // doFilter.

    private void printMetaData(final HttpServletRequest request,
                               final HttpServletResponse response,
                               final Configuration configuration) throws ServletException {

        // First, get the Entity descriptor.
        final EntityDescriptor descriptor =
                configuration.getMetaDescriptorService().getServiceProviderEntityDescriptor();
        Writer writer = null;

        try {

            // get ready to convert it to XML.
            response.setContentType("text/xml");
            writer = response.getWriter();
            this.metaDataXMLPrinter.print(descriptor, writer);
            response.setStatus(HttpServletResponse.SC_OK);
        } catch (ParserConfigurationException | TransformerException | IOException | MarshallingException e) {

            Logger.error(this.getClass(),
                    e.getMessage(), e);
            throw new ServletException(e);
        } finally {

            IOUtils.closeQuietly(writer);
        }
    } // printMetaData.

    private boolean autoLogin (final HttpServletRequest request,
                            final HttpServletResponse response,
                            HttpSession         session) {

        final User user =
                this.samlAuthenticationService.getUser(request, response);

        if (null != user) {

            // we are going to do the autologin, so if the session is null, create it!
            session = (null == session)? request.getSession():session;

            try {

                Logger.info(this, "User returned by SAML Service, id " + user.getUserId() +
                        ", user Map: " + user.toMap());
            } catch (Exception e) {

                Logger.error(this, e.getMessage(), e);
            }

            // todo: 3.7 this should be changed to LoginService
            final boolean doCookieLogin = LoginFactory.doCookieLogin(PublicEncryptionFactory.encryptString
                    (user.getUserId()), request, response);

            Logger.info(this, "Login result by LoginFactory: " + doCookieLogin);

            if (doCookieLogin) {

                if (null != session && null != user.getUserId()) {
                    // this is what the PortalRequestProcessor needs to check the login.
                    Logger.info(this, "Setting the user id on the session: " + user.getUserId());
                    session.setAttribute(com.liferay.portal.util.WebKeys.USER_ID, user.getUserId());

                    return this.checkRedirection(request, response, session);
                }
            }
        }

        return true;
    } // autoLogin.

    private boolean checkRedirection(final HttpServletRequest request,
                                  final HttpServletResponse response,
                                  final HttpSession session) {

        final String redirectAfterLogin = (String) session.
                getAttribute(WebKeys.REDIRECT_AFTER_LOGIN);

        if (null != redirectAfterLogin) {

            session.removeAttribute(WebKeys.REDIRECT_AFTER_LOGIN);
            final String currentRequest = request.getRequestURI() +
                    ((null != request.getQueryString())? "?" + request.getQueryString():
                            StringUtils.EMPTY);
            // if it is not the same request.
            if (!currentRequest.equals(redirectAfterLogin)) {

                try {

                    Logger.info(this, "Redirecting to: " + redirectAfterLogin);
                    response.sendRedirect(redirectAfterLogin);
                    return false; // not continue. since it is a redirect.
                } catch (IOException e) {

                    Logger.error(this, e.getMessage(), e);
                }
            }
        }

        return true; // continue with the current request.
    } // checkRedirection.

    @Override
    public void destroy() {

    }
} // E:O:F:SamlAccessFilter.
