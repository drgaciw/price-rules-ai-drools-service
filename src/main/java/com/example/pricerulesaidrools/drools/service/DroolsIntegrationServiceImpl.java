package com.example.pricerulesaidrools.drools.service;

import com.example.pricerulesaidrools.drools.config.DroolsConfig;
import com.example.pricerulesaidrools.drools.dto.RuleCacheMetrics;
import com.example.pricerulesaidrools.drools.dto.RuleDeploymentResult;
import com.example.pricerulesaidrools.drools.dto.RuleExecutionMetrics;
import com.example.pricerulesaidrools.drools.dto.RuleValidationResult;
import com.example.pricerulesaidrools.drools.model.RuleSetMetadata;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.codec.digest.DigestUtils;
import org.drools.core.impl.InternalKnowledgeBase;
import org.kie.api.KieBase;
import org.kie.api.KieServices;
import org.kie.api.builder.KieBuilder;
import org.kie.api.builder.KieFileSystem;
import org.kie.api.builder.Message;
import org.kie.api.builder.Results;
import org.kie.api.io.ResourceType;
import org.kie.api.runtime.KieSession;
import org.kie.internal.io.ResourceFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.cache.annotation.CacheEvict;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.*;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.atomic.AtomicLong;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
@Slf4j
public class DroolsIntegrationServiceImpl implements DroolsIntegrationService {

    private final DroolsConfig droolsConfig;
    private final KieServices kieServices;
    private final KieBase kieBase;
    private final RedisTemplate<String, Object> redisTemplate;
    
    @Value("${drools.rule-expiration:3600}")
    private int ruleExpiration;
    
    @Value("${drools.rule-execution-timeout:1000}")
    private int ruleExecutionTimeout;
    
    // In-memory stores for rule metadata and metrics (would be replaced with DB in production)
    private final Map<String, RuleSetMetadata> ruleSetMetadataMap = new ConcurrentHashMap<>();
    private final Map<String, RuleExecutionMetrics> executionMetricsMap = new ConcurrentHashMap<>();
    private final Map<String, Long> cacheHitsMap = new ConcurrentHashMap<>();
    private final Map<String, Long> cacheMissesMap = new ConcurrentHashMap<>();
    private final Map<String, AtomicLong> executionCountMap = new ConcurrentHashMap<>();

    @Override
    public RuleDeploymentResult deployRules(String ruleContent) {
        String ruleId = DigestUtils.md5Hex(ruleContent);
        
        try {
            // Validate rules first
            RuleValidationResult validationResult = validateRules(ruleContent);
            if (!validationResult.isValid()) {
                return RuleDeploymentResult.builder()
                        .id(ruleId)
                        .successful(false)
                        .message("Rule validation failed")
                        .validationErrors(validationResult.getErrors())
                        .build();
            }
            
            // Create a KieFileSystem with the rule content
            KieFileSystem kfs = kieServices.newKieFileSystem();
            kfs.write("src/main/resources/rules/rule_" + ruleId + ".drl", 
                    ResourceFactory.newByteArrayResource(ruleContent.getBytes()));
            
            // Build the rules
            KieBuilder kieBuilder = kieServices.newKieBuilder(kfs);
            kieBuilder.buildAll();
            
            // Check for errors
            Results results = kieBuilder.getResults();
            if (results.hasMessages(Message.Level.ERROR)) {
                List<RuleDeploymentResult.ValidationError> errors = results.getMessages(Message.Level.ERROR)
                        .stream()
                        .map(message -> RuleDeploymentResult.ValidationError.builder()
                                .code("COMPILATION_ERROR")
                                .message(message.getText())
                                .severity(RuleDeploymentResult.ValidationError.Severity.ERROR)
                                .build())
                        .collect(Collectors.toList());
                
                return RuleDeploymentResult.builder()
                        .id(ruleId)
                        .successful(false)
                        .message("Rule compilation failed")
                        .validationErrors(errors)
                        .build();
            }
            
            // Store metadata
            RuleSetMetadata metadata = RuleSetMetadata.builder()
                    .id(ruleId)
                    .name("Rule_" + ruleId)
                    .version("1.0")
                    .status(RuleSetMetadata.RuleStatus.ACTIVE)
                    .createdDate(LocalDateTime.now())
                    .lastUpdated(LocalDateTime.now())
                    .build();
            
            ruleSetMetadataMap.put(ruleId, metadata);
            
            // Initialize metrics
            executionMetricsMap.put(ruleId, RuleExecutionMetrics.builder()
                    .ruleSetId(ruleId)
                    .totalExecutions(0)
                    .averageExecutionTimeMs(0)
                    .cacheHitRate(0)
                    .errorRate(0)
                    .build());
            
            executionCountMap.put(ruleId, new AtomicLong(0));
            cacheHitsMap.put(ruleId, 0L);
            cacheMissesMap.put(ruleId, 0L);
            
            // Store the rule in Redis cache
            redisTemplate.opsForValue().set("rule:" + ruleId, ruleContent);
            redisTemplate.expire("rule:" + ruleId, ruleExpiration, java.util.concurrent.TimeUnit.SECONDS);
            
            return RuleDeploymentResult.builder()
                    .id(ruleId)
                    .ruleSetId(ruleId)
                    .successful(true)
                    .message("Rule deployed successfully")
                    .build();
            
        } catch (Exception e) {
            log.error("Error deploying rules", e);
            return RuleDeploymentResult.builder()
                    .id(ruleId)
                    .successful(false)
                    .message("Error deploying rules: " + e.getMessage())
                    .build();
        }
    }

    @Override
    public RuleDeploymentResult updateRules(String ruleContent, String version) {
        String ruleId = DigestUtils.md5Hex(ruleContent);
        
        // Check if rule exists
        if (!ruleSetMetadataMap.containsKey(ruleId)) {
            return RuleDeploymentResult.builder()
                    .id(ruleId)
                    .successful(false)
                    .message("Rule set not found")
                    .build();
        }
        
        try {
            // Validate rules first
            RuleValidationResult validationResult = validateRules(ruleContent);
            if (!validationResult.isValid()) {
                return RuleDeploymentResult.builder()
                        .id(ruleId)
                        .successful(false)
                        .message("Rule validation failed")
                        .validationErrors(validationResult.getErrors())
                        .build();
            }
            
            // Update metadata
            RuleSetMetadata metadata = ruleSetMetadataMap.get(ruleId);
            metadata.setVersion(version);
            metadata.setLastUpdated(LocalDateTime.now());
            ruleSetMetadataMap.put(ruleId, metadata);
            
            // Update the rule in Redis cache
            redisTemplate.opsForValue().set("rule:" + ruleId, ruleContent);
            redisTemplate.expire("rule:" + ruleId, ruleExpiration, java.util.concurrent.TimeUnit.SECONDS);
            
            // Clear KieBase cache
            ((InternalKnowledgeBase) kieBase).clearRules();
            
            return RuleDeploymentResult.builder()
                    .id(ruleId)
                    .ruleSetId(ruleId)
                    .successful(true)
                    .message("Rule updated successfully")
                    .build();
            
        } catch (Exception e) {
            log.error("Error updating rules", e);
            return RuleDeploymentResult.builder()
                    .id(ruleId)
                    .successful(false)
                    .message("Error updating rules: " + e.getMessage())
                    .build();
        }
    }

    @Override
    @CacheEvict(value = "drools-rules", key = "#version")
    public void undeployRules(String version) {
        // Find rule set by version
        Optional<String> ruleSetIdOpt = ruleSetMetadataMap.entrySet().stream()
                .filter(entry -> entry.getValue().getVersion().equals(version))
                .map(Map.Entry::getKey)
                .findFirst();
        
        if (ruleSetIdOpt.isPresent()) {
            String ruleSetId = ruleSetIdOpt.get();
            
            // Update metadata
            RuleSetMetadata metadata = ruleSetMetadataMap.get(ruleSetId);
            metadata.setStatus(RuleSetMetadata.RuleStatus.DELETED);
            ruleSetMetadataMap.put(ruleSetId, metadata);
            
            // Remove from Redis cache
            redisTemplate.delete("rule:" + ruleSetId);
            
            log.info("Rule set with ID {} undeployed", ruleSetId);
        } else {
            log.warn("Rule set with version {} not found", version);
        }
    }

    @Override
    public RuleValidationResult validateRules(String ruleContent) {
        long startTime = System.currentTimeMillis();
        List<RuleDeploymentResult.ValidationError> errors = new ArrayList<>();
        
        try {
            // Create a KieFileSystem with the rule content
            KieFileSystem kfs = kieServices.newKieFileSystem();
            kfs.write("src/main/resources/rules/validation.drl", 
                    ResourceFactory.newByteArrayResource(ruleContent.getBytes()));
            
            // Build the rules without adding to the KieContainer
            KieBuilder kieBuilder = kieServices.newKieBuilder(kfs);
            kieBuilder.buildAll();
            
            // Check for errors
            Results results = kieBuilder.getResults();
            if (results.hasMessages(Message.Level.ERROR)) {
                errors = results.getMessages(Message.Level.ERROR)
                        .stream()
                        .map(message -> RuleDeploymentResult.ValidationError.builder()
                                .code("COMPILATION_ERROR")
                                .message(message.getText())
                                .severity(RuleDeploymentResult.ValidationError.Severity.ERROR)
                                .build())
                        .collect(Collectors.toList());
            }
            
            // Check for warnings
            if (results.hasMessages(Message.Level.WARNING)) {
                List<RuleDeploymentResult.ValidationError> warnings = results.getMessages(Message.Level.WARNING)
                        .stream()
                        .map(message -> RuleDeploymentResult.ValidationError.builder()
                                .code("COMPILATION_WARNING")
                                .message(message.getText())
                                .severity(RuleDeploymentResult.ValidationError.Severity.WARNING)
                                .build())
                        .collect(Collectors.toList());
                
                errors.addAll(warnings);
            }
            
        } catch (Exception e) {
            log.error("Error validating rules", e);
            errors.add(RuleDeploymentResult.ValidationError.builder()
                    .code("VALIDATION_ERROR")
                    .message(e.getMessage())
                    .severity(RuleDeploymentResult.ValidationError.Severity.ERROR)
                    .build());
        }
        
        long validationTime = System.currentTimeMillis() - startTime;
        
        return RuleValidationResult.builder()
                .isValid(errors.stream().noneMatch(e -> e.getSeverity() == RuleDeploymentResult.ValidationError.Severity.ERROR))
                .errors(errors)
                .validationTimeMs(validationTime)
                .build();
    }

    @Override
    public RuleValidationResult validateRuleSet(String ruleSetId) {
        // Check if rule set exists
        if (!ruleSetMetadataMap.containsKey(ruleSetId)) {
            List<RuleDeploymentResult.ValidationError> errors = new ArrayList<>();
            errors.add(RuleDeploymentResult.ValidationError.builder()
                    .code("RULE_SET_NOT_FOUND")
                    .message("Rule set with ID " + ruleSetId + " not found")
                    .severity(RuleDeploymentResult.ValidationError.Severity.ERROR)
                    .build());
            
            return RuleValidationResult.builder()
                    .isValid(false)
                    .errors(errors)
                    .validationTimeMs(0)
                    .build();
        }
        
        // Get rule content from Redis
        String ruleContent = (String) redisTemplate.opsForValue().get("rule:" + ruleSetId);
        
        if (ruleContent == null) {
            List<RuleDeploymentResult.ValidationError> errors = new ArrayList<>();
            errors.add(RuleDeploymentResult.ValidationError.builder()
                    .code("RULE_CONTENT_NOT_FOUND")
                    .message("Rule content for ID " + ruleSetId + " not found in cache")
                    .severity(RuleDeploymentResult.ValidationError.Severity.ERROR)
                    .build());
            
            return RuleValidationResult.builder()
                    .isValid(false)
                    .errors(errors)
                    .validationTimeMs(0)
                    .build();
        }
        
        return validateRules(ruleContent);
    }

    @Override
    @SuppressWarnings("unchecked")
    @Cacheable(value = "drools-execution", key = "{#ruleSetId, #facts.hashCode()}")
    public <T> T executeRules(String ruleSetId, Map<String, Object> facts) {
        long startTime = System.currentTimeMillis();
        boolean success = true;
        
        // Check if rule set exists
        if (!ruleSetMetadataMap.containsKey(ruleSetId)) {
            log.error("Rule set with ID {} not found", ruleSetId);
            cacheHitsMap.put(ruleSetId, cacheHitsMap.getOrDefault(ruleSetId, 0L));
            cacheMissesMap.put(ruleSetId, cacheMissesMap.getOrDefault(ruleSetId, 0L) + 1);
            return null;
        }
        
        // Create a new KieSession
        KieSession kieSession = droolsConfig.newKieSession(kieBase);
        T result = null;
        
        try {
            // Insert facts
            facts.forEach(kieSession::insert);
            
            // Set global variables if needed
            kieSession.setGlobal("logger", log);
            
            // Fire rules
            kieSession.fireAllRules();
            
            // Get result - assuming there's a specific fact of type T we want to return
            // In a real application, we'd have a more structured approach to handle results
            // This is a simplified version
            result = (T) facts.values().stream()
                    .filter(fact -> fact instanceof Map && ((Map<?, ?>) fact).containsKey("result"))
                    .findFirst()
                    .orElse(null);
            
            // Update metrics
            executionCountMap.computeIfAbsent(ruleSetId, k -> new AtomicLong(0)).incrementAndGet();
            updateExecutionMetrics(ruleSetId, System.currentTimeMillis() - startTime, success);
            
        } catch (Exception e) {
            log.error("Error executing rules", e);
            success = false;
            updateExecutionMetrics(ruleSetId, System.currentTimeMillis() - startTime, success);
        } finally {
            kieSession.dispose();
        }
        
        return result;
    }

    @Override
    @SuppressWarnings("unchecked")
    public <T> List<T> executeBatchRules(String ruleSetId, List<Map<String, Object>> facts) {
        List<T> results = new ArrayList<>();
        
        // Execute each set of facts
        for (Map<String, Object> factSet : facts) {
            T result = executeRules(ruleSetId, factSet);
            results.add(result);
        }
        
        return results;
    }

    @Override
    public RuleSetMetadata getRuleSetMetadata(String ruleSetId) {
        return ruleSetMetadataMap.get(ruleSetId);
    }

    @Override
    public List<RuleSetMetadata> listRuleSets() {
        return new ArrayList<>(ruleSetMetadataMap.values());
    }

    @Override
    @CacheEvict(value = "drools-rules", key = "#ruleSetId")
    public void reloadRuleSet(String ruleSetId) {
        // Check if rule set exists
        if (!ruleSetMetadataMap.containsKey(ruleSetId)) {
            log.error("Rule set with ID {} not found", ruleSetId);
            return;
        }
        
        // Get rule content from Redis
        String ruleContent = (String) redisTemplate.opsForValue().get("rule:" + ruleSetId);
        
        if (ruleContent == null) {
            log.error("Rule content for ID {} not found in cache", ruleSetId);
            return;
        }
        
        try {
            // Create a KieFileSystem with the rule content
            KieFileSystem kfs = kieServices.newKieFileSystem();
            kfs.write("src/main/resources/rules/rule_" + ruleSetId + ".drl", 
                    ResourceFactory.newByteArrayResource(ruleContent.getBytes()));
            
            // Build the rules
            KieBuilder kieBuilder = kieServices.newKieBuilder(kfs);
            kieBuilder.buildAll();
            
            // Clear KieBase cache
            ((InternalKnowledgeBase) kieBase).clearRules();
            
            log.info("Rule set with ID {} reloaded", ruleSetId);
            
        } catch (Exception e) {
            log.error("Error reloading rule set with ID {}", ruleSetId, e);
        }
    }

    @Override
    public RuleExecutionMetrics getRuleExecutionMetrics(String ruleSetId) {
        return executionMetricsMap.getOrDefault(ruleSetId, RuleExecutionMetrics.builder()
                .ruleSetId(ruleSetId)
                .build());
    }

    @Override
    public RuleCacheMetrics getRuleCacheMetrics(String ruleSetId) {
        long hits = cacheHitsMap.getOrDefault(ruleSetId, 0L);
        long misses = cacheMissesMap.getOrDefault(ruleSetId, 0L);
        long total = hits + misses;
        double hitRate = total > 0 ? (double) hits / total : 0;
        
        return RuleCacheMetrics.builder()
                .ruleSetId(ruleSetId)
                .cacheHits(hits)
                .cacheMisses(misses)
                .hitRate(hitRate)
                .cacheSize(1) // Simplified - in reality would track actual cache size
                .maxCacheSize(100) // Simplified - in reality would get from config
                .build();
    }
    
    private void updateExecutionMetrics(String ruleSetId, long executionTime, boolean success) {
        RuleExecutionMetrics metrics = executionMetricsMap.getOrDefault(ruleSetId, 
                RuleExecutionMetrics.builder().ruleSetId(ruleSetId).build());
        
        long totalExecutions = metrics.getTotalExecutions() + 1;
        double avgTime = ((metrics.getAverageExecutionTimeMs() * (totalExecutions - 1)) + executionTime) / totalExecutions;
        double errorRate = success ? metrics.getErrorRate() * (totalExecutions - 1) / totalExecutions 
                : (metrics.getErrorRate() * (totalExecutions - 1) + 1) / totalExecutions;
        
        metrics.setTotalExecutions(totalExecutions);
        metrics.setAverageExecutionTimeMs(avgTime);
        metrics.setLastExecutionTimeMs(executionTime);
        metrics.setPeakExecutionTimeMs(Math.max(metrics.getPeakExecutionTimeMs(), executionTime));
        metrics.setErrorRate(errorRate);
        
        // Update cache metrics
        long cacheHits = cacheHitsMap.getOrDefault(ruleSetId, 0L);
        long cacheMisses = cacheMissesMap.getOrDefault(ruleSetId, 0L);
        long total = cacheHits + cacheMisses;
        metrics.setCacheHitRate(total > 0 ? (double) cacheHits / total : 0);
        
        executionMetricsMap.put(ruleSetId, metrics);
    }
}