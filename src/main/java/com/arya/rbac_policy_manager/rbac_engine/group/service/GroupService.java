package com.arya.rbac_policy_manager.rbac_engine.group.service;

import com.arya.rbac_policy_manager.rbac_engine.association.repo.GroupHierarchyRepository;
import com.arya.rbac_policy_manager.rbac_engine.association.repo.GroupPermissionRepository;
import com.arya.rbac_policy_manager.rbac_engine.association.repo.RoleGroupRepository;
import com.arya.rbac_policy_manager.rbac_engine.common.Enums.Status;
import com.arya.rbac_policy_manager.rbac_engine.common.exception.ActiveEntityNotFoundException;
import com.arya.rbac_policy_manager.rbac_engine.common.exception.DuplicateEntityException;
import com.arya.rbac_policy_manager.rbac_engine.common.exception.EntityNotFoundException;
import com.arya.rbac_policy_manager.rbac_engine.common.exception.InvalidEntityStateException;
import com.arya.rbac_policy_manager.rbac_engine.group.entity.Group;
import com.arya.rbac_policy_manager.rbac_engine.group.repo.GroupRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.Instant;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

@Service
@Transactional
@RequiredArgsConstructor
public class GroupService {
    private final GroupRepository groupRepository;

    private final GroupPermissionRepository groupPermissionRepository;
    private final RoleGroupRepository roleGroupRepository;
    private final GroupHierarchyRepository groupHierarchyRepository;

    public Group getActiveGroup(UUID groupId) {
        return groupRepository.findByIdAndStatus(
                groupId,
                Status.ACTIVE).orElseThrow(() -> new ActiveEntityNotFoundException("Group", groupId));
    }

    public Group createGroup(
            String name,
            String description) {
        Optional<Group> existing = groupRepository.findByName(name);
        if (existing.isPresent()) {
            throw new DuplicateEntityException("Group", name, existing.get().getStatus());
        }
        Group group = new Group();

        group.setName(name);
        group.setDescription(description);
        group.setStatus(Status.ACTIVE);

        return groupRepository.save(group);
    }

    public Group updateGroup(
            UUID groupId,
            String name,
            String description) {
        Group group = getActiveGroup(groupId);

        group.setName(name);
        group.setDescription(description);

        return groupRepository.save(group);
    }

    @Transactional(readOnly = true)
    public Group getGroup(UUID groupId) {
        return getActiveGroup(groupId);
    }

    @Transactional(readOnly = true)
    public List<Group> getAllGroups() {
        return groupRepository.findByStatus(Status.ACTIVE);
    }

    public void disableGroup(UUID groupId) {
        Group group = getActiveGroup(groupId);

        Instant now = Instant.now();

        group.setStatus(Status.DISABLED);
        group.setDisabledAt(now);
        group.setDeletedAt(null); // ensure deletedAt is null.
        groupRepository.save(group);

        roleGroupRepository.cascadedMarkRoleGroupsAsDisabledByGroup(groupId, now);
        groupPermissionRepository.cascadedMarkGroupPermissionsAsDisabledByGroup(groupId, now);
        groupHierarchyRepository.cascadedMarkGroupHierarchiesAsDisabledByChild(groupId, now);
        groupHierarchyRepository.cascadedMarkGroupHierarchiesAsDisabledByParent(groupId, now);
    }

    public void enableGroup(UUID groupId) {
        // Enabling a group does not automatically enable related entities. They must be
        // enabled separately if needed.
        Group group = groupRepository.findById(groupId)
                .orElseThrow(() -> new EntityNotFoundException("Group not found"));

        if (group.getStatus() == Status.ACTIVE) {
            throw new InvalidEntityStateException("Group is already active.");
        }

        else if (group.getStatus() == Status.DELETED) {
            throw new InvalidEntityStateException("Deleted group cannot be enabled.");
        }

        group.setStatus(Status.ACTIVE);
        group.setDisabledAt(null); // ensure disabledAt is null.
        group.setDeletedAt(null); // ensure deletedAt is null.

        groupRepository.save(group);
    }

    // Manual deletion of group is not allowed. It must be disabled first, then a
    // scheduled job will permanently delete it after a retention period.
}