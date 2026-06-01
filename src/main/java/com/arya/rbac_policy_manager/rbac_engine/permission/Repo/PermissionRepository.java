package com.arya.rbac_policy_manager.rbac_engine.permission.Repo;

import java.util.Optional;
import java.util.UUID;

import org.springframework.data.jpa.repository.JpaRepository;

import com.arya.rbac_policy_manager.rbac_engine.action.Entity.Action;
import com.arya.rbac_policy_manager.rbac_engine.permission.Entity.Permission;
import com.arya.rbac_policy_manager.rbac_engine.resource.Entity.Resource;

public interface PermissionRepository extends JpaRepository<Permission, UUID> {
    Optional<Permission> findByResourceAndAction(
            Resource resource,
            Action action);

    boolean existsByResourceAndAction(
            Resource resource,
            Action action);
}
