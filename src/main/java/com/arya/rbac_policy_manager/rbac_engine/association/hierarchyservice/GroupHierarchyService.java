package com.arya.rbac_policy_manager.rbac_engine.association.hierarchyservice;

import com.arya.rbac_policy_manager.rbac_engine.association.entity.GroupHierarchy;
import com.arya.rbac_policy_manager.rbac_engine.association.repo.GroupHierarchyRepository;
import com.arya.rbac_policy_manager.rbac_engine.common.Enums.Status;
import com.arya.rbac_policy_manager.rbac_engine.common.exception.ActiveEntityNotFoundException;
import com.arya.rbac_policy_manager.rbac_engine.common.exception.DuplicateEntityException;
import com.arya.rbac_policy_manager.rbac_engine.common.exception.EntityNotFoundException;
import com.arya.rbac_policy_manager.rbac_engine.group.entity.Group;
import com.arya.rbac_policy_manager.rbac_engine.group.repo.GroupRepository;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.Instant;
import java.util.List;
import java.util.Optional;
import java.util.Set;
import java.util.UUID;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
@Transactional
public class GroupHierarchyService {

        private final GroupRepository groupRepository;
        private final GroupHierarchyRepository groupHierarchyRepository;
        private final GroupHierarchyValidationService validationService;

        public Optional<GroupHierarchy> createRelationship(UUID parentGroupId, UUID childGroupId) {

                Group parent = getActiveGroup(parentGroupId);
                Group child = getActiveGroup(childGroupId);

                Optional<GroupHierarchy> existing = groupHierarchyRepository.findByParentGroupAndChildGroup(parent,
                                child);

                if (existing.isPresent()) {
                        throw new DuplicateEntityException("GroupHeirarchy",
                                        parent.getName() + " -> " + child.getName(), existing.get().getStatus());
                }

                validationService.validateSelfReference(parent, child);
                validationService.validateNoCycle(parent, child);
                validationService.validateDepthLimit(parent, child);

                GroupHierarchy edge = new GroupHierarchy();

                edge.setParentGroup(parent);
                edge.setChildGroup(child);
                edge.setName(child.getName() + "->" + parent.getName());
                edge.setStatus(Status.ACTIVE);
                edge.setDeletedAt(null);
                edge.setDisabledAt(null);

                groupHierarchyRepository.save(edge);

                return Optional.ofNullable(edge);
        }

        public void enableRelationship(UUID parentGroupId, UUID childGroupId) {
                Group parent = groupRepository.getReferenceById(parentGroupId);
                Group child = groupRepository.getReferenceById(childGroupId);

                GroupHierarchy edge = groupHierarchyRepository
                                .findByParentGroupAndChildGroupAndStatus(parent, child, Status.DISABLED)
                                .orElseThrow(() -> new EntityNotFoundException("Group Hierarchy not found"));

                validationService.validateNoCycle(edge.getParentGroup(), edge.getChildGroup());
                validationService.validateDepthLimit(edge.getParentGroup(), edge.getChildGroup());

                edge.setStatus(Status.ACTIVE);
                edge.setDisabledAt(null); // Clear disabledAt.
                edge.setDeletedAt(null); // Clear deletedAt in case it was previously marked deleted.

                groupHierarchyRepository.save(edge);
        }

        public void disableRelationship(UUID parentGroupId, UUID childGroupId) {

                GroupHierarchy edge = getActiveRelationship(parentGroupId, childGroupId);

                edge.setStatus(Status.DISABLED);
                edge.setDisabledAt(Instant.now());
                edge.setDeletedAt(null);

                groupHierarchyRepository.save(edge);

        }

        @Transactional(readOnly = true)
        public List<GroupHierarchy> getRelationshipsForParent(UUID parentGroupId) {
                Group parent = groupRepository.findById(parentGroupId)
                                .orElseThrow(() -> new EntityNotFoundException("Group not found"));
                return groupHierarchyRepository.findByParentGroup(parent);
        }

        // Manual deletion of a RolePermission assignment is not allowed. It must be
        // disabled first, then a scheduled job will permanently delete it after a
        // retention period.

        private Group getActiveGroup(UUID groupId) {

                Group group = groupRepository.findById(groupId)
                                .orElseThrow(() -> new EntityNotFoundException("Group not found"));

                if (group.getStatus() != Status.ACTIVE) {
                        throw new IllegalStateException("Group must be ACTIVE");
                }

                return group;
        }

        private GroupHierarchy getActiveRelationship(UUID parentGroupId, UUID childGroupId) {

                Group parent = groupRepository.getReferenceById(parentGroupId);
                Group child = groupRepository.getReferenceById(childGroupId);

                return groupHierarchyRepository.findByParentGroupAndChildGroupAndStatus(parent, child, Status.ACTIVE)
                                .orElseThrow(() -> new ActiveEntityNotFoundException(
                                                "Active Group Hierarchy not found"));
        }

        public Set<Group> getActiveChildren(Group group) {
                return groupHierarchyRepository
                                .findByParentGroupAndStatus(group, Status.ACTIVE)
                                .stream()
                                .map(GroupHierarchy::getChildGroup)
                                .collect(Collectors.toSet());
        }
}
