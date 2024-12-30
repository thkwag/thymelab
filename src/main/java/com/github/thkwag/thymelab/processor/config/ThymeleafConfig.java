package com.github.thkwag.thymelab.processor.config;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Primary;
import org.thymeleaf.spring6.SpringTemplateEngine;
import org.thymeleaf.spring6.templateresolver.SpringResourceTemplateResolver;
import org.thymeleaf.spring6.view.ThymeleafViewResolver;
import org.thymeleaf.templatemode.TemplateMode;
import nz.net.ultraq.thymeleaf.layoutdialect.LayoutDialect;
import org.springframework.beans.factory.annotation.Value;
import lombok.extern.slf4j.Slf4j;
import org.thymeleaf.IEngineConfiguration;
import org.thymeleaf.templateresource.ITemplateResource;

import java.util.Map;

import org.springframework.context.ApplicationContext;

@Slf4j
@Configuration
public class ThymeleafConfig {

    private final ApplicationContext applicationContext;
    @Value("${watch.directory.templates:classpath:/default/templates/}")
    private String templatesDir;

    public ThymeleafConfig(ApplicationContext applicationContext) {
        this.applicationContext = applicationContext;
    }

    @Bean
    @Primary
    public SpringResourceTemplateResolver templateResolver() {
        SpringResourceTemplateResolver resolver = new SpringResourceTemplateResolver() {
            @Override
            protected ITemplateResource computeTemplateResource(
                    IEngineConfiguration configuration, String ownerTemplate, String template,
                    String resourceName, String characterEncoding, Map<String, Object> templateResolutionAttributes) {
                log.info("<<Template>> Name: {}, Owner: {}, Resource: {}", template, ownerTemplate, resourceName);
                return super.computeTemplateResource(
                        configuration, ownerTemplate, template, resourceName, characterEncoding,
                        templateResolutionAttributes);
            }
        };
        resolver.setApplicationContext(applicationContext);

        String resolvedTemplatesDir = templatesDir.startsWith("classpath:")
                ? templatesDir
                : "file:" + templatesDir + "/";

        resolver.setPrefix(resolvedTemplatesDir);
        resolver.setSuffix(".html");
        resolver.setTemplateMode(TemplateMode.HTML);
        resolver.setCharacterEncoding("UTF-8");
        resolver.setCacheable(false);
        resolver.setCheckExistence(true);
        return resolver;
    }

    @Bean
    public SpringTemplateEngine templateEngine() {
        SpringTemplateEngine engine = new SpringTemplateEngine();
        engine.setTemplateResolver(templateResolver());
        engine.addDialect(new LayoutDialect());
        engine.setEnableSpringELCompiler(true);
        return engine;
    }

    @Bean
    public ThymeleafViewResolver viewResolver() {
        ThymeleafViewResolver resolver = new ThymeleafViewResolver();
        resolver.setTemplateEngine(templateEngine());
        resolver.setCharacterEncoding("UTF-8");
        resolver.setCache(false);
        resolver.setOrder(1);
        return resolver;
    }
} 