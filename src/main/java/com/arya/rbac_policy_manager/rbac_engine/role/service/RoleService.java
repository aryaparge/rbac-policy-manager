package com.arya.rbac_policy_manager.rbac_engine.role.service;

import com.arya.rbac_policy_manager.rbac_engine.common.Enum.Status;
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

    private Role getActiveRole(UUID roleId) {
        return roleRepository.findByIdAndStatus(roleId, Status.ACTIVE)
                .orElseThrow(() -> new ActiveEntityNotFoundException("Role", roleId));
    }

    public Role createRole(
            String name,
            String description) {
        Optional<Role> existing = roleRepository.findByName(name);

        if (existing.isPresent()) {
            throw new DuplicateEntityException("Role", name, Status.ACTIVE);
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

        role.setStatus(Status.DISABLED);
        role.setDisabledAt(Instant.now());

        roleRepository.save(role);
    }

    public void deleteRole(UUID roleId) {
        Role role = roleRepository.findById(roleId).orElseThrow(() -> new EntityNotFoundException("Role not found"));

        if (role.getStatus() == Status.ACTIVE) {
            throw new InvalidEntityStateException("Active resource cannot be deleted. Consider disabling instead.");
        }

        else if (role.getStatus() == Status.DELETED) {
            throw new InvalidEntityStateException("Resource already deleted.");
        }

        role.setStatus(Status.DELETED);
        role.setDeletedAt(Instant.now());

        roleRepository.save(role);
    }
}