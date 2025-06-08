package com.example.pricerulesaidrools.repository;

import com.example.pricerulesaidrools.model.User;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

/**
 * Repository for the User entity
 */
@Repository
public interface UserRepository extends JpaRepository<User, Long> {

    /**
     * Finds a user by username
     *
     * @param username The username
     * @return The user, if found
     */
    Optional<User> findByUsername(String username);

    /**
     * Finds a user by email
     *
     * @param email The email
     * @return The user, if found
     */
    Optional<User> findByEmail(String email);

    /**
     * Finds users by enabled status
     *
     * @param enabled The enabled status
     * @return List of users with the given enabled status
     */
    List<User> findByEnabled(boolean enabled);

    /**
     * Finds users with a specific role
     *
     * @param roleName The role name
     * @return List of users with the given role
     */
    @Query("SELECT u FROM User u JOIN u.roles r WHERE r.name = :roleName")
    List<User> findByRole(@Param("roleName") String roleName);

    /**
     * Checks if a username exists
     *
     * @param username The username
     * @return True if the username exists, false otherwise
     */
    boolean existsByUsername(String username);

    /**
     * Checks if an email exists
     *
     * @param email The email
     * @return True if the email exists, false otherwise
     */
    boolean existsByEmail(String email);
}