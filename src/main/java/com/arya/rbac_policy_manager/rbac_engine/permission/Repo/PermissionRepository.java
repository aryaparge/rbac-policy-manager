package com.arya.rbac_policy_manager.rbac_engine.permission.repo;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

import org.springframework.data.jpa.repository.JpaRepository;

import com.arya.rbac_policy_manager.rbac_engine.action.entity.Action;
import com.arya.rbac_policy_manager.rbac_engine.common.Enums.Status;
import com.arya.rbac_policy_manager.rbac_engine.permission.entity.Permission;
import com.arya.rbac_policy_manager.rbac_engine.resource.entity.Resource;

public interface PermissionRepository extends JpaRepository<Permission, UUID> {
        Optional<Permission> findByResourceAndAction( Resource resource, Action action);

        boolean existsByResourceAndAction( Resource resource, Action action);

        Optional<Permission> findByResourceAndActionAndStatus( Resource resource, Action action, Status status );

        Optional<Permission> findByIdAndStatus(UUID id, Status status);

        List<Permission> findByStatus(Status status);
}
