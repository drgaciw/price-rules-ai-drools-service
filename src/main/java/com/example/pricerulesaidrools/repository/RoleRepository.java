package com.example.pricerulesaidrools.repository;

import com.example.pricerulesaidrools.model.Role;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;

/**
 * Repository for the Role entity
 */
@Repository
public interface RoleRepository extends JpaRepository<Role, Long> {

    /**
     * Finds a role by name
     *
     * @param name The role name
     * @return The role, if found
     */
    Optional<Role> findByName(String name);

    /**
     * Checks if a role name exists
     *
     * @param name The role name
     * @return True if the role name exists, false otherwise
     */
    boolean existsByName(String name);
}