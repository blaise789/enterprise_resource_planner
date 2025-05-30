package com.erp.erp.config;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Description;
import org.thymeleaf.spring6.SpringTemplateEngine;
import org.thymeleaf.spring6.templateresolver.SpringResourceTemplateResolver;
import org.thymeleaf.templatemode.TemplateMode;
import org.thymeleaf.extras.springsecurity6.dialect.SpringSecurityDialect;
import org.thymeleaf.spring6.dialect.SpringStandardDialect;
import java.util.Set;

import java.nio.charset.StandardCharsets;

/**
 * Configuration class for Thymeleaf template engine.
 * This configuration is necessary for processing HTML templates like payslip.html.
 */
@Configuration
public class ThymeleafConfig {

    /**
     * Template resolver for HTML templates in the classpath:/templates/ directory.
     * This resolver is used for both web views and email templates.
     */
    @Bean
    @Description("Thymeleaf template resolver for HTML templates")
    public SpringResourceTemplateResolver templateResolver() {
        SpringResourceTemplateResolver templateResolver = new SpringResourceTemplateResolver();
        templateResolver.setPrefix("classpath:/templates/");
        templateResolver.setSuffix(".html");
        templateResolver.setTemplateMode(TemplateMode.HTML);
        templateResolver.setCharacterEncoding(StandardCharsets.UTF_8.name());
        templateResolver.setCacheable(false); // Set to true in production
        templateResolver.setCheckExistence(true);
        return templateResolver;
    }

    /**
     * Thymeleaf template engine with the HTML template resolver.
     */
    @Bean
    @Description("Thymeleaf template engine with HTML template resolver")
    public SpringTemplateEngine templateEngine() {
        SpringTemplateEngine templateEngine = new SpringTemplateEngine();
        templateEngine.setTemplateResolver(templateResolver());
        templateEngine.setEnableSpringELCompiler(true);

        // Add SpringSecurityDialect for additional features
        templateEngine.addDialect(new SpringSecurityDialect());

        // Configure the SpringStandardDialect to allow the format method on DecimalFormat
        SpringStandardDialect dialect = new SpringStandardDialect();
        dialect.getExecutionAttributes( );

        // Allow java.text.DecimalFormat.format method
        Set<String> allowedSpringELFormatterMethods = Set.of("format");
        dialect.setEnableSpringELCompiler(true);

        templateEngine.setDialect(dialect);

        return templateEngine;
    }
}
