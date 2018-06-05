package org.apereo.cas.config;

import org.apereo.cas.configuration.CasConfigurationProperties;
import org.apereo.cas.configuration.model.support.saml.sps.AbstractSamlSPProperties;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.context.annotation.Configuration;

/**
 * This is {@link CasSamlSPAcademicWorksConfiguration}.
 *
 * @author Misagh Moayyed
 * @since 5.1.0
 */
@Configuration("casSamlSPAcademicWorksConfiguration")
@EnableConfigurationProperties(CasConfigurationProperties.class)
public class CasSamlSPAcademicWorksConfiguration extends BaseCasSamlSPConfiguration {
    
    @Override
    protected AbstractSamlSPProperties getServiceProvider() {
        return casProperties.getSamlSp().getAcademicWorks();
    }
}
