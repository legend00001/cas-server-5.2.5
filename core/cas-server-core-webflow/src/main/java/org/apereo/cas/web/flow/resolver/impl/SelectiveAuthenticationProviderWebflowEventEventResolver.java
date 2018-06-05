package org.apereo.cas.web.flow.resolver.impl;

import org.apache.commons.lang3.tuple.Pair;
import org.apereo.cas.CentralAuthenticationService;
import org.apereo.cas.authentication.Authentication;
import org.apereo.cas.authentication.AuthenticationServiceSelectionPlan;
import org.apereo.cas.authentication.AuthenticationSystemSupport;
import org.apereo.cas.authentication.MultifactorAuthenticationUtils;
import org.apereo.cas.services.MultifactorAuthenticationProvider;
import org.apereo.cas.services.MultifactorAuthenticationProviderSelector;
import org.apereo.cas.services.RegisteredService;
import org.apereo.cas.services.ServicesManager;
import org.apereo.cas.ticket.registry.TicketRegistrySupport;
import org.apereo.cas.web.flow.authentication.BaseMultifactorAuthenticationProviderEventResolver;
import org.apereo.cas.web.support.WebUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.web.util.CookieGenerator;
import org.springframework.webflow.execution.Event;
import org.springframework.webflow.execution.RequestContext;

import javax.servlet.http.HttpServletRequest;
import java.util.Collection;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;

/**
 * This is {@link SelectiveAuthenticationProviderWebflowEventEventResolver}
 * that acts as a stub resolver, specifically designed for extensions.
 * Deployers can extend this class to perform additional processes on the final set
 * of resolved events, to select one vs another based on the nature of the event attributes.
 *
 * @author Misagh Moayyed
 * @since 5.0.0
 */
public class SelectiveAuthenticationProviderWebflowEventEventResolver extends BaseMultifactorAuthenticationProviderEventResolver {
    private static final Logger LOGGER = LoggerFactory.getLogger(SelectiveAuthenticationProviderWebflowEventEventResolver.class);
    
    public SelectiveAuthenticationProviderWebflowEventEventResolver(final AuthenticationSystemSupport authenticationSystemSupport,
                                                                    final CentralAuthenticationService centralAuthenticationService,
                                                                    final ServicesManager servicesManager,
                                                                    final TicketRegistrySupport ticketRegistrySupport,
                                                                    final CookieGenerator warnCookieGenerator,
                                                                    final AuthenticationServiceSelectionPlan authenticationSelectionStrategies,
                                                                    final MultifactorAuthenticationProviderSelector selector) {
        super(authenticationSystemSupport, centralAuthenticationService, servicesManager, ticketRegistrySupport, warnCookieGenerator,
                authenticationSelectionStrategies, selector);
    }

    @Override
    public Set<Event> resolveInternal(final RequestContext context) {
        final Set<Event> resolvedEvents = getResolvedEventsAsAttribute(context);
        final Authentication authentication = WebUtils.getAuthentication(context);
        final RegisteredService registeredService = resolveRegisteredServiceInRequestContext(context);
        final HttpServletRequest request = WebUtils.getHttpServletRequestFromExternalWebflowContext(context);
        return resolveEventsInternal(resolvedEvents, authentication, registeredService, request, context);
    }

    /**
     * Resolve events internal set. Implementation may filter events from the collection
     * to only return the one that is appropriate for this request. The default
     * implementation returns the entire collection.
     *
     * @param resolveEvents     the resolve events
     * @param authentication    the authentication
     * @param registeredService the registered service
     * @param request           the request
     * @param context           the request context
     * @return the set of resolved events
     */
    protected Set<Event> resolveEventsInternal(final Set<Event> resolveEvents, final Authentication authentication, final RegisteredService registeredService,
                                               final HttpServletRequest request, final RequestContext context) {
        LOGGER.debug("Collection of resolved events for this authentication sequence are:");
        resolveEvents.forEach(e -> LOGGER.debug("Event id [{}] resolved from [{}]", e.getId(), e.getSource().getClass().getName()));
        final Pair<Set<Event>, Collection<MultifactorAuthenticationProvider>> pair =
                filterEventsByMultifactorAuthenticationProvider(resolveEvents, authentication, registeredService, request);
        WebUtils.putResolvedMultifactorAuthenticationProviders(context, pair.getValue());
        return pair.getKey();
    }

    /**
     * Filter events by multifactor authentication providers.
     *
     * @param resolveEvents     the resolve events
     * @param authentication    the authentication
     * @param registeredService the registered service
     * @param request           the request
     * @return the set of events
     */
    protected Pair<Set<Event>, Collection<MultifactorAuthenticationProvider>> filterEventsByMultifactorAuthenticationProvider(
            final Set<Event> resolveEvents, final Authentication authentication, 
            final RegisteredService registeredService,
            final HttpServletRequest request) {
        LOGGER.debug("Locating multifactor providers to determine support for this authentication sequence");
        final Map<String, MultifactorAuthenticationProvider> providers =
                MultifactorAuthenticationUtils.getAvailableMultifactorAuthenticationProviders(applicationContext);

        if (providers == null || providers.isEmpty()) {
            LOGGER.debug("No providers are available to honor this request. Moving on...");
            return Pair.of(resolveEvents, new HashSet<>(0));
        }

        final Collection<MultifactorAuthenticationProvider> flattenedProviders = flattenProviders(providers.values());

        // remove providers that don't support the event
        flattenedProviders.removeIf(p -> resolveEvents.stream()
                .filter(e -> p.supports(e, authentication, registeredService, request))
                .count() == 0);

        // remove events that are not supported by providers.
        resolveEvents.removeIf(e -> flattenedProviders.stream()
                .filter(p -> p.supports(e, authentication, registeredService, request))
                .count() == 0);

        LOGGER.debug("Finalized set of resolved events are [{}]", resolveEvents);
        return Pair.of(resolveEvents, flattenedProviders);
    }
}
