package com.arya.rbac_policy_manager.rbac_engine.role.service;

import com.arya.rbac_policy_manager.rbac_engine.association.repo.RoleGroupRepository;
import com.arya.rbac_policy_manager.rbac_engine.association.repo.RoleHierarchyRepository;
import com.arya.rbac_policy_manager.rbac_engine.association.repo.RolePermissionRepository;
import com.arya.rbac_policy_manager.rbac_engine.association.repo.SubjectRoleRepository;
import com.arya.rbac_policy_manager.rbac_engine.common.Enums.Status;
import com.arya.rbac_policy_manager.rbac_engine.common.exception.ActiveEntityNotFoundException;
import com.arya.rbac_policy_manager.rbac_engine.common.exception.DuplicateEntityException;
import com.arya.rbac_policy_manager.rbac_engine.common.exception.EntityNotFoundException;
import com.arya.rbac_policy_manager.rbac_engine.common.exception.InvalidEntityStateException;
import com.arya.rbac_policy_manager.rbac_engine.role.entity.Role;
import com.arya.rbac_policy_manager.rbac_engine.role.repo.RoleRepository;

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
public class RoleService {
    private final RoleRepository roleRepository;

    private final RolePermissionRepository rolePermissionRepository;
    private final RoleGroupRepository roleGroupRepository;
    private final SubjectRoleRepository subjectRoleRepository;
    private final RoleHierarchyRepository roleHierarchyRepository;

    public Role getActiveRole(UUID roleId) {
        return roleRepository.findByIdAndStatus(roleId, Status.ACTIVE)
                .orElseThrow(() -> new ActiveEntityNotFoundException("Role", roleId));
    }

    public Role createRole(
            String name,
            String description) {
        Optional<Role> existing = roleRepository.findByName(name);

        if (existing.isPresent()) {
            throw new DuplicateEntityException("Role", name, existing.get().getStatus());
        }

        Role role = new Role();

        role.setName(name);
        role.setDescription(description);
        role.setStatus(Status.ACTIVE);

        return roleRepository.save(role);
    }

    public Role updateRole(
            UUID roleId,
            String name,
            String description) {
        Role role = getActiveRole(roleId);

        role.setName(name);
        role.setDescription(description);

        return roleRepository.save(role);
    }

    @Transactional(readOnly = true)
    public Role getRole(UUID roleId) {
        return getActiveRole(roleId);
    }

    @Transactional(readOnly = true)
    public List<Role> getAllRoles() {
        return roleRepository.findByStatus(Status.ACTIVE);
    }

    public void disableRole(UUID roleId) {
        Role role = getActiveRole(roleId);

        Instant now = Instant.now();

        role.setStatus(Status.DISABLED);
        role.setDisabledAt(now);
        role.setDeletedAt(null); // Clear deletedAt.
        roleRepository.save(role);

        subjectRoleRepository.cascadedMarkSubjectRolesAsDisabledByRole(roleId, now);
        rolePermissionRepository.cascadedMarkRolePermissionsAsDisabledByRole(roleId, now);
        roleGroupRepository.cascadedMarkRoleGroupsAsDisabledByRole(roleId, now);
        roleHierarchyRepository.cascadedMarkRoleHierarchiesAsDisabledByChild(roleId, now);
        roleHierarchyRepository.cascadedMarkRoleHierarchiesAsDisabledByParent(roleId, now);
    }

    public void enableRole(UUID roleId) {
        // Enabling a role deos not automatically re-enable associated permissions or group associations. Those must be managed separately.
        Role role = roleRepository.findById(roleId)
                .orElseThrow(() -> new EntityNotFoundException("Role not found"));     
        
        if (role.getStatus() == Status.ACTIVE) {
            throw new InvalidEntityStateException("Role is already active."); 
        }
        if (role.getStatus() == Status.DELETED) {
            throw new InvalidEntityStateException("Deleted role cannot be re-enabled.");
        }   
        
        role.setStatus(Status.ACTIVE);
        role.setDisabledAt(null);
        role.setDeletedAt(null);    

        roleRepository.save(role);
    }

    // Manual deletion of roles is not allowed. Disable the role and let the scheduled cleanup handle deletion.
}