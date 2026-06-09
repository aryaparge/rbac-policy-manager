package com.arya.rbac_policy_manager.rbac_engine.group.service;

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

        group.setStatus(Status.DISABLED);
        group.setDisabledAt(Instant.now());

        groupRepository.save(group);
    }

    public void deleteGroup(UUID groupId) {
        Group group = groupRepository.findById(groupId)
                .orElseThrow(() -> new EntityNotFoundException("Group not found"));

        if (group.getStatus() == Status.ACTIVE) {
            throw new InvalidEntityStateException("Active group cannot be deleted. Consider disabling instead");
        }

        else if (group.getStatus() == Status.DELETED) {
            throw new InvalidEntityStateException("Group already deleted.");
        }

        group.setStatus(Status.DELETED);
        group.setDeletedAt(Instant.now());

        groupRepository.save(group);
    }
}