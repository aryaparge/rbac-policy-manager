package com.arya.rbac_policy_manager.rbac_engine.association.service;

import java.time.Instant;
import java.util.List;
import java.util.Optional;
import java.util.Set;
import java.util.UUID;
import java.util.stream.Collectors;

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

        Optional<GroupPermission> existing = groupPermissionRepository.findByGroupAndPermission(group, permission);

        if (existing.isPresent()) {
            throw new DuplicateEntityException("GroupPermission", group.getName() + " -> " + permission.getName(),
                    existing.get().getStatus());
        }

        GroupPermission assignment = new GroupPermission();

        assignment.setGroup(group);
        assignment.setPermission(permission);
        assignment.setName(permission.getName() + "->" + group.getName());
        assignment.setStatus(Status.ACTIVE);

        return groupPermissionRepository.save(assignment);
    }

    public List<GroupPermission> getAssignmentsForGroup(UUID groupId) {
        return groupPermissionRepository.findByGroup(groupService.getGroup(groupId));
    }

    public void disableAssignment(UUID assignmentId) {
        // disabled association does not need cascading disable of related entities.
        GroupPermission assignment = getActiveAssignment(assignmentId);

        assignment.setStatus(Status.DISABLED);
        assignment.setDisabledAt(Instant.now());
        assignment.setDeletedAt(null); // Clear deletedAt.

        groupPermissionRepository.save(assignment);
    }

    public void enableAssignment(UUID assignmentId) {

        GroupPermission assignment = groupPermissionRepository.findById(assignmentId)
                .orElseThrow(() -> new EntityNotFoundException("GroupPermission not found"));

        if (assignment.getStatus() == Status.ACTIVE) {
            throw new InvalidEntityStateException(
                    "Assignment is already active.");
        }

        if (assignment.getStatus() == Status.DELETED) {
            throw new InvalidEntityStateException(
                    "Deleted assignment cannot be enabled. Create a new assignment instead.");
        }

        assignment.setStatus(Status.ACTIVE);
        assignment.setDisabledAt(null); // Clear disabledAt.
        assignment.setDeletedAt(null); // Clear deletedAt.

        groupPermissionRepository.save(assignment);
    }
    // Manual deletion of a GroupPermission assignment is not allowed. It must be
    // disabled first, then a scheduled job will permanently delete it after a
    // retention period.

        public Set<Permission> getActivePermissions(Group group) {
       return groupPermissionRepository
            .findByGroupAndStatus(group, Status.ACTIVE)
            .stream()
            .map(GroupPermission::getPermission)
            .collect(Collectors.toSet());
    }
}
