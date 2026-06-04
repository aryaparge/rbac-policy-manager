package com.arya.rbac_policy_manager.rbac_engine.association.hierarchyservice;

import com.arya.rbac_policy_manager.rbac_engine.association.entity.RoleHierarchy;
import com.arya.rbac_policy_manager.rbac_engine.association.repo.RoleHierarchyRepository;
import com.arya.rbac_policy_manager.rbac_engine.common.Enum.Status;
import com.arya.rbac_policy_manager.rbac_engine.role.entity.Role;
import com.arya.rbac_policy_manager.rbac_engine.role.repo.RoleRepository;

import jakarta.persistence.EntityNotFoundException;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.UUID;

@Service
@RequiredArgsConstructor
@Transactional
public class RoleHierarchyService {

        private final RoleRepository roleRepository;
        private final RoleHierarchyRepository roleHierarchyRepository;
        private final RoleHierarchyValidationService validationService;

        public void createRelationship(UUID parentRoleId, UUID childRoleId) {

                Role parent = getActiveRole(parentRoleId);
                Role child = getActiveRole(childRoleId);

                RoleHierarchy existing = getRelationship(parentRoleId, childRoleId);

                if (existing != null && existing.getStatus() == Status.ACTIVE) {
                        throw new IllegalArgumentException("Relationship already exists");
                }

                validationService.validateSelfReference(parent, child);
                validationService.validateNoCycle(parent, child);
                validationService.validateDepthLimit(parent, child);

                if (existing == null) {

                        RoleHierarchy edge = new RoleHierarchy();

                        edge.setParentRole(parent);
                        edge.setChildRole(child);
                        edge.setStatus(Status.ACTIVE);

                        roleHierarchyRepository.save(edge);
                }
                // improve the following two cases post lifecycle cleanup implementation.
                else if (existing.getStatus() == Status.DISABLED) {
                        enableRelationship(parentRoleId, childRoleId);
                }

                else if (existing.getStatus() == Status.DELETED) {

                        restoreRelationship(parentRoleId, childRoleId);
                }
        }

        public void enableRelationship(UUID parentRoleId, UUID childRoleId) {
                RoleHierarchy edge = getRelationship(parentRoleId, childRoleId);
                enable(edge);
        }

        private void enable(RoleHierarchy edge) {// test ValidateNoCycle logic.
                validationService.validateNoCycle(edge.getParentRole(), edge.getChildRole());

                validationService.validateDepthLimit(edge.getParentRole(), edge.getChildRole());

                edge.setStatus(Status.ACTIVE);
        }

        public void restoreRelationship(UUID parentRoleId, UUID childRoleId) {
                RoleHierarchy edge = getRelationship(parentRoleId, childRoleId);
                restore(edge);
        }

        private void restore(RoleHierarchy edge) {// test ValidateNoCycle logic.
                // user should confirm restore before restoring.

                validationService.validateNoCycle(edge.getParentRole(), edge.getChildRole());

                validationService.validateDepthLimit(edge.getParentRole(), edge.getChildRole());

                edge.setStatus(Status.ACTIVE);
        }

        public void disableRelationship(UUID parentRoleId, UUID childRoleId) {

                RoleHierarchy edge = getRelationship(parentRoleId, childRoleId);

                if (edge.getStatus() == Status.ACTIVE) {
                        edge.setStatus(Status.DISABLED);
                }

                else {
                        throw new IllegalArgumentException("Only active state can be diabled.");
                }
        }

        public void deleteRelationship(UUID parentRoleId, UUID childRoleId) {

                RoleHierarchy edge = getRelationship(parentRoleId, childRoleId);
                if (edge.getStatus() == Status.DISABLED) {
                        edge.setStatus(Status.DELETED);
                } else if (edge.getStatus() == Status.ACTIVE) {
                        System.out.println("Active relationship cannot be directly deleted, disabling relationship.");//change to log.
                        disableRelationship(parentRoleId, childRoleId);
                }
        }

        private Role getActiveRole(UUID roleId) {

                Role role = roleRepository.findById(roleId).orElseThrow(() -> new EntityNotFoundException("Role not found"));

                if (role.getStatus() != Status.ACTIVE) {
                        throw new IllegalStateException("Role must be ACTIVE");
                }

                return role;
        }

        private RoleHierarchy getRelationship(UUID parentRoleId, UUID childRoleId) {

                Role parent = roleRepository.getReferenceById(parentRoleId);
                Role child = roleRepository.getReferenceById(childRoleId);

                return roleHierarchyRepository.findByParentRoleAndChildRole(parent, child).orElse(null);
        }
}