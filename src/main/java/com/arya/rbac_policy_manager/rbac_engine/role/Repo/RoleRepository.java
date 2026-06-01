package com.arya.rbac_policy_manager.rbac_engine.role.Repo;

import java.util.Optional;
import java.util.UUID;

import org.springframework.data.jpa.repository.JpaRepository;
import com.arya.rbac_policy_manager.rbac_engine.role.Entity.Role;

public interface RoleRepository extends JpaRepository<Role, UUID> {
    Optional<Role> findByName(String name);

    boolean existsByName(String name);
}
