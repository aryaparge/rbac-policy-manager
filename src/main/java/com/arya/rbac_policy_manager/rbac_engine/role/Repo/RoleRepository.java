package com.arya.rbac_policy_manager.rbac_engine.role.repo;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

import org.springframework.data.jpa.repository.JpaRepository;

import com.arya.rbac_policy_manager.rbac_engine.common.Enums.Status;
import com.arya.rbac_policy_manager.rbac_engine.role.entity.Role;

public interface RoleRepository extends JpaRepository<Role, UUID> {
    Optional<Role> findByName(String name);

    boolean existsByName(String name);

    Optional<Role> findByIdAndStatus( UUID id, Status status );

    List<Role> findByStatus(Status status);
}
