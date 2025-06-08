package com.example.pricerulesaidrools.model;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * Entity representing a role in the system for authorization
 */
@Entity
@Table(name = "roles")
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class Role {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "name", unique = true, nullable = false, length = 50)
    private String name;

    @Column(name = "description", length = 255)
    private String description;

    /**
     * Predefined system roles
     */
    public static final String ROLE_ADMIN = "ROLE_ADMIN";
    public static final String ROLE_RULE_MANAGER = "ROLE_RULE_MANAGER";
    public static final String ROLE_RULE_EXECUTOR = "ROLE_RULE_EXECUTOR";
    public static final String ROLE_MONITOR = "ROLE_MONITOR";
}