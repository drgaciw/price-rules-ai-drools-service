package com.example.pricerulesaidrools.drools.config;

import lombok.extern.slf4j.Slf4j;
import org.kie.api.KieBase;
import org.kie.api.KieBaseConfiguration;
import org.kie.api.KieServices;
import org.kie.api.builder.KieBuilder;
import org.kie.api.builder.KieFileSystem;
import org.kie.api.builder.KieModule;
import org.kie.api.builder.KieRepository;
import org.kie.api.builder.Message.Level;
import org.kie.api.conf.EventProcessingOption;
import org.kie.api.io.Resource;
import org.kie.api.runtime.KieContainer;
import org.kie.api.runtime.KieSession;
import org.kie.internal.io.ResourceFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.core.io.support.PathMatchingResourcePatternResolver;
import org.springframework.core.io.support.ResourcePatternResolver;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

@Configuration
@Slf4j
public class DroolsConfig {

    @Value("${drools.default-rule-path}")
    private String defaultRulePath;

    @Value("${drools.rule-execution-timeout:1000}")
    private int ruleExecutionTimeout;

    @Bean
    public KieServices kieServices() {
        return KieServices.Factory.get();
    }

    @Bean
    public KieFileSystem kieFileSystem(KieServices kieServices) throws IOException {
        KieFileSystem kieFileSystem = kieServices.newKieFileSystem();
        
        // Load rules from classpath
        ResourcePatternResolver resourcePatternResolver = new PathMatchingResourcePatternResolver();
        org.springframework.core.io.Resource[] resources = resourcePatternResolver.getResources("classpath*:" + defaultRulePath + "**/*.drl");
        
        if (resources.length == 0) {
            log.warn("No DRL files found in the default rule path: {}", defaultRulePath);
        } else {
            log.info("Found {} DRL files in the default rule path", resources.length);
        }
        
        for (org.springframework.core.io.Resource resource : resources) {
            Resource droolsResource = ResourceFactory.newInputStreamResource(
                    resource.getInputStream(),
                    "UTF-8");
            kieFileSystem.write("src/main/resources/" + resource.getFilename(), droolsResource);
            log.info("Loaded rule file: {}", resource.getFilename());
        }
        
        return kieFileSystem;
    }

    @Bean
    public KieContainer kieContainer(KieServices kieServices, KieFileSystem kieFileSystem) {
        KieRepository kieRepository = kieServices.getRepository();
        kieRepository.addKieModule(() -> kieRepository.getDefaultReleaseId());
        
        KieBuilder kieBuilder = kieServices.newKieBuilder(kieFileSystem);
        kieBuilder.buildAll();
        
        if (kieBuilder.getResults().hasMessages(Level.ERROR)) {
            List<String> errors = new ArrayList<>();
            kieBuilder.getResults().getMessages(Level.ERROR).forEach(message -> 
                errors.add(message.getText()));
            
            String errorMessage = String.join("\n", errors);
            log.error("Rule compilation errors: {}", errorMessage);
            throw new RuntimeException("Rule compilation error: " + errorMessage);
        }
        
        KieModule kieModule = kieBuilder.getKieModule();
        return kieServices.newKieContainer(kieModule.getReleaseId());
    }

    @Bean
    public KieBase kieBase(KieContainer kieContainer, KieServices kieServices) {
        KieBaseConfiguration kieBaseConfiguration = kieServices.newKieBaseConfiguration();
        kieBaseConfiguration.setOption(EventProcessingOption.STREAM);
        return kieContainer.newKieBase(kieBaseConfiguration);
    }

    @Bean
    public KieSession kieSession(KieBase kieBase) {
        KieSession kieSession = kieBase.newKieSession();
        kieSession.setGlobal("logger", log);
        return kieSession;
    }
    
    /**
     * Creates a new KieSession instance from the KieBase
     * This method can be used to create a new session for each rule execution
     * to ensure thread safety
     * 
     * @param kieBase The KieBase to create the session from
     * @return A new KieSession instance
     */
    public KieSession newKieSession(KieBase kieBase) {
        KieSession kieSession = kieBase.newKieSession();
        kieSession.setGlobal("logger", log);
        return kieSession;
    }
}