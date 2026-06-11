package com.arya.rbac_policy_manager.rbac_engine.action.service;

import com.arya.rbac_policy_manager.rbac_engine.common.Enums.Status;
import com.arya.rbac_policy_manager.rbac_engine.common.exception.ActiveEntityNotFoundException;
import com.arya.rbac_policy_manager.rbac_engine.common.exception.DuplicateEntityException;
import com.arya.rbac_policy_manager.rbac_engine.common.exception.EntityNotFoundException;
import com.arya.rbac_policy_manager.rbac_engine.common.exception.InvalidEntityStateException;
import com.arya.rbac_policy_manager.rbac_engine.permission.repo.PermissionRepository;
import com.arya.rbac_policy_manager.rbac_engine.action.entity.Action;
import com.arya.rbac_policy_manager.rbac_engine.action.repo.ActionRepository;
import com.arya.rbac_policy_manager.rbac_engine.association.repo.GroupPermissionRepository;
import com.arya.rbac_policy_manager.rbac_engine.association.repo.RolePermissionRepository;

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
public class ActionService {
    private final ActionRepository actionRepository;

    private final PermissionRepository permissionRepository;
    private final RolePermissionRepository rolePermissionRepository;
    private final GroupPermissionRepository groupPermissionRepository;

    public Action getActiveAction(UUID actionId) {
        return actionRepository.findByIdAndStatus(
                actionId,
                Status.ACTIVE).orElseThrow(() -> new ActiveEntityNotFoundException("Action", actionId));
    }

    public Action createAction(
            String name,
            String description) {
        Optional<Action> existing = actionRepository.findByName(name);

        if (existing.isPresent()) {
            throw new DuplicateEntityException("Action", name, existing.get().getStatus());
        }

        Action action = new Action();

        action.setName(name);
        action.setDescription(description);
        action.setStatus(Status.ACTIVE);
        action.setDeletedAt(null);
        action.setDisabledAt(null);

        return actionRepository.save(action);
    }

    public Action updateAction(
            UUID actionId,
            String name,
            String description) {
        Action action = getActiveAction(actionId);

        action.setName(name);
        action.setDescription(description);

        return actionRepository.save(action);
    }

    @Transactional(readOnly = true)
    public Action getAction(UUID actionId) {
        return getActiveAction(actionId);
    }

    @Transactional(readOnly = true)
    public List<Action> getAllActions() {
        return actionRepository.findByStatus(Status.ACTIVE);
    }

    public void disableAction(UUID actionId) {
        Action action = getActiveAction(actionId);

        Instant now = Instant.now();

        action.setStatus(Status.DISABLED);
        action.setDisabledAt(now);
        action.setDeletedAt(null); // ensure deletedAt is null.
        actionRepository.save(action);

        permissionRepository.cascadedMarkPermissionsAsDisabledByAction(now);
        rolePermissionRepository.cascadedMarkRolePermissionsAsDisabledByPermission(now);
        groupPermissionRepository.cascadedMarkGroupPermissionsAsDisabledByPermission(now);
    }

    public void enableAction(UUID actionId) {
        // Enabling an action does not automatically enable related entities. They must
        // be enabled separately if needed.
        Action action = actionRepository.findById(actionId)
                .orElseThrow(() -> new EntityNotFoundException("Action not found"));

        if (action.getStatus() == Status.ACTIVE) {
            throw new InvalidEntityStateException("Action is already active.");
        }

        action.setStatus(Status.ACTIVE);
        action.setDisabledAt(null);
        action.setDeletedAt(null);

        actionRepository.save(action);
    }
    // Manual deletion of actions is not allowed. Disable the action and let the
    // scheduled cleanup process handle deletion.
}