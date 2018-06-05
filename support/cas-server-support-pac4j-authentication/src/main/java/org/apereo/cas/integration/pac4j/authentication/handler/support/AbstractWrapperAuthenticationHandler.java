package org.apereo.cas.integration.pac4j.authentication.handler.support;

import org.apereo.cas.authentication.Credential;
import org.apereo.cas.authentication.HandlerResult;
import org.apereo.cas.authentication.PreventedException;
import org.apereo.cas.authentication.handler.support.AbstractPac4jAuthenticationHandler;
import org.apereo.cas.authentication.principal.ClientCredential;
import org.apereo.cas.authentication.principal.PrincipalFactory;
import org.apereo.cas.services.ServicesManager;
import org.apereo.cas.util.HttpRequestUtils;
import org.apereo.cas.util.Pac4jUtils;
import org.pac4j.core.context.WebContext;
import org.pac4j.core.credentials.Credentials;
import org.pac4j.core.credentials.authenticator.Authenticator;
import org.pac4j.core.profile.UserProfile;
import org.pac4j.core.profile.creator.AuthenticatorProfileCreator;
import org.pac4j.core.profile.creator.ProfileCreator;
import org.pac4j.core.util.CommonHelper;
import org.pac4j.core.util.InitializableObject;
import org.pac4j.core.util.InitializableWebObject;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.security.auth.login.FailedLoginException;
import java.security.GeneralSecurityException;

/**
 * Abstract pac4j authentication handler which uses a pac4j authenticator and profile creator.
 *
 * @author Jerome Leleu
 * @param <I> the type parameter
 * @param <C> the type parameter
 * @since 4.2.0
 */
public abstract class AbstractWrapperAuthenticationHandler<I extends Credential, C extends Credentials>
        extends AbstractPac4jAuthenticationHandler {

    private static final Logger LOGGER = LoggerFactory.getLogger(AbstractWrapperAuthenticationHandler.class);
    
    /**
     * The pac4j profile creator used for authentication.
     */
    protected ProfileCreator profileCreator = AuthenticatorProfileCreator.INSTANCE;

    public AbstractWrapperAuthenticationHandler(final String name, final ServicesManager servicesManager, final PrincipalFactory principalFactory,
                                                final Integer order) {
        super(name, servicesManager, principalFactory, order);
    }

    @Override
    public boolean supports(final Credential credential) {
        return credential != null && getCasCredentialsType().isAssignableFrom(credential.getClass());
    }

    @Override
    protected HandlerResult doAuthentication(final Credential credential) throws GeneralSecurityException, PreventedException {
        CommonHelper.assertNotNull("profileCreator", this.profileCreator);

        final C credentials = convertToPac4jCredentials((I) credential);
        LOGGER.debug("credentials: [{}]", credentials);

        try {
            final Authenticator authenticator = getAuthenticator(credential);
     
            if (authenticator instanceof InitializableObject) {
                ((InitializableObject) authenticator).init();
            }
            if (authenticator instanceof InitializableWebObject) {
                ((InitializableWebObject) authenticator).init(getWebContext());
            }
            
            CommonHelper.assertNotNull("authenticator", authenticator);
            authenticator.validate(credentials, getWebContext());

            final UserProfile profile = this.profileCreator.create(credentials, getWebContext());
            LOGGER.debug("profile: [{}]", profile);

            return createResult(new ClientCredential(credentials), profile);
        } catch (final Exception e) {
            LOGGER.error("Failed to validate credentials", e);
            throw new FailedLoginException("Failed to validate credentials: " + e.getMessage());
        }
    }

    /**
     * Gets the web context from the current thread-bound object.
     *
     * @return the web context
     */
    protected static WebContext getWebContext() {
        return Pac4jUtils.getPac4jJ2EContext(
                HttpRequestUtils.getHttpServletRequestFromRequestAttributes(),
                HttpRequestUtils.getHttpServletResponseFromRequestAttributes());
    }

    /**
     * Convert a CAS credential into a pac4j credentials to play the authentication.
     *
     * @param casCredential the CAS credential
     * @return the pac4j credentials
     * @throws GeneralSecurityException On authentication failure.
     */
    protected abstract C convertToPac4jCredentials(I casCredential) throws GeneralSecurityException;

    /**
     * Return the CAS credential supported by this handler (to be converted in a pac4j credentials
     * by {@link #convertToPac4jCredentials(Credential)}).
     *
     * @return the CAS credential class
     */
    protected abstract Class<I> getCasCredentialsType();

    /**
     * Gets authenticator.
     *
     * @param credential the credential
     * @return the authenticator
     */
    protected abstract Authenticator getAuthenticator(Credential credential);

    public ProfileCreator getProfileCreator() {
        return this.profileCreator;
    }

    public void setProfileCreator(final ProfileCreator profileCreator) {
        this.profileCreator = profileCreator;
    }
}
