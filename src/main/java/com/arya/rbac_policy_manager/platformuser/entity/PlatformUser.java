package com.arya.rbac_policy_manager.platformuser.entity;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.Setter;

import java.util.UUID;

/**
 * Represents someone who can log into the RBAC administration system.
 * Intentionally has no relationship to Subject, Role, or any other
 * RBAC engine entity - authentication is kept separate from authorization.
 */
@Entity
@Getter
@Setter
@Table(name = "platform_user")
public class PlatformUser {

    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    private UUID id;

    @Column(nullable = false, unique = true, updatable = false)
    private String username;

    @Column(nullable = false)
    private String passwordHash;

    @Column(nullable = false)
    private boolean enabled = true;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private PlatformRole role;
}
