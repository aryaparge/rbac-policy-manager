package com.arya.rbac_policy_manager.rbac_engine.association.hierarchyservice;

import com.arya.rbac_policy_manager.rbac_engine.association.entity.RoleHierarchy;
import com.arya.rbac_policy_manager.rbac_engine.association.repo.RoleHierarchyRepository;
import com.arya.rbac_policy_manager.rbac_engine.common.Enums.Status;
import com.arya.rbac_policy_manager.rbac_engine.common.exception.DuplicateEntityException;
import com.arya.rbac_policy_manager.rbac_engine.role.entity.Role;
import com.arya.rbac_policy_manager.rbac_engine.role.repo.RoleRepository;

import jakarta.persistence.EntityNotFoundException;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.Optional;
import java.util.UUID;

@Service
@RequiredArgsConstructor
@Transactional
public class RoleHierarchyService {

        private final RoleRepository roleRepository;
        private final RoleHierarchyRepository roleHierarchyRepository;
        private final RoleHierarchyValidationService validationService;

        public Optional<RoleHierarchy> createRelationship(UUID parentRoleId, UUID childRoleId) {

                Role parent = getActiveRole(parentRoleId);
                Role child = getActiveRole(childRoleId);

                Optional<RoleHierarchy> existing = roleHierarchyRepository.findByParentRoleAndChildRole(parent, child);

                if(existing.isPresent())
                {
                        throw new DuplicateEntityException("RoleHeirarchy", parent.getName() + " -> " + child.getName(), existing.get().getStatus());
                }

                validationService.validateSelfReference(parent, child);
                validationService.validateNoCycle(parent, child);
                validationService.validateDepthLimit(parent, child);

                RoleHierarchy edge = new RoleHierarchy();

                edge.setParentRole(parent);
                edge.setChildRole(child);
                edge.setStatus(Status.ACTIVE);

                roleHierarchyRepository.save(edge);

                return Optional.ofNullable(edge);
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
                        System.out.println("Active relationship cannot be directly deleted, disabling relationship.");// change
                                                                                                                      // to
                                                                                                                      // log.
                        disableRelationship(parentRoleId, childRoleId);
                }
        }

        private Role getActiveRole(UUID roleId) {

                Role role = roleRepository.findById(roleId)
                                .orElseThrow(() -> new EntityNotFoundException("Role not found"));

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