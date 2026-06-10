package com.arya.rbac_policy_manager.rbac_engine.association.service;

import java.time.Instant;
import java.util.Optional;
import java.util.UUID;

import org.springframework.stereotype.Service;

import com.arya.rbac_policy_manager.rbac_engine.association.entity.RoleGroup;
import com.arya.rbac_policy_manager.rbac_engine.association.repo.RoleGroupRepository;
import com.arya.rbac_policy_manager.rbac_engine.common.Enums.Status;
import com.arya.rbac_policy_manager.rbac_engine.common.exception.ActiveEntityNotFoundException;
import com.arya.rbac_policy_manager.rbac_engine.common.exception.DuplicateEntityException;
import com.arya.rbac_policy_manager.rbac_engine.common.exception.EntityNotFoundException;
import com.arya.rbac_policy_manager.rbac_engine.common.exception.InvalidEntityStateException;
import com.arya.rbac_policy_manager.rbac_engine.group.entity.Group;
import com.arya.rbac_policy_manager.rbac_engine.group.service.GroupService;
import com.arya.rbac_policy_manager.rbac_engine.role.entity.Role;
import com.arya.rbac_policy_manager.rbac_engine.role.service.RoleService;

import lombok.RequiredArgsConstructor;

@Service
@RequiredArgsConstructor
public class RoleGroupService {

    private final RoleGroupRepository roleGroupRepository;
    private final RoleService roleService;
    private final GroupService groupService;

    public RoleGroup getActiveAssignment(UUID assignmentId) {
        RoleGroup assignment = roleGroupRepository.findById(assignmentId)
                .orElseThrow(() -> new EntityNotFoundException("RoleGroup not found"));

        if (assignment.getStatus() != Status.ACTIVE) {
            throw new ActiveEntityNotFoundException("RoleGroup", assignmentId);
        }

        return assignment;
    }

    public RoleGroup assignRoleToGroup(
            UUID roleId,
            UUID groupId) {

        Role role = roleService.getRole(roleId);
        Group group = groupService.getGroup(groupId);

        Optional<RoleGroup> existing =
                roleGroupRepository.findByRoleAndGroup(role, group);

        if (existing.isPresent()) {
            throw new DuplicateEntityException(
                    "RoleGroup",
                    role.getName() + " -> " + group.getName(),
                    existing.get().getStatus());
        }

        RoleGroup assignment = new RoleGroup();

        assignment.setRole(role);
        assignment.setGroup(group);
        assignment.setStatus(Status.ACTIVE);

        return roleGroupRepository.save(assignment);
    }

    public void disableAssignment(UUID assignmentId) {

        RoleGroup assignment = getActiveAssignment(assignmentId);

        assignment.setStatus(Status.DISABLED);
        assignment.setDisabledAt(Instant.now());
        assignment.setDeletedAt(null); // Clear deletedAt in case it was previously marked deleted.

        roleGroupRepository.save(assignment);
    }

    public void enableAssignment(UUID assignmentId) {

        RoleGroup assignment = roleGroupRepository.findById(assignmentId)
                .orElseThrow(() -> new EntityNotFoundException("RoleGroup not found"));

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

        roleGroupRepository.save(assignment);
    }
    //Manual deletion of a RoleGroup assignment is not allowed. It must be disabled first, then a scheduled job will permanently delete it after a retention period.
}
