package com.dotcms.plugin.saml.v3.filter;

import com.dotcms.plugin.saml.v3.SamlAuthenticationService;
import com.dotcms.plugin.saml.v3.config.Configuration;
import com.dotcms.plugin.saml.v3.init.Initializer;
import com.dotmarketing.util.WebKeys;
import com.liferay.portal.model.User;
import com.liferay.util.InstancePool;
import org.junit.Before;
import org.junit.Test;
import org.mockito.invocation.InvocationOnMock;
import org.mockito.stubbing.Answer;

import javax.servlet.FilterChain;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.servlet.http.HttpSession;

import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;
import static org.mockito.Mockito.*;

/**
 * Test {@link SamlAccessFilter}
 * @author jsanca
 */
public class SamlAccessFilterTest {

    private boolean filterChainCalled = false;

    @Before
    public void initTest(){

        filterChainCalled = false;
        final Configuration configuration =
                mock(Configuration.class);
        InstancePool.put(Configuration.class.getName(), configuration);
        when(configuration.getAccessFilterArray()).thenReturn(new String [] {"saml3/metadata/dotcms_metadata.xml"});
    }

    @Test
    public void doFilterMetadataTest () throws Exception {

        final SamlAuthenticationService authenticationService =
                mock(SamlAuthenticationService.class);
        final Initializer initializer = mock(Initializer.class);
        final SamlAccessFilter accessFilter =
                new SamlAccessFilter(authenticationService, initializer);
        final HttpServletRequest request  = mock(HttpServletRequest.class);
        final HttpServletResponse response = mock(HttpServletResponse.class);
        final HttpSession session  = mock(HttpSession.class);
        final FilterChain chain = mock(FilterChain.class);
        when(request.getSession(false)).thenReturn(session);
        when(request.getRequestURI()).thenReturn("saml3/metadata/dotcms_metadata.xml");
        doAnswer(new Answer<Void>() { // if this method is called, should fail

            @Override
            public Void answer(InvocationOnMock invocation) throws Throwable {

                filterChainCalled = true;
                return null;
            }
        }).when(chain).doFilter(request, response);

        accessFilter.doFilter(request, response, chain);
        assertTrue(this.filterChainCalled);
    } // doFilterTestMetada

    @Test
    public void doFilterSessionNullTest () throws Exception {

        final SamlAuthenticationService authenticationService =
                mock(SamlAuthenticationService.class);
        final Initializer initializer = mock(Initializer.class);
        final SamlAccessFilter accessFilter =
                new SamlAccessFilter(authenticationService, initializer);
        final HttpServletRequest request  = mock(HttpServletRequest.class);
        final HttpServletResponse response = mock(HttpServletResponse.class);
        final HttpSession session  = mock(HttpSession.class);
        final FilterChain chain = mock(FilterChain.class);

        when(request.getSession(false)).thenReturn(null);
        when(request.getRequestURI()).thenReturn("some/test/url");
        doAnswer(new Answer<Void>() { // if this method is called, should fail

            @Override
            public Void answer(InvocationOnMock invocation) throws Throwable {

                filterChainCalled = true;
                return null;
            }
        }).when(chain).doFilter(request, response);

        accessFilter.doFilter(request, response, chain);
        assertFalse(this.filterChainCalled);
    } // doFilterSessionNullTest

    @Test
    public void doFilterSessionNotNullTest () throws Exception {

        final SamlAuthenticationService authenticationService =
                mock(SamlAuthenticationService.class);
        final Initializer initializer = mock(Initializer.class);
        final SamlAccessFilter accessFilter =
                new SamlAccessFilter(authenticationService, initializer);
        final HttpServletRequest request  = mock(HttpServletRequest.class);
        final HttpServletResponse response = mock(HttpServletResponse.class);
        final HttpSession session  = mock(HttpSession.class);
        final FilterChain chain = mock(FilterChain.class);

        when(request.getSession(false)).thenReturn(session);
        when(request.getRequestURI()).thenReturn("some/test/url");
        doAnswer(new Answer<Void>() { // if this method is called, should fail

            @Override
            public Void answer(InvocationOnMock invocation) throws Throwable {

                filterChainCalled = true;
                return null;
            }
        }).when(chain).doFilter(request, response);

        accessFilter.doFilter(request, response, chain);
        assertFalse(this.filterChainCalled);
    } // doFilterSessionNotNullTest

    @Test
    public void doFilterSessionNotNullUserLoggedInTest () throws Exception {

        final SamlAuthenticationService authenticationService =
                mock(SamlAuthenticationService.class);
        final Initializer initializer = mock(Initializer.class);
        final SamlAccessFilter accessFilter =
                new SamlAccessFilter(authenticationService, initializer);
        final HttpServletRequest request  = mock(HttpServletRequest.class);
        final HttpServletResponse response = mock(HttpServletResponse.class);
        final HttpSession session  = mock(HttpSession.class);
        final FilterChain chain = mock(FilterChain.class);

        when(request.getSession(false)).thenReturn(session);
        when(request.getRequestURI()).thenReturn("some/test/url");
        when(session.getAttribute(WebKeys.CMS_USER)).thenReturn(new User());
        doAnswer(new Answer<Void>() { // if this method is called, should fail

            @Override
            public Void answer(InvocationOnMock invocation) throws Throwable {

                filterChainCalled = true;
                return null;
            }
        }).when(chain).doFilter(request, response);

        accessFilter.doFilter(request, response, chain);
        assertTrue(this.filterChainCalled);
    } // doFilterSessionNullTest

} // E:O:F:SamlAccessFilterTest.
