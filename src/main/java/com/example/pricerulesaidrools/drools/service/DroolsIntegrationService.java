package com.example.pricerulesaidrools.drools.service;

import com.example.pricerulesaidrools.drools.dto.RuleCacheMetrics;
import com.example.pricerulesaidrools.drools.dto.RuleDeploymentResult;
import com.example.pricerulesaidrools.drools.dto.RuleExecutionMetrics;
import com.example.pricerulesaidrools.drools.dto.RuleValidationResult;
import com.example.pricerulesaidrools.drools.model.RuleSetMetadata;

import java.util.List;
import java.util.Map;

/**
 * Service interface for Drools rule engine integration
 * Provides methods for rule deployment, validation, and execution
 */
public interface DroolsIntegrationService {
    
    /**
     * Deploys a new rule set
     * 
     * @param ruleContent The rule content in DRL format
     * @return Deployment result with status and validation information
     */
    RuleDeploymentResult deployRules(String ruleContent);
    
    /**
     * Updates an existing rule set
     * 
     * @param ruleContent The updated rule content
     * @param version The version to update
     * @return Deployment result with status and validation information
     */
    RuleDeploymentResult updateRules(String ruleContent, String version);
    
    /**
     * Undeploys a rule set
     * 
     * @param version The version to undeploy
     */
    void undeployRules(String version);
    
    /**
     * Validates rule content without deploying
     * 
     * @param ruleContent The rule content to validate
     * @return Validation result with errors if any
     */
    RuleValidationResult validateRules(String ruleContent);
    
    /**
     * Validates an existing rule set
     * 
     * @param ruleSetId The rule set ID to validate
     * @return Validation result with errors if any
     */
    RuleValidationResult validateRuleSet(String ruleSetId);
    
    /**
     * Executes rules on a set of facts
     * 
     * @param ruleSetId The rule set ID to execute
     * @param facts The facts to evaluate rules against
     * @param <T> The return type
     * @return The result of rule execution
     */
    <T> T executeRules(String ruleSetId, Map<String, Object> facts);
    
    /**
     * Executes rules on multiple sets of facts (batch execution)
     * 
     * @param ruleSetId The rule set ID to execute
     * @param facts The list of fact sets to evaluate rules against
     * @param <T> The return type
     * @return List of results from rule execution
     */
    <T> List<T> executeBatchRules(String ruleSetId, List<Map<String, Object>> facts);
    
    /**
     * Gets metadata for a rule set
     * 
     * @param ruleSetId The rule set ID
     * @return Rule set metadata
     */
    RuleSetMetadata getRuleSetMetadata(String ruleSetId);
    
    /**
     * Lists all rule sets
     * 
     * @return List of rule set metadata
     */
    List<RuleSetMetadata> listRuleSets();
    
    /**
     * Reloads a rule set from storage
     * 
     * @param ruleSetId The rule set ID to reload
     */
    void reloadRuleSet(String ruleSetId);
    
    /**
     * Gets execution metrics for a rule set
     * 
     * @param ruleSetId The rule set ID
     * @return Rule execution metrics
     */
    RuleExecutionMetrics getRuleExecutionMetrics(String ruleSetId);
    
    /**
     * Gets cache metrics for a rule set
     * 
     * @param ruleSetId The rule set ID
     * @return Rule cache metrics
     */
    RuleCacheMetrics getRuleCacheMetrics(String ruleSetId);
}