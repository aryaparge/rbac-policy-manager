package com.arya.rbac_policy_manager.rbac_engine.association.hierarchyservice;

import com.arya.rbac_policy_manager.rbac_engine.association.entity.GroupHierarchy;
import com.arya.rbac_policy_manager.rbac_engine.association.repo.GroupHierarchyRepository;
import com.arya.rbac_policy_manager.rbac_engine.common.Enums.Status;
import com.arya.rbac_policy_manager.rbac_engine.common.exception.DuplicateEntityException;
import com.arya.rbac_policy_manager.rbac_engine.common.exception.EntityNotFoundException;
import com.arya.rbac_policy_manager.rbac_engine.group.entity.Group;
import com.arya.rbac_policy_manager.rbac_engine.group.repo.GroupRepository;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.Optional;
import java.util.UUID;

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

                Optional<GroupHierarchy> existing = groupHierarchyRepository.findByParentGroupAndChildGroup(parent, child);

                if(existing.isPresent())
                {
                        throw new DuplicateEntityException("GroupHeirarchy", parent.getName() + " -> " + child.getName(), existing.get().getStatus());
                }
                
                validationService.validateSelfReference(parent, child);
                validationService.validateNoCycle(parent, child);
                validationService.validateDepthLimit(parent, child);

                GroupHierarchy edge = new GroupHierarchy();

                edge.setParentGroup(parent);
                edge.setChildGroup(child);
                edge.setStatus(Status.ACTIVE);

                groupHierarchyRepository.save(edge);

                return Optional.ofNullable(edge);
        }

        public void enableRelationship(UUID parentGroupId, UUID childGroupId) {
                GroupHierarchy edge = getRelationship(parentGroupId, childGroupId);
                enable(edge);
        }

        private void enable(GroupHierarchy edge) {// test ValidateNoCycle logic.
                validationService.validateNoCycle(edge.getParentGroup(), edge.getChildGroup());

                validationService.validateDepthLimit(edge.getParentGroup(), edge.getChildGroup());

                edge.setStatus(Status.ACTIVE);
        }

        public void restoreRelationship(UUID parentGroupId, UUID childGroupId) {
                GroupHierarchy edge = getRelationship(parentGroupId, childGroupId);
                restore(edge);
        }

        private void restore(GroupHierarchy edge) {// test ValidateNoCycle logic.
                //user should confirm restore before restoring.

                validationService.validateNoCycle(edge.getParentGroup(), edge.getChildGroup());

                validationService.validateDepthLimit(edge.getParentGroup(), edge.getChildGroup());

                edge.setStatus(Status.ACTIVE);
        }

        public void disableRelationship(UUID parentGroupId, UUID childGroupId) {

                GroupHierarchy edge = getRelationship(parentGroupId, childGroupId);

                if(edge.getStatus() == Status.ACTIVE) {
                        edge.setStatus(Status.DISABLED);
                }

                else {
                        throw new IllegalArgumentException("Only active state can be diabled.");
                }
        }

        public void deleteRelationship(UUID parentGroupId, UUID childGroupId) {

                GroupHierarchy edge = getRelationship(parentGroupId, childGroupId);
                if(edge.getStatus() == Status.DISABLED) {
                        edge.setStatus(Status.DELETED);
                }
                else if(edge.getStatus() == Status.ACTIVE) {
                        System.out.println("Active relationship cannot be directly deleted, disabling relationship.");
                        disableRelationship(parentGroupId, childGroupId);
                }
        }

        private Group getActiveGroup(UUID groupId) {

                Group group = groupRepository.findById(groupId)
                                .orElseThrow(() -> new EntityNotFoundException("Group not found"));

                if (group.getStatus() != Status.ACTIVE) {
                        throw new IllegalStateException("Group must be ACTIVE");
                }

                return group;
        }

        private GroupHierarchy getRelationship(UUID parentGroupId, UUID childGroupId) {

                Group parent = groupRepository.getReferenceById(parentGroupId);
                Group child = groupRepository.getReferenceById(childGroupId);

                return groupHierarchyRepository.findByParentGroupAndChildGroup(parent, child).orElseThrow(() -> new EntityNotFoundException("Relationship not found"));
        }
}