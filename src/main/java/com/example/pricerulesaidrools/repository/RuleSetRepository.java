package com.example.pricerulesaidrools.repository;

import com.example.pricerulesaidrools.model.RuleSet;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

/**
 * Repository for the RuleSet entity
 */
@Repository
public interface RuleSetRepository extends JpaRepository<RuleSet, Long> {

    /**
     * Finds a rule set by name
     *
     * @param name The name of the rule set
     * @return The rule set, if found
     */
    Optional<RuleSet> findByName(String name);

    /**
     * Finds all active rule sets
     *
     * @return List of active rule sets
     */
    List<RuleSet> findByActiveTrue();

    /**
     * Finds rule sets created by a specific user
     *
     * @param userId The user ID
     * @return List of rule sets created by the user
     */
    @Query("SELECT rs FROM RuleSet rs WHERE rs.createdBy.id = :userId")
    List<RuleSet> findByCreatedBy(@Param("userId") Long userId);

    /**
     * Counts the number of rule sets with a given name
     *
     * @param name The name to search for
     * @return The count of rule sets with the given name
     */
    long countByName(String name);

    /**
     * Finds rule sets containing rules with a specific name pattern
     *
     * @param pattern The pattern to search for
     * @return List of rule sets containing matching rules
     */
    @Query("SELECT DISTINCT rs FROM RuleSet rs JOIN rs.rules r WHERE r.name LIKE %:pattern%")
    List<RuleSet> findByRuleNameContaining(@Param("pattern") String pattern);
}