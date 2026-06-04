package com.arya.rbac_policy_manager.rbac_engine.permission.service;

import java.util.List;
import java.util.UUID;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.arya.rbac_policy_manager.rbac_engine.action.entity.Action;
import com.arya.rbac_policy_manager.rbac_engine.action.repo.ActionRepository;
import com.arya.rbac_policy_manager.rbac_engine.common.Enum.Status;
import com.arya.rbac_policy_manager.rbac_engine.permission.entity.Permission;
import com.arya.rbac_policy_manager.rbac_engine.permission.repo.PermissionRepository;
import com.arya.rbac_policy_manager.rbac_engine.resource.entity.Resource;
import com.arya.rbac_policy_manager.rbac_engine.resource.repo.ResourceRepository;

import lombok.RequiredArgsConstructor;

@Service
@Transactional
@RequiredArgsConstructor
public class PermissionService {

    private final PermissionRepository permissionRepository;
    private final ResourceRepository resourceRepository;
    private final ActionRepository actionRepository;

    private Permission getActivePermission(
            UUID permissionId
    ) {
        Permission permission = permissionRepository
                .findById(permissionId)
                .orElseThrow(() ->
                        new RuntimeException(
                                "Permission not found"
                        ));

        if (permission.getStatus() != Status.ACTIVE) {
            throw new RuntimeException(
                    "Permission not found"
            );
        }

        return permission;
    }

    public Permission createPermission(
            String resourceName,
            String actionName,
            String description
    ) {
        Resource resource = resourceRepository
                .findByNameAndStatus(
                        resourceName,
                        Status.ACTIVE
                )
                .orElseThrow(() ->
                        new RuntimeException(
                                "Resource not found"
                        ));

        Action action = actionRepository
                .findByNameAndStatus(
                        actionName,
                        Status.ACTIVE
                )
                .orElseThrow(() ->
                        new RuntimeException(
                                "Action not found"
                        ));

        if (permissionRepository.existsByResourceAndAction(
                resource,
                action
        )) {
            throw new RuntimeException(
                    "Permission already exists"
            );
        }

        Permission permission = new Permission();

        permission.setResource(resource);
        permission.setAction(action);
        permission.setDescription(description);
        permission.setStatus(Status.ACTIVE);

        return permissionRepository.save(permission);
    }

    @Transactional(readOnly = true)
    public Permission getPermission(
            UUID permissionId
    ) {
        return getActivePermission(permissionId);
    }

    @Transactional(readOnly = true)
    public List<Permission> getAllPermissions() {
        return permissionRepository.findByStatus(
                Status.ACTIVE
        );
    }

    public void disablePermission(
            UUID permissionId
    ) {
        Permission permission =
                getActivePermission(permissionId);

        permission.setStatus(Status.DISABLED);

        permissionRepository.save(permission);
    }

    public void deletePermission(
            UUID permissionId
    ) {
        Permission permission =
                getActivePermission(permissionId);

        permission.setStatus(Status.DELETED);

        permissionRepository.save(permission);
    }
}