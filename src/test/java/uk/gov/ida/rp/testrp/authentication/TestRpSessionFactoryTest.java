package uk.gov.ida.rp.testrp.authentication;

import com.squarespace.jersey2.guice.JerseyGuiceUtils;
import org.junit.Assert;
import org.junit.Before;
import org.junit.BeforeClass;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mock;
import org.mockito.runners.MockitoJUnitRunner;
import uk.gov.ida.common.SessionId;
import uk.gov.ida.rp.testrp.TestRpConfiguration;
import uk.gov.ida.rp.testrp.controllogic.AuthnRequestSenderHandler;
import uk.gov.ida.rp.testrp.repositories.Session;
import uk.gov.ida.rp.testrp.tokenservice.AccessTokenValidator;

import javax.ws.rs.WebApplicationException;
import javax.ws.rs.container.ContainerRequestContext;
import javax.ws.rs.container.ResourceContext;
import javax.ws.rs.core.Cookie;
import javax.ws.rs.core.MultivaluedHashMap;
import javax.ws.rs.core.Response;
import javax.ws.rs.core.UriInfo;
import java.lang.reflect.Field;
import java.net.URI;
import java.util.HashMap;
import java.util.Map;
import java.util.Optional;

import static java.util.Collections.singletonList;
import static org.mockito.Matchers.any;
import static org.mockito.Matchers.anyBoolean;
import static org.mockito.Matchers.anyString;
import static org.mockito.Matchers.eq;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import static uk.gov.ida.rp.testrp.Urls.Cookies.TEST_RP_SESSION_COOKIE_NAME;

@RunWith(MockitoJUnitRunner.class)
public class TestRpSessionFactoryTest {

    @BeforeClass
    public static void doALittleHackToMakeGuicierHappyForSomeReason() {
        JerseyGuiceUtils.reset();
    }

    private SessionFactory factory;

    @Mock
    private TestRpConfiguration configuration;

    @Mock
    private SimpleAuthenticator authenticator;

    @Mock
    private AccessTokenValidator accessTokenValidator;

    @Mock
    private AuthnRequestSenderHandler authnRequestManager;

    @Mock
    private ResourceContext resourceContext;

    @Mock
    private ContainerRequestContext containerRequestContext;

    @Mock
    private UriInfo uriInfo;

    private final Session expectedSession = new Session(
            SessionId.createNewSessionId(),
            "requestId",
            URI.create("pathUserWasTryingToAccess"),
            "issuerId",
            Optional.ofNullable(1),
            Optional.empty(),
            false,
            false,
            false,
            false);

    @Before
    public void before() throws Exception {
        // https://github.com/HubSpot/dropwizard-guice/issues/95#issuecomment-274851181
        JerseyGuiceUtils.reset();

        when(resourceContext.getResource(ContainerRequestContext.class)).thenReturn(containerRequestContext);
        when(containerRequestContext.getUriInfo()).thenReturn(uriInfo);
        when(uriInfo.getQueryParameters()).thenReturn(new MultivaluedHashMap<>());

        when(authenticator.authenticate(any())).thenReturn(Optional.of(expectedSession));

        factory = new SessionFactory(authenticator,
                configuration,
                authnRequestManager,
                accessTokenValidator);

        setContextUsingReflection(factory);
    }

    @Test
    public void shouldProvideASession() {
        Map<String, Cookie> cookieMap = new HashMap<>();
        Cookie theUserCookieValue = new Cookie(TEST_RP_SESSION_COOKIE_NAME, expectedSession.getSessionId().getSessionId());
        cookieMap.put(TEST_RP_SESSION_COOKIE_NAME, theUserCookieValue);
        when(containerRequestContext.getCookies()).thenReturn(cookieMap);

        Session session = factory.provide();
        Assert.assertEquals(expectedSession, session);
    }

    @Test
    public void shouldSendAuthnRequestWithEidasFlagWhenQueryStringContainsEidas() {
        MultivaluedHashMap<String, String> queryParams = new MultivaluedHashMap<>();
        queryParams.put("eidas", singletonList("true"));

        when(uriInfo.getQueryParameters()).thenReturn(queryParams);

        Response response = null;
        try {
            factory.provide();
        } catch (WebApplicationException wae){
            response = wae.getResponse();
        }
        Assert.assertNotNull(response);

        verify(authnRequestManager).sendAuthnRequest(any(URI.class),
                any(),
                anyString(),
                any(),
                any(),
                anyBoolean(),
                eq(true),
                anyBoolean(),
                anyBoolean()
        );
    }

    /**
     * use reflection to set context - see http://stackoverflow.com/a/29133704/442256
     */
    private void setContextUsingReflection(SessionFactory factory) throws Exception {
        Field context = SessionFactory.class.getDeclaredField("context");
        context.setAccessible(true);
        context.set(factory, resourceContext);
    }
}
