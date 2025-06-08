package com.example.pricerulesaidrools.repository;

import com.example.pricerulesaidrools.model.Rule;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

/**
 * Repository for the Rule entity
 */
@Repository
public interface RuleRepository extends JpaRepository<Rule, Long> {

    /**
     * Finds a rule by its name, rule set ID, and version
     *
     * @param name      The rule name
     * @param ruleSetId The rule set ID
     * @param version   The rule version
     * @return The rule, if found
     */
    Optional<Rule> findByNameAndRuleSetIdAndVersion(String name, Long ruleSetId, Integer version);

    /**
     * Finds the latest version of a rule by name and rule set ID
     *
     * @param name      The rule name
     * @param ruleSetId The rule set ID
     * @return The latest version of the rule, if found
     */
    @Query("SELECT r FROM Rule r WHERE r.name = :name AND r.ruleSet.id = :ruleSetId " +
           "ORDER BY r.version DESC")
    Optional<Rule> findLatestVersionByNameAndRuleSetId(
            @Param("name") String name, 
            @Param("ruleSetId") Long ruleSetId);

    /**
     * Finds all rules in a rule set
     *
     * @param ruleSetId The rule set ID
     * @return List of rules in the rule set
     */
    List<Rule> findByRuleSetId(Long ruleSetId);

    /**
     * Finds all active rules in a rule set
     *
     * @param ruleSetId The rule set ID
     * @return List of active rules in the rule set
     */
    List<Rule> findByRuleSetIdAndActiveTrue(Long ruleSetId);

    /**
     * Finds all rules with a specific status
     *
     * @param status The rule status
     * @param pageable The pagination information
     * @return Page of rules with the given status
     */
    Page<Rule> findByStatus(Rule.RuleStatus status, Pageable pageable);

    /**
     * Finds rules containing a specific text in their content
     *
     * @param searchText The text to search for
     * @return List of rules containing the text
     */
    @Query("SELECT r FROM Rule r WHERE r.content LIKE %:searchText%")
    List<Rule> findByContentContaining(@Param("searchText") String searchText);

    /**
     * Finds all versions of a rule
     *
     * @param name      The rule name
     * @param ruleSetId The rule set ID
     * @return List of all versions of the rule
     */
    @Query("SELECT r FROM Rule r WHERE r.name = :name AND r.ruleSet.id = :ruleSetId " +
           "ORDER BY r.version ASC")
    List<Rule> findAllVersionsByNameAndRuleSetId(
            @Param("name") String name, 
            @Param("ruleSetId") Long ruleSetId);

    /**
     * Finds AI-generated rules
     *
     * @return List of AI-generated rules
     */
    List<Rule> findByAiGeneratedTrue();

    /**
     * Counts the number of rules in a rule set
     *
     * @param ruleSetId The rule set ID
     * @return The count of rules in the rule set
     */
    long countByRuleSetId(Long ruleSetId);
}