package com.quantexits.opportunity.spring;

import com.quantexits.dao.spring.QESDAOConfig;
import org.springframework.context.annotation.ComponentScan;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Import;

/**
 * Spring configuration.
 */
@Configuration
@ComponentScan("com.quantexits.opportunity")
@Import(QESDAOConfig.class)
class OpportunityGeneratorConfig {
}
