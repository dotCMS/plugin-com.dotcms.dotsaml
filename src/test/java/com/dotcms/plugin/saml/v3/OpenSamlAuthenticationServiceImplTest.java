package com.dotcms.plugin.saml.v3;

import com.dotcms.repackage.org.hibernate.validator.constraints.Email;
import com.dotmarketing.business.Role;
import com.dotmarketing.business.RoleAPI;
import com.dotmarketing.business.UserAPI;
import com.dotmarketing.exception.DotDataException;
import com.dotmarketing.exception.DotSecurityException;
import com.liferay.portal.model.User;
import org.joda.time.DateTime;
import org.junit.Test;
import org.mockito.invocation.InvocationOnMock;
import org.mockito.stubbing.Answer;
import org.opensaml.core.xml.XMLObject;
import org.opensaml.core.xml.config.XMLObjectProviderRegistrySupport;
import org.opensaml.core.xml.schema.XSString;
import org.opensaml.core.xml.schema.impl.XSStringBuilder;
import org.opensaml.saml.saml2.core.*;
import org.opensaml.saml.saml2.core.impl.AttributeStatementImpl;
import org.w3c.dom.Element;
import org.w3c.dom.Node;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import java.util.Arrays;
import java.util.List;

import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertTrue;
import static org.mockito.Matchers.any;
import static org.mockito.Mockito.*;

/**
 * Test {@link OpenSamlAuthenticationServiceImpl}
 * @author jsanca
 */
public class OpenSamlAuthenticationServiceImplTest {

    public static final String USER_EMAIL = "admin@dotmcs.com";
    public static final String NAME = "admin";
    public static final String LAST_NAME = "admin sn";
    public static final String ROLE1 = "role1";
    public static final String ROLE2 = "role2";
    private boolean userCreated = false;
    private boolean role1Created = false;
    private boolean role2Created = false;

    @Test
    public void testCreateUser () throws DotDataException, DotSecurityException {

        final HttpClient httpClient = mock(HttpClient.class);
        final UserAPI userAPI  = mock(UserAPI.class);
        final RoleAPI roleAPI  = mock(RoleAPI.class);
        final HttpServletRequest request  = mock(HttpServletRequest.class);
        final HttpServletResponse response = mock(HttpServletResponse.class);
        final User superUser = new User();
        final User user = new User();
        superUser.setUserId("superuser.1");
        user.setUserId("admin.1");
        user.setEmailAddress(USER_EMAIL);
        when(request.getParameter(OpenSamlAuthenticationServiceImpl.SAML_ART_PARAM_KEY)).thenReturn("SAMLArt=iwuerhckjheiru12943723874");
        when(userAPI.getSystemUser()).thenReturn(superUser);
        when(userAPI.loadByUserByEmail(USER_EMAIL, superUser, false)).thenReturn(null); // user does not exists
        when(userAPI.createUser(any(String.class), eq(USER_EMAIL))).thenReturn(user);
        doAnswer(new Answer<Void>() { // if this method is called, should fail

            @Override
            public Void answer(InvocationOnMock invocation) throws Throwable {

                User user = (User) invocation.getArguments()[0];

                if (null != user) {

                    userCreated = user.getFirstName().equals(NAME) &&
                            user.getLastName().equals(LAST_NAME) &&
                            user.getEmailAddress().equals(USER_EMAIL) &&
                            null != user.getUserId() &&
                            null != user.getPassword() &&
                            user.isActive() && user.isPasswordEncrypted();
                }

                return null;
            }
        }).when(userAPI).save(any(User.class), any(User.class), eq(false));

        doNothing().when(roleAPI).removeAllRolesFromUser(any());

        Role role1 = new Role();
        role1.setId(ROLE1);
        when(roleAPI.loadRoleByKey(eq(ROLE1))).thenReturn(role1);

        Role role2 = new Role();
        role2.setId(ROLE2);
        when(roleAPI.loadRoleByKey(eq(ROLE2))).thenReturn(role2);

        when(roleAPI.doesUserHaveRole(any(User.class), eq(role1))).thenReturn(false);
        when(roleAPI.doesUserHaveRole(any(User.class), eq(role2))).thenReturn(false);

        doAnswer(new Answer<Void>() { // if this method is called, should fail

            @Override
            public Void answer(InvocationOnMock invocation) throws Throwable {

                Role role = (Role) invocation.getArguments()[0];
                User user = (User) invocation.getArguments()[1];

                if (null != role && null != user) {

                    role1Created = user.getFirstName().equals(NAME) &&
                            user.getLastName().equals(LAST_NAME) &&
                            user.getEmailAddress().equals(USER_EMAIL) &&
                            null != user.getUserId() &&
                            null != user.getPassword() &&
                            user.isActive() && user.isPasswordEncrypted() &&
                            role.getId().equals(ROLE1);
                }

                return null;
            }
        }).when(roleAPI).addRoleToUser(eq(role1), any(User.class));

        doAnswer(new Answer<Void>() { // if this method is called, should fail

            @Override
            public Void answer(InvocationOnMock invocation) throws Throwable {

                Role role = (Role) invocation.getArguments()[0];
                User user = (User) invocation.getArguments()[1];

                if (null != role && null != user) {

                    role2Created = user.getFirstName().equals(NAME) &&
                            user.getLastName().equals(LAST_NAME) &&
                            user.getEmailAddress().equals(USER_EMAIL) &&
                            null != user.getUserId() &&
                            null != user.getPassword() &&
                            user.isActive() && user.isPasswordEncrypted() &&
                            role.getId().equals(ROLE2);
                }

                return null;
            }
        }).when(roleAPI).addRoleToUser(eq(role2), any(User.class));

        final SamlAuthenticationService authenticationService =
                new OpenSamlAuthenticationServiceImpl(httpClient, userAPI, roleAPI) {

                    @Override
                    protected Assertion resolveAssertion(HttpServletRequest request, HttpServletResponse response) {
                        return OpenSamlAuthenticationServiceImplTest.this.buildAssertion();
                    }
                };

        User userR = authenticationService.getUser(request, response);

        assertNotNull(userR);
        assertTrue(this.userCreated);
        assertTrue(this.role1Created);
        assertTrue(this.role2Created);
    } // testAuthentication.

    private Assertion buildAssertion() {

        Assertion assertion = mock(Assertion.class);
        List<AttributeStatement> attributeStatements = Arrays.asList(buildAttributeStatement());
        when(assertion.getAttributeStatements()).thenReturn(attributeStatements);

        return assertion;
    }

    private AttributeStatement buildAttributeStatement() {
        AttributeStatement attributeStatement = mock(AttributeStatementImpl.class);

        Attribute attributeEmail = mock(Attribute.class);
        when(attributeEmail.getFriendlyName()).thenReturn("mail");
        XMLObject email         = mock(XMLObject.class);
        Element   emailElement  = mock(Element.class);
        Node      emailNode     = mock(Node.class);
        when(attributeEmail.getAttributeValues()).thenReturn(Arrays.asList(email));
        when(email.getDOM()).thenReturn(emailElement);
        when(emailElement.getFirstChild()).thenReturn(emailNode);
        when(emailNode.getNodeValue()).thenReturn(USER_EMAIL);

        Attribute attributeName = mock(Attribute.class);
        when(attributeName.getFriendlyName()).thenReturn("givenName");
        XMLObject name          = mock(XMLObject.class);
        Element   nameElement   = mock(Element.class);
        Node      nameNode      = mock(Node.class);
        when(attributeName.getAttributeValues()).thenReturn(Arrays.asList(name));
        when(name.getDOM()).thenReturn(nameElement);
        when(nameElement.getFirstChild()).thenReturn(nameNode);
        when(nameNode.getNodeValue()).thenReturn(NAME);

        Attribute attributeLastName = mock(Attribute.class);
        when(attributeLastName.getFriendlyName()).thenReturn("sn");
        XMLObject lastname         = mock(XMLObject.class);
        Element   lastnameElement  = mock(Element.class);
        Node      lastnameNode     = mock(Node.class);
        when(attributeLastName.getAttributeValues()).thenReturn(Arrays.asList(lastname));
        when(lastname.getDOM()).thenReturn(lastnameElement);
        when(lastnameElement.getFirstChild()).thenReturn(lastnameNode);
        when(lastnameNode.getNodeValue()).thenReturn(LAST_NAME);

        XMLObject role1 = mock(XMLObject.class);
        Element   role1Element  = mock(Element.class);
        Node      role1Node     = mock(Node.class);
        when(role1.getDOM()).thenReturn(role1Element);
        when(role1Element.getFirstChild()).thenReturn(role1Node);
        when(role1Node.getNodeValue()).thenReturn(ROLE1);

        XMLObject role2 = mock(XMLObject.class);
        Element   role2Element  = mock(Element.class);
        Node      role2Node     = mock(Node.class);
        when(role2.getDOM()).thenReturn(role2Element);
        when(role2Element.getFirstChild()).thenReturn(role2Node);
        when(role2Node.getNodeValue()).thenReturn(ROLE2);

        Attribute attributeAuth = mock(Attribute.class);
        when(attributeAuth.getFriendlyName()).thenReturn("authorisations");
        when(attributeAuth.getAttributeValues()).thenReturn(Arrays.asList(role1, role2));

        when(attributeStatement.getAttributes()).thenReturn(Arrays.asList
                (attributeEmail, attributeName, attributeLastName, attributeAuth));


        return attributeStatement;
    }
} // E:O:F:OpenSamlAuthenticationServiceImplTest.
