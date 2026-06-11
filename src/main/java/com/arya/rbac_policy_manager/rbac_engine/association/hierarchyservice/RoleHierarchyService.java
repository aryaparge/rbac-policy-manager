package com.arya.rbac_policy_manager.rbac_engine.association.hierarchyservice;

import com.arya.rbac_policy_manager.rbac_engine.association.entity.RoleHierarchy;
import com.arya.rbac_policy_manager.rbac_engine.association.repo.RoleHierarchyRepository;

import com.arya.rbac_policy_manager.rbac_engine.common.Enums.Status;

import com.arya.rbac_policy_manager.rbac_engine.common.exception.ActiveEntityNotFoundException;
import com.arya.rbac_policy_manager.rbac_engine.common.exception.DuplicateEntityException;
import com.arya.rbac_policy_manager.rbac_engine.common.exception.InvalidEntityStateException;
import com.arya.rbac_policy_manager.rbac_engine.common.exception.EntityNotFoundException;

import com.arya.rbac_policy_manager.rbac_engine.role.entity.Role;
import com.arya.rbac_policy_manager.rbac_engine.role.repo.RoleRepository;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.Instant;
import java.util.Optional;
import java.util.Set;
import java.util.UUID;
import java.util.stream.Collectors;

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

                if (existing.isPresent()) {
                        throw new DuplicateEntityException("RoleHeirarchy", parent.getName() + " -> " + child.getName(),
                                        existing.get().getStatus());
                }

                validationService.validateSelfReference(parent, child);
                validationService.validateNoCycle(parent, child);
                validationService.validateDepthLimit(parent, child);

                RoleHierarchy edge = new RoleHierarchy();

                edge.setParentRole(parent);
                edge.setChildRole(child);
                edge.setName(child.getName() + "->" +parent.getName());
                edge.setStatus(Status.ACTIVE);

                roleHierarchyRepository.save(edge);

                return Optional.ofNullable(edge);
        }

        public void enableRelationship(UUID parentRoleId, UUID childRoleId) {
                RoleHierarchy edge = roleHierarchyRepository.findByParentRoleAndChildRole(null, null)
                                .orElseThrow(() -> new EntityNotFoundException("Role Heirarchy not found"));

                if (edge.getStatus() != Status.DISABLED) {
                        throw new InvalidEntityStateException(
                                        "Only disabled edges can be enabled.");
                }

                validationService.validateNoCycle(edge.getParentRole(), edge.getChildRole());
                validationService.validateDepthLimit(edge.getParentRole(), edge.getChildRole());

                edge.setStatus(Status.ACTIVE);
                edge.setDisabledAt(null); // Clear disabledAt.
                edge.setDeletedAt(null); // Clear deletedAt in case it was previously marked deleted.

                roleHierarchyRepository.save(edge);
        }

        public void disableRelationship(UUID parentRoleId, UUID childRoleId) {

                RoleHierarchy edge = getActiveRelationship(parentRoleId, childRoleId);

                edge.setStatus(Status.DISABLED);
                edge.setDisabledAt(Instant.now());
                edge.setDeletedAt(null);

                roleHierarchyRepository.save(edge);

        }

        // Manual deletion of a RolePermission assignment is not allowed. It must be
        // disabled first, then a scheduled job will permanently delete it after a
        // retention period.

        private Role getActiveRole(UUID roleId) {

                Role role = roleRepository.findById(roleId)
                                .orElseThrow(() -> new EntityNotFoundException("Role not found"));

                if (role.getStatus() != Status.ACTIVE) {
                        throw new ActiveEntityNotFoundException("Role", roleId);
                }

                return role;
        }

        private RoleHierarchy getActiveRelationship(UUID parentRoleId, UUID childRoleId) {

                Role parent = roleRepository.getReferenceById(parentRoleId);
                Role child = roleRepository.getReferenceById(childRoleId);

                return roleHierarchyRepository.findByParentRoleAndChildRoleAndStatus(parent, child, Status.ACTIVE)
                                .orElseThrow(() -> new ActiveEntityNotFoundException(
                                                "Active Role Hierarchy not found"));
        }

        public Set<Role> getActiveChildren(Role role) {
            return roleHierarchyRepository
            .findByParentRoleAndStatus(role, Status.ACTIVE)
            .stream()
            .map(RoleHierarchy::getChildRole)
            .collect(Collectors.toSet());
        }
}
