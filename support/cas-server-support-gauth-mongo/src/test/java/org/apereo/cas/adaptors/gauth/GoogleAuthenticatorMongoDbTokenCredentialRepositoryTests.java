package org.apereo.cas.adaptors.gauth;

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
import org.apereo.cas.config.CasPersonDirectoryConfiguration;
import org.apereo.cas.config.GoogleAuthenticatorMongoDbConfiguration;
import org.apereo.cas.config.support.CasWebApplicationServiceFactoryConfiguration;
import org.apereo.cas.config.support.EnvironmentConversionServiceInitializer;
import org.apereo.cas.config.support.authentication.GoogleAuthenticatorAuthenticationEventExecutionPlanConfiguration;
import org.apereo.cas.logout.config.CasCoreLogoutConfiguration;
import org.apereo.cas.otp.repository.credentials.OneTimeTokenAccount;
import org.apereo.cas.otp.repository.credentials.OneTimeTokenCredentialRepository;
import org.apereo.cas.util.SchedulingUtils;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.boot.autoconfigure.aop.AopAutoConfiguration;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.context.TestConfiguration;
import org.springframework.cloud.autoconfigure.RefreshAutoConfiguration;
import org.springframework.context.ApplicationContext;
import org.springframework.context.annotation.EnableAspectJAutoProxy;
import org.springframework.scheduling.annotation.EnableScheduling;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.TestPropertySource;
import org.springframework.test.context.junit4.SpringRunner;
import org.springframework.transaction.annotation.EnableTransactionManagement;

import javax.annotation.PostConstruct;
import java.util.Arrays;

import static org.junit.Assert.*;

/**
 * This is {@link GoogleAuthenticatorMongoDbTokenCredentialRepositoryTests}.
 *
 * @author Misagh Moayyed
 * @since 5.0.0
 */
@RunWith(SpringRunner.class)
@SpringBootTest(
        classes = {
                GoogleAuthenticatorMongoDbTokenCredentialRepositoryTests.MongoTestConfiguration.class,
                GoogleAuthenticatorMongoDbConfiguration.class,
                CasCoreTicketsConfiguration.class,
                CasCoreTicketCatalogConfiguration.class,
                CasCoreLogoutConfiguration.class,
                CasCoreHttpConfiguration.class,
                CasCoreServicesConfiguration.class,
                CasWebApplicationServiceFactoryConfiguration.class,
                CasCoreAuthenticationConfiguration.class,
                CasCoreServicesAuthenticationConfiguration.class,
                CasCoreAuthenticationMetadataConfiguration.class,
                CasCoreAuthenticationPolicyConfiguration.class,
                CasCoreAuthenticationPrincipalConfiguration.class,
                CasCoreAuthenticationHandlersConfiguration.class,
                CasCoreAuthenticationSupportConfiguration.class,
                CasPersonDirectoryConfiguration.class,
                GoogleAuthenticatorAuthenticationEventExecutionPlanConfiguration.class,
                AopAutoConfiguration.class,
                CasCoreConfiguration.class,
                CasCoreAuthenticationServiceSelectionStrategyConfiguration.class,
                CasCoreUtilConfiguration.class,
                RefreshAutoConfiguration.class,
                CasCoreWebConfiguration.class})
@EnableTransactionManagement(proxyTargetClass = true)
@EnableAspectJAutoProxy(proxyTargetClass = true)
@TestPropertySource(locations = {"classpath:/mongogauth.properties"})
@EnableScheduling
@ContextConfiguration(initializers = EnvironmentConversionServiceInitializer.class)
public class GoogleAuthenticatorMongoDbTokenCredentialRepositoryTests {

    @Autowired
    @Qualifier("googleAuthenticatorAccountRegistry")
    private OneTimeTokenCredentialRepository registry;

    @Test
    public void verifySave() {
        registry.save("uid", "secret", 143211, Arrays.asList(1, 2, 3, 4, 5, 6));
        final OneTimeTokenAccount s = registry.get("uid");
        assertEquals(s.getSecretKey(), "secret");
    }

    @TestConfiguration
    public static class MongoTestConfiguration {
        @Autowired
        protected ApplicationContext applicationContext;

        @PostConstruct
        public void init() {
            SchedulingUtils.prepScheduledAnnotationBeanPostProcessor(applicationContext);
        }
    }
}
