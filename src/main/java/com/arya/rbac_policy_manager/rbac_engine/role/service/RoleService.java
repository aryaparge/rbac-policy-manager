package com.arya.rbac_policy_manager.rbac_engine.role.service;

import com.arya.rbac_policy_manager.rbac_engine.common.Enum.Status;
import com.arya.rbac_policy_manager.rbac_engine.role.entity.Role;
import com.arya.rbac_policy_manager.rbac_engine.role.repo.RoleRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import java.util.List;
import java.util.UUID;

@Service
@Transactional
@RequiredArgsConstructor
public class RoleService {
    private final RoleRepository roleRepository;

    private Role getActiveRole(UUID roleId) {
        return roleRepository.findByIdAndStatus(
                roleId,
                Status.ACTIVE).orElseThrow(() -> new RuntimeException("Role not found"));
    }

    public Role createRole(
            String name,
            String description) {
        if (roleRepository.existsByName(name)) {
            throw new RuntimeException("Role already exists");
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
        return roleRepository.findByStatus( Status.ACTIVE);
    }

    public void disableRole(UUID roleId) {
        Role role = getActiveRole(roleId);

        role.setStatus(Status.DISABLED);

        roleRepository.save(role);
    }

    public void deleteRole(UUID roleId) {
        Role role = getActiveRole(roleId);

        role.setStatus(Status.DELETED);

        roleRepository.save(role);
    }
}