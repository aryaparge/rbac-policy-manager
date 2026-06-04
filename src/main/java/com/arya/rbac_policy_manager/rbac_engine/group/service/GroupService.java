package com.arya.rbac_policy_manager.rbac_engine.group.service;

import com.arya.rbac_policy_manager.rbac_engine.common.Enum.Status;
import com.arya.rbac_policy_manager.rbac_engine.group.entity.Group;
import com.arya.rbac_policy_manager.rbac_engine.group.repo.GroupRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import java.util.List;
import java.util.UUID;

@Service
@Transactional
@RequiredArgsConstructor
public class GroupService {
    private final GroupRepository groupRepository;

    private Group getActiveGroup(UUID groupId) {
        return groupRepository.findByIdAndStatus(
                groupId,
                Status.ACTIVE).orElseThrow(() -> new RuntimeException("Group not found"));
    }

    public Group createGroup(
            String name,
            String description) {
        if (groupRepository.existsByName(name)) {
            throw new RuntimeException("Group already exists");
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
        return groupRepository.findByStatus( Status.ACTIVE);
    }

    public void disableGroup(UUID groupId) {
        Group group = getActiveGroup(groupId);

        group.setStatus(Status.DISABLED);

        groupRepository.save(group);
    }

    public void deleteGroup(UUID groupId) {
        Group group = getActiveGroup(groupId);

        group.setStatus(Status.DELETED);

        groupRepository.save(group);
    }
}