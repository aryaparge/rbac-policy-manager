package com.arya.rbac_policy_manager.rbac_engine.association.service;

import java.time.Instant;
import java.util.Optional;
import java.util.UUID;

import org.springframework.stereotype.Service;

import com.arya.rbac_policy_manager.rbac_engine.common.Enums.Status;
import com.arya.rbac_policy_manager.rbac_engine.association.entity.GroupPermission;
import com.arya.rbac_policy_manager.rbac_engine.association.repo.GroupPermissionRepository;
import com.arya.rbac_policy_manager.rbac_engine.common.exception.ActiveEntityNotFoundException;
import com.arya.rbac_policy_manager.rbac_engine.common.exception.DuplicateEntityException;
import com.arya.rbac_policy_manager.rbac_engine.common.exception.EntityNotFoundException;
import com.arya.rbac_policy_manager.rbac_engine.common.exception.InvalidEntityStateException;
import com.arya.rbac_policy_manager.rbac_engine.group.entity.Group;
import com.arya.rbac_policy_manager.rbac_engine.group.service.GroupService;
import com.arya.rbac_policy_manager.rbac_engine.permission.entity.Permission;
import com.arya.rbac_policy_manager.rbac_engine.permission.service.PermissionService;

import lombok.RequiredArgsConstructor;

@Service
@RequiredArgsConstructor
public class GroupPermissionService {

    private final GroupPermissionRepository groupPermissionRepository;
    private final GroupService groupService;
    private final PermissionService permissionService;

    public GroupPermission getActiveAssignment(UUID assignmentId) {
        GroupPermission assignment = groupPermissionRepository.findById(assignmentId)
                .orElseThrow(() -> new EntityNotFoundException("GroupPermission not found"));

        if (assignment.getStatus() != Status.ACTIVE) {
            throw new ActiveEntityNotFoundException("GroupPermission", assignmentId);
        }

        return assignment;
    }

    public GroupPermission assignPermissionToGroup(
            UUID permissionId,
            UUID groupId) {

        Group group = groupService.getGroup(groupId);
        Permission permission = permissionService.getPermission(permissionId);

        Optional<GroupPermission> existing =
                groupPermissionRepository.findByGroupAndPermission(group, permission);

        if (existing.isPresent()) {
            throw new DuplicateEntityException("GroupPermission", group.getName() + " -> " + permission.getName(), existing.get().getStatus());
        }

        GroupPermission assignment = new GroupPermission();

        assignment.setGroup(group);
        assignment.setPermission(permission);
        assignment.setStatus(Status.ACTIVE);

        return groupPermissionRepository.save(assignment);
    }

    public void disableAssignment(UUID assignmentId) {

        GroupPermission assignment = getActiveAssignment(assignmentId);

        assignment.setStatus(Status.DISABLED);
        assignment.setDisabledAt(Instant.now());

        groupPermissionRepository.save(assignment);
    }

    public void deleteAssignment(UUID assignmentId) {

        GroupPermission assignment =
                groupPermissionRepository.findById(assignmentId)
                        .orElseThrow(() -> new EntityNotFoundException("GroupPermission not found"));

        if (assignment.getStatus() == Status.ACTIVE) {
            throw new InvalidEntityStateException(
                    "Active assignment cannot be deleted. Disable it first.");
        }

        if (assignment.getStatus() == Status.DELETED) {
            throw new InvalidEntityStateException(
                    "Assignment already deleted.");
        }

        assignment.setStatus(Status.DELETED);
        assignment.setDeletedAt(Instant.now());

        groupPermissionRepository.save(assignment);
    }
}
