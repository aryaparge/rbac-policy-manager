package com.arya.rbac_policy_manager.rbac_engine.permission.service;

import java.time.Instant;
import java.util.List;
import java.util.UUID;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.arya.rbac_policy_manager.rbac_engine.action.entity.Action;
import com.arya.rbac_policy_manager.rbac_engine.action.repo.ActionRepository;
import com.arya.rbac_policy_manager.rbac_engine.common.Enums.Status;
import com.arya.rbac_policy_manager.rbac_engine.common.exception.ActiveEntityNotFoundException;
import com.arya.rbac_policy_manager.rbac_engine.common.exception.DuplicateEntityException;
import com.arya.rbac_policy_manager.rbac_engine.common.exception.EntityNotFoundException;
import com.arya.rbac_policy_manager.rbac_engine.common.exception.InvalidEntityStateException;
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

    public Permission getActivePermission(
            UUID permissionId) {
        Permission permission = permissionRepository.findByIdAndStatus(permissionId, Status.ACTIVE)
                .orElseThrow(() -> new ActiveEntityNotFoundException("Permission", permissionId));

        return permission;
    }

    public Permission createPermission(
            UUID actionId,
            UUID resourceId,
            String description) {
        Resource resource = resourceRepository
                .findByIdAndStatus(resourceId, Status.ACTIVE)
                .orElseThrow(() -> new ActiveEntityNotFoundException("Resource", resourceId));

        Action action = actionRepository.findByIdAndStatus(actionId, Status.ACTIVE)
                .orElseThrow(() -> new ActiveEntityNotFoundException("Action", actionId));

        if (permissionRepository.existsByResourceAndAction(resource, action)) {
            throw new DuplicateEntityException("Permission", action.getName() + "_" + resource.getName(),
                    Status.ACTIVE);
        }

        Permission permission = new Permission();

        permission.setResource(resource);
        permission.setAction(action);
        permission.setName(action.getName() + "_" + resource.getName());
        permission.setDescription(description);
        permission.setStatus(Status.ACTIVE);

        return permissionRepository.save(permission);
    }

    @Transactional(readOnly = true)
    public Permission getPermission(UUID permissionId) {
        return getActivePermission(permissionId);
    }

    @Transactional(readOnly = true)
    public List<Permission> getAllPermissions() {
        return permissionRepository.findByStatus(Status.ACTIVE);
    }

    public void disablePermission(UUID permissionId) {
        Permission permission = getActivePermission(permissionId);

        permission.setStatus(Status.DISABLED);
        permission.setDisabledAt(Instant.now());
        permission.setDeletedAt(null); //ensure deletedAt is null.

        permissionRepository.save(permission);
    }

    public void enablePermission(UUID permissionId) {
        Permission permission = permissionRepository.findByIdAndStatus(permissionId, Status.DISABLED)
                .orElseThrow(() -> new ActiveEntityNotFoundException("Disabled Permission", permissionId));

        permission.setStatus(Status.ACTIVE);
        permission.setDisabledAt(null);
        permission.setDeletedAt(null);

        permissionRepository.save(permission);
    }

    public void deletePermission(UUID permissionId) {
        Permission permission = permissionRepository.findById(permissionId)
                .orElseThrow(() -> new EntityNotFoundException("Permission not found"));

        if (permission.getStatus() == Status.ACTIVE) {
            throw new InvalidEntityStateException("Active permission cannot be deleted. Consider disabling instead");
        }

        else if (permission.getStatus() == Status.DELETED) {
            throw new InvalidEntityStateException("Permission already deleted.");
        }

        permission.setStatus(Status.DELETED);

        permissionRepository.save(permission);
    }
}