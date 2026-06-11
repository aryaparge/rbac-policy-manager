package com.arya.rbac_policy_manager.rbac_engine.association.service;

import java.time.Instant;
import java.util.Optional;
import java.util.Set;
import java.util.UUID;
import java.util.stream.Collectors;

import org.springframework.stereotype.Service;

import com.arya.rbac_policy_manager.rbac_engine.association.entity.RolePermission;
import com.arya.rbac_policy_manager.rbac_engine.association.repo.RolePermissionRepository;
import com.arya.rbac_policy_manager.rbac_engine.common.Enums.Status;
import com.arya.rbac_policy_manager.rbac_engine.common.exception.ActiveEntityNotFoundException;
import com.arya.rbac_policy_manager.rbac_engine.common.exception.DuplicateEntityException;
import com.arya.rbac_policy_manager.rbac_engine.common.exception.EntityNotFoundException;
import com.arya.rbac_policy_manager.rbac_engine.common.exception.InvalidEntityStateException;
import com.arya.rbac_policy_manager.rbac_engine.permission.entity.Permission;
import com.arya.rbac_policy_manager.rbac_engine.permission.service.PermissionService;
import com.arya.rbac_policy_manager.rbac_engine.role.entity.Role;
import com.arya.rbac_policy_manager.rbac_engine.role.service.RoleService;

import lombok.RequiredArgsConstructor;

@Service
@RequiredArgsConstructor
public class RolePermissionService {

    private final RolePermissionRepository rolePermissionRepository;
    private final RoleService roleService;
    private final PermissionService permissionService;

    public RolePermission getActiveAssignment(UUID assignmentId) {
        RolePermission assignment = rolePermissionRepository.findById(assignmentId)
                .orElseThrow(() -> new EntityNotFoundException("RolePermission not found"));

        if (assignment.getStatus() != Status.ACTIVE) {
            throw new ActiveEntityNotFoundException("RolePermission", assignmentId);
        }

        return assignment;
    }

    public RolePermission assignPermissionToRole(
            UUID permissionId,
            UUID roleId) {

        Role role = roleService.getRole(roleId);
        Permission permission = permissionService.getPermission(permissionId);

        Optional<RolePermission> existing =
                rolePermissionRepository.findByRoleAndPermission(role, permission);

        if (existing.isPresent()) {
            throw new DuplicateEntityException(
                    "RolePermission",
                    role.getName() + " -> " + permission.getName(),
                    existing.get().getStatus());
        }

        RolePermission assignment = new RolePermission();

        assignment.setRole(role);
        assignment.setPermission(permission);
        assignment.setName(permission.getName() + "->" + role.getName());
        assignment.setStatus(Status.ACTIVE);

        return rolePermissionRepository.save(assignment);
    }

    public void disableAssignment(UUID assignmentId) {

        RolePermission assignment = getActiveAssignment(assignmentId);

        assignment.setStatus(Status.DISABLED);
        assignment.setDisabledAt(Instant.now());
        assignment.setDeletedAt(null); // Clear deletedAt in case it was previously marked deleted.

        rolePermissionRepository.save(assignment);
    }

    public void enableAssignment(UUID assignmentId) {

        RolePermission assignment = rolePermissionRepository.findById(assignmentId)
                .orElseThrow(() -> new EntityNotFoundException("RolePermission not found"));

        if (assignment.getStatus() != Status.DISABLED) {
            throw new InvalidEntityStateException(
                    "Only disabled assignments can be enabled.");
        }

        assignment.setStatus(Status.ACTIVE);
        assignment.setDisabledAt(null); // Clear disabledAt.
        assignment.setDeletedAt(null); // Clear deletedAt in case it was previously marked deleted.

        rolePermissionRepository.save(assignment);
    }

    public Set<Permission> getActivePermissions(Role role) {
       return rolePermissionRepository
            .findByRoleAndStatus(role, Status.ACTIVE)
            .stream()
            .map(RolePermission::getPermission)
            .collect(Collectors.toSet());
    }
    //Manual deletion of a RolePermission assignment is not allowed. It must be disabled first, then a scheduled job will permanently delete it after a retention period.
}