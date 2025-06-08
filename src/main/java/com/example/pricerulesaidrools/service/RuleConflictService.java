package com.example.pricerulesaidrools.service;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.drools.core.definitions.rule.impl.RuleImpl;
import org.kie.api.KieBase;
import org.kie.api.definition.rule.Rule;
import org.springframework.stereotype.Service;

import java.util.*;
import java.util.stream.Collectors;

/**
 * Service for detecting and resolving rule conflicts in the Drools rule engine.
 * This service analyzes rules to identify potential conflicts based on patterns, salience, and conditions.
 */
@Service
@Slf4j
@RequiredArgsConstructor
public class RuleConflictService {

    private final DroolsIntegrationService droolsIntegrationService;
    
    /**
     * Represents a potential conflict between Drools rules.
     */
    public static class RuleConflict {
        private Rule rule1;
        private Rule rule2;
        private String conflictType;
        private String description;
        private int severity; // 1-10, with 10 being most severe
        private List<String> resolutionOptions;
        
        // Getters, setters, and constructor
        public RuleConflict(Rule rule1, Rule rule2, String conflictType, String description, int severity) {
            this.rule1 = rule1;
            this.rule2 = rule2;
            this.conflictType = conflictType;
            this.description = description;
            this.severity = severity;
            this.resolutionOptions = new ArrayList<>();
        }
        
        public String getRule1Name() {
            return rule1.getName();
        }
        
        public String getRule2Name() {
            return rule2.getName();
        }
        
        public String getConflictType() {
            return conflictType;
        }
        
        public String getDescription() {
            return description;
        }
        
        public int getSeverity() {
            return severity;
        }
        
        public List<String> getResolutionOptions() {
            return resolutionOptions;
        }
        
        public void addResolutionOption(String option) {
            this.resolutionOptions.add(option);
        }
    }
    
    /**
     * Detects potential conflicts in the current rule base.
     *
     * @return list of detected rule conflicts
     */
    public List<RuleConflict> detectConflicts() {
        KieBase kieBase = droolsIntegrationService.getKieSession().getKieBase();
        Collection<Rule> rules = kieBase.getKiePackages().stream()
                .flatMap(pkg -> pkg.getRules().stream())
                .collect(Collectors.toList());
        
        List<RuleConflict> conflicts = new ArrayList<>();
        
        // Check all pairs of rules for potential conflicts
        for (Rule rule1 : rules) {
            for (Rule rule2 : rules) {
                if (rule1 == rule2) continue;
                
                // Check for salience conflicts (rules with same patterns but different salience)
                conflicts.addAll(checkSalienceConflicts(rule1, rule2));
                
                // Check for redundant rules (rules with very similar patterns and actions)
                conflicts.addAll(checkRedundantRules(rule1, rule2));
                
                // Check for opposing effects (rules that might cancel each other out)
                conflicts.addAll(checkOpposingEffects(rule1, rule2));
            }
        }
        
        return conflicts;
    }
    
    /**
     * Checks for salience conflicts between rules.
     */
    private List<RuleConflict> checkSalienceConflicts(Rule rule1, Rule rule2) {
        List<RuleConflict> conflicts = new ArrayList<>();
        
        // Extract rule implementations
        RuleImpl ruleImpl1 = (RuleImpl) rule1;
        RuleImpl ruleImpl2 = (RuleImpl) rule2;
        
        // Check if rules operate on similar objects but have different salience
        boolean similarPatterns = haveSimilarPatterns(rule1, rule2);
        boolean differentSalience = ruleImpl1.getSalience().getValue() != ruleImpl2.getSalience().getValue();
        
        if (similarPatterns && differentSalience) {
            RuleConflict conflict = new RuleConflict(
                rule1, 
                rule2, 
                "SALIENCE_CONFLICT", 
                "Rules have similar patterns but different salience values which may cause unexpected execution order", 
                7
            );
            
            conflict.addResolutionOption("Review and adjust salience values to ensure desired execution order");
            conflict.addResolutionOption("Consider merging the rules if they perform similar actions");
            conflict.addResolutionOption("Add more specific constraints to differentiate the rules' intended use cases");
            
            conflicts.add(conflict);
        }
        
        return conflicts;
    }
    
    /**
     * Checks for redundant rules (rules with very similar patterns and actions).
     */
    private List<RuleConflict> checkRedundantRules(Rule rule1, Rule rule2) {
        List<RuleConflict> conflicts = new ArrayList<>();
        
        // Extract rule names and check for similarity
        String name1 = rule1.getName();
        String name2 = rule2.getName();
        
        // Check if rules are in the same package/category and have similar names
        boolean similarNames = haveSimilarNames(name1, name2);
        boolean similarPatterns = haveSimilarPatterns(rule1, rule2);
        
        if (similarNames && similarPatterns) {
            RuleConflict conflict = new RuleConflict(
                rule1, 
                rule2, 
                "REDUNDANT_RULES", 
                "Rules appear to be redundant with similar patterns and names", 
                5
            );
            
            conflict.addResolutionOption("Consider merging the rules into a single rule");
            conflict.addResolutionOption("Add documentation to clarify the intended difference between the rules");
            conflict.addResolutionOption("Rename one of the rules to better reflect its purpose");
            
            conflicts.add(conflict);
        }
        
        return conflicts;
    }
    
    /**
     * Checks for rules with opposing effects that might cancel each other out.
     */
    private List<RuleConflict> checkOpposingEffects(Rule rule1, Rule rule2) {
        List<RuleConflict> conflicts = new ArrayList<>();
        
        // This is a simplistic approach - actual implementation would need deeper analysis
        // of the rule actions, perhaps using AST or other code analysis techniques
        
        String name1 = rule1.getName().toLowerCase();
        String name2 = rule2.getName().toLowerCase();
        
        // Look for indicator terms in rule names
        boolean rule1Increases = name1.contains("increase") || name1.contains("premium") || name1.contains("add");
        boolean rule1Decreases = name1.contains("decrease") || name1.contains("discount") || name1.contains("reduce");
        boolean rule2Increases = name2.contains("increase") || name2.contains("premium") || name2.contains("add");
        boolean rule2Decreases = name2.contains("decrease") || name2.contains("discount") || name2.contains("reduce");
        
        // Check for opposing effects
        if ((rule1Increases && rule2Decreases) || (rule1Decreases && rule2Increases)) {
            if (haveSimilarPatterns(rule1, rule2)) {
                RuleConflict conflict = new RuleConflict(
                    rule1, 
                    rule2, 
                    "OPPOSING_EFFECTS", 
                    "Rules appear to have opposing effects that might cancel each other", 
                    8
                );
                
                conflict.addResolutionOption("Review the business logic to ensure both rules are needed");
                conflict.addResolutionOption("Add constraints to ensure they apply to different scenarios");
                conflict.addResolutionOption("Adjust salience to ensure a specific execution order if both are needed");
                
                conflicts.add(conflict);
            }
        }
        
        return conflicts;
    }
    
    /**
     * Simplistic check for similar patterns in rules.
     * In a real implementation, this would involve deeper analysis of the rule conditions.
     */
    private boolean haveSimilarPatterns(Rule rule1, Rule rule2) {
        // This is a simplified check that would need to be replaced with
        // more sophisticated analysis in a real implementation
        
        String name1 = rule1.getName().toLowerCase();
        String name2 = rule2.getName().toLowerCase();
        
        // Check if rules are in the same "family" based on name
        Set<String> categories = Set.of("discount", "commitment", "pricing", "tier", "risk", "contract");
        
        for (String category : categories) {
            if (name1.contains(category) && name2.contains(category)) {
                return true;
            }
        }
        
        return false;
    }
    
    /**
     * Checks if rule names are similar enough to suggest redundancy.
     */
    private boolean haveSimilarNames(String name1, String name2) {
        name1 = name1.toLowerCase();
        name2 = name2.toLowerCase();
        
        // Calculate Levenshtein distance (or similar metric)
        int distance = levenshteinDistance(name1, name2);
        int threshold = Math.min(name1.length(), name2.length()) / 3; // Arbitrary threshold
        
        return distance <= threshold;
    }
    
    /**
     * Calculates Levenshtein distance between strings.
     */
    private int levenshteinDistance(String s1, String s2) {
        int[][] dp = new int[s1.length() + 1][s2.length() + 1];
        
        for (int i = 0; i <= s1.length(); i++) {
            for (int j = 0; j <= s2.length(); j++) {
                if (i == 0) {
                    dp[i][j] = j;
                } else if (j == 0) {
                    dp[i][j] = i;
                } else {
                    dp[i][j] = min(
                        dp[i-1][j-1] + (s1.charAt(i-1) == s2.charAt(j-1) ? 0 : 1),
                        dp[i-1][j] + 1,
                        dp[i][j-1] + 1
                    );
                }
            }
        }
        
        return dp[s1.length()][s2.length()];
    }
    
    private int min(int a, int b, int c) {
        return Math.min(Math.min(a, b), c);
    }
    
    /**
     * Gets suggestions for resolving a specific rule conflict.
     *
     * @param conflict the rule conflict to resolve
     * @return list of suggested resolutions
     */
    public List<String> getSuggestedResolutions(RuleConflict conflict) {
        return conflict.getResolutionOptions();
    }
    
    /**
     * Analyzes the current rule base and provides a summary of potential issues.
     *
     * @return a summary report of rule base health
     */
    public Map<String, Object> analyzeRuleBase() {
        Map<String, Object> report = new HashMap<>();
        List<RuleConflict> conflicts = detectConflicts();
        
        // Count conflicts by severity
        Map<Integer, Long> severityCounts = conflicts.stream()
                .collect(Collectors.groupingBy(RuleConflict::getSeverity, Collectors.counting()));
        
        // Count conflicts by type
        Map<String, Long> typeCounts = conflicts.stream()
                .collect(Collectors.groupingBy(RuleConflict::getConflictType, Collectors.counting()));
        
        // Calculate overall health score (0-100)
        int healthScore = calculateHealthScore(conflicts);
        
        report.put("totalRules", droolsIntegrationService.getKieSession().getKieBase().getKiePackages().stream()
                .mapToInt(pkg -> pkg.getRules().size()).sum());
        report.put("totalConflicts", conflicts.size());
        report.put("severityCounts", severityCounts);
        report.put("typeCounts", typeCounts);
        report.put("healthScore", healthScore);
        report.put("topConflicts", conflicts.stream()
                .sorted(Comparator.comparingInt(RuleConflict::getSeverity).reversed())
                .limit(5)
                .map(c -> Map.of(
                    "rule1", c.getRule1Name(),
                    "rule2", c.getRule2Name(),
                    "type", c.getConflictType(),
                    "severity", c.getSeverity(),
                    "description", c.getDescription()
                ))
                .collect(Collectors.toList()));
        
        return report;
    }
    
    /**
     * Calculates a health score for the rule base based on detected conflicts.
     *
     * @param conflicts the list of detected conflicts
     * @return a health score between 0 and 100
     */
    private int calculateHealthScore(List<RuleConflict> conflicts) {
        if (conflicts.isEmpty()) {
            return 100; // Perfect score if no conflicts
        }
        
        // Calculate weighted severity
        double totalSeverity = conflicts.stream()
                .mapToInt(RuleConflict::getSeverity)
                .sum();
        
        int totalRules = droolsIntegrationService.getKieSession().getKieBase().getKiePackages().stream()
                .mapToInt(pkg -> pkg.getRules().size()).sum();
        
        // Basic formula: 100 - (weightedSeverity / totalRules)
        // Adjust the divisor as needed for your specific rule base
        double score = 100 - (totalSeverity / (totalRules * 2));
        
        return (int) Math.max(0, Math.min(100, score));
    }
}