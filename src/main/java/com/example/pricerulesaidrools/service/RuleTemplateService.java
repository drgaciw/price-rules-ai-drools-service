package com.example.pricerulesaidrools.service;

import com.example.pricerulesaidrools.drools.service.DroolsIntegrationService;
import lombok.RequiredArgsConstructor;
import org.drools.template.ObjectDataCompiler;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;

import java.io.IOException;
import java.io.InputStream;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.StandardOpenOption;
import java.util.List;
import java.util.Map;

/**
 * Service for generating Drools rules from templates.
 * This service handles the dynamic creation of pricing rules based on
 * predefined templates.
 */
@Service
@RequiredArgsConstructor
public class RuleTemplateService {

    private static final Logger log = LoggerFactory.getLogger(RuleTemplateService.class);
    private final DroolsIntegrationService droolsIntegrationService;

    /**
     * Generates a Drools rule file from a template and data.
     *
     * @param templatePath the classpath to the template file
     * @param data         the data to populate the template with
     * @param outputPath   the path where the generated rule file should be saved
     * @return the path to the generated rule file
     * @throws IOException if an error occurs during rule generation
     */
    public Path generateRuleFromTemplate(String templatePath, Map<String, Object> data, String outputPath)
            throws IOException {
        try (InputStream templateStream = getClass().getResourceAsStream(templatePath)) {
            if (templateStream == null) {
                throw new IOException("Template not found: " + templatePath);
            }

            ObjectDataCompiler compiler = new ObjectDataCompiler();
            String drl = compiler.compile(List.of(data), templateStream);

            Path outputFilePath = Paths.get(outputPath);
            Files.createDirectories(outputFilePath.getParent());
            Files.writeString(outputFilePath, drl, StandardOpenOption.CREATE, StandardOpenOption.TRUNCATE_EXISTING);

            log.info("Generated rule file from template {} at {}", templatePath, outputPath);

            return outputFilePath;
        }
    }

    /**
     * Generates a volume discount rule from template.
     *
     * @param data       parameters for the volume discount rule
     * @param outputPath path where the generated rule should be saved
     * @return the path to the generated rule file
     * @throws IOException if an error occurs during rule generation
     */
    public Path generateVolumeDiscountRule(Map<String, Object> data, String outputPath) throws IOException {
        return generateRuleFromTemplate("/rules/templates/volume-discount-template.drl", data, outputPath);
    }

    /**
     * Generates a commitment rule from template.
     *
     * @param data       parameters for the commitment rule
     * @param outputPath path where the generated rule should be saved
     * @return the path to the generated rule file
     * @throws IOException if an error occurs during rule generation
     */
    public Path generateCommitmentRule(Map<String, Object> data, String outputPath) throws IOException {
        return generateRuleFromTemplate("/rules/templates/commitment-rules-template.drl", data, outputPath);
    }

    /**
     * Generates a risk adjustment rule from template.
     *
     * @param data       parameters for the risk adjustment rule
     * @param outputPath path where the generated rule should be saved
     * @return the path to the generated rule file
     * @throws IOException if an error occurs during rule generation
     */
    public Path generateRiskAdjustmentRule(Map<String, Object> data, String outputPath) throws IOException {
        return generateRuleFromTemplate("/rules/templates/risk-adjustment-template.drl", data, outputPath);
    }

    /**
     * Generates a contract length incentive rule from template.
     *
     * @param data       parameters for the contract length rule
     * @param outputPath path where the generated rule should be saved
     * @return the path to the generated rule file
     * @throws IOException if an error occurs during rule generation
     */
    public Path generateContractLengthRule(Map<String, Object> data, String outputPath) throws IOException {
        return generateRuleFromTemplate("/rules/templates/contract-length-template.drl", data, outputPath);
    }

    /**
     * Reloads all rules after generating a new rule file.
     *
     * @param rulePath the path to the newly generated rule file
     * @throws Exception if an error occurs during rule reloading
     */
    public void reloadRulesAfterGeneration(Path rulePath) throws Exception {
        droolsIntegrationService.reloadRules();
        log.info("Reloaded rules after generating new rule file: {}", rulePath);
    }
}