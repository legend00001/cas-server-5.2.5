package org.apereo.cas.impl.calcs;

import org.apereo.cas.api.AuthenticationRiskEvaluator;
import org.apereo.cas.api.AuthenticationRiskScore;
import org.apereo.cas.authentication.Authentication;
import org.apereo.cas.authentication.CoreAuthenticationTestUtils;
import org.apereo.cas.config.CasCoreAuthenticationConfiguration;
import org.apereo.cas.config.CasCoreAuthenticationHandlersConfiguration;
import org.apereo.cas.config.CasCoreAuthenticationMetadataConfiguration;
import org.apereo.cas.config.CasCoreAuthenticationPolicyConfiguration;
import org.apereo.cas.config.CasCoreAuthenticationPrincipalConfiguration;
import org.apereo.cas.config.CasCoreAuthenticationServiceSelectionStrategyConfiguration;
import org.apereo.cas.config.CasCoreAuthenticationSupportConfiguration;
import org.apereo.cas.config.CasCoreConfiguration;
import org.apereo.cas.config.CasCoreHttpConfiguration;
import org.apereo.cas.config.CasCoreServicesAuthenticationConfiguration;
import org.apereo.cas.config.CasCoreServicesConfiguration;
import org.apereo.cas.config.CasCoreTicketCatalogConfiguration;
import org.apereo.cas.config.CasCoreTicketsConfiguration;
import org.apereo.cas.config.CasCoreUtilConfiguration;
import org.apereo.cas.config.CasCoreWebConfiguration;
import org.apereo.cas.config.CasDefaultServiceTicketIdGeneratorsConfiguration;
import org.apereo.cas.config.CasPersonDirectoryConfiguration;
import org.apereo.cas.config.ElectronicFenceConfiguration;
import org.apereo.cas.config.support.CasWebApplicationServiceFactoryConfiguration;
import org.apereo.cas.impl.mock.MockTicketGrantingTicketCreatedEventProducer;
import org.apereo.cas.logout.config.CasCoreLogoutConfiguration;
import org.apereo.cas.services.RegisteredService;
import org.apereo.cas.services.RegisteredServiceTestUtils;
import org.apereo.cas.support.events.CasEventRepository;
import org.apereo.cas.support.events.config.CasCoreEventsConfiguration;
import org.apereo.cas.support.events.config.CasEventsInMemoryRepositoryConfiguration;
import org.apereo.cas.support.geo.config.GoogleMapsGeoCodingConfiguration;
import org.apereo.cas.web.config.CasCookieConfiguration;
import org.apereo.cas.web.flow.config.CasCoreWebflowConfiguration;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.cloud.autoconfigure.RefreshAutoConfiguration;
import org.springframework.mock.web.MockHttpServletRequest;
import org.springframework.scheduling.annotation.EnableScheduling;
import org.springframework.test.annotation.DirtiesContext;
import org.springframework.test.context.TestPropertySource;
import org.springframework.test.context.junit4.SpringRunner;

import static org.junit.Assert.*;

/**
 * This is {@link DateTimeAuthenticationRequestRiskCalculatorTests}.
 *
 * @author Misagh Moayyed
 * @since 5.1.0
 */
@RunWith(SpringRunner.class)
@SpringBootTest(classes = {RefreshAutoConfiguration.class,
        ElectronicFenceConfiguration.class,
        CasWebApplicationServiceFactoryConfiguration.class,
        CasDefaultServiceTicketIdGeneratorsConfiguration.class,
        CasCoreAuthenticationPrincipalConfiguration.class,
        CasCoreAuthenticationPolicyConfiguration.class,
        CasCoreAuthenticationMetadataConfiguration.class,
        CasCoreAuthenticationSupportConfiguration.class,
        CasCoreAuthenticationHandlersConfiguration.class,
        CasCoreAuthenticationConfiguration.class, 
        CasCoreServicesAuthenticationConfiguration.class,
        CasCoreHttpConfiguration.class,
        CasPersonDirectoryConfiguration.class,
        CasCoreServicesConfiguration.class,
        GoogleMapsGeoCodingConfiguration.class,
        CasCoreWebConfiguration.class,
        CasCoreWebflowConfiguration.class,
        CasCoreConfiguration.class,
        CasCoreAuthenticationServiceSelectionStrategyConfiguration.class,
        CasCoreTicketsConfiguration.class,
        CasCoreTicketCatalogConfiguration.class,
        CasCoreLogoutConfiguration.class,
        CasCookieConfiguration.class,
        CasEventsInMemoryRepositoryConfiguration.class,
        CasCoreUtilConfiguration.class,
        CasCoreEventsConfiguration.class})
@TestPropertySource(properties = "cas.authn.adaptive.risk.dateTime.enabled=true")
@DirtiesContext
@EnableScheduling
public class DateTimeAuthenticationRequestRiskCalculatorTests {

    @Autowired
    @Qualifier("casEventRepository")
    private CasEventRepository casEventRepository;

    @Autowired
    @Qualifier("authenticationRiskEvaluator")
    private AuthenticationRiskEvaluator authenticationRiskEvaluator;
    
    @Before
    public void prepTest() {
        MockTicketGrantingTicketCreatedEventProducer.createEvents(this.casEventRepository);
    }

    @Test
    public void verifyTestWhenNoAuthnEventsFoundForUser() {
        final Authentication authentication = CoreAuthenticationTestUtils.getAuthentication("datetimeperson");
        final RegisteredService service = RegisteredServiceTestUtils.getRegisteredService("test");
        final MockHttpServletRequest request = new MockHttpServletRequest();
        final AuthenticationRiskScore score = authenticationRiskEvaluator.eval(authentication, service, request);
        assertTrue(score.isHighestRisk());
    }

    @Test
    public void verifyTestWhenAuthnEventsFoundForUser() {
        final Authentication authentication = CoreAuthenticationTestUtils.getAuthentication("casuser");
        final RegisteredService service = RegisteredServiceTestUtils.getRegisteredService("test");
        final MockHttpServletRequest request = new MockHttpServletRequest();
        final AuthenticationRiskScore score = authenticationRiskEvaluator.eval(authentication, service, request);
        assertTrue(score.isLowestRisk());
    }
}
