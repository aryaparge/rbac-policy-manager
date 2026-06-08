package com.arya.rbac_policy_manager.rbac_engine.action.service;

import com.arya.rbac_policy_manager.rbac_engine.common.Enum.Status;
import com.arya.rbac_policy_manager.rbac_engine.common.exception.ActiveEntityNotFoundException;
import com.arya.rbac_policy_manager.rbac_engine.common.exception.DuplicateEntityException;
import com.arya.rbac_policy_manager.rbac_engine.common.exception.EntityNotFoundException;
import com.arya.rbac_policy_manager.rbac_engine.common.exception.InvalidEntityStateException;
import com.arya.rbac_policy_manager.rbac_engine.action.entity.Action;
import com.arya.rbac_policy_manager.rbac_engine.action.repo.ActionRepository;

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

    private Action getActiveAction(UUID actionId) {
        return actionRepository.findByIdAndStatus(
                actionId,
                Status.ACTIVE).orElseThrow(() -> new ActiveEntityNotFoundException("Action", actionId));
    }

    public Action createAction(
            String name,
            String description) {
        Optional<Action> existing = actionRepository.findByName(name);

        if (existing.isPresent())
        {
            throw new DuplicateEntityException( "Action", name, existing.get().getStatus());
        }

        Action action = new Action();

        action.setName(name);
        action.setDescription(description);
        action.setStatus(Status.ACTIVE);

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

        action.setStatus(Status.DISABLED);
        action.setDisabledAt(Instant.now());

        actionRepository.save(action);
    }

    public void deleteAction(UUID actionId) {
        Action action = actionRepository.findById(actionId).orElseThrow(() -> new EntityNotFoundException("Action not found"));

        if(action.getStatus() == Status.ACTIVE) {
            throw new InvalidEntityStateException("Active action cannot be deleted. Consider disabling instead");
        }

        else if(action.getStatus() == Status.DELETED) {
            throw new InvalidEntityStateException("Action already deleted.");
        }

        action.setStatus(Status.DELETED);
        action.setDeletedAt(Instant.now());

        actionRepository.save(action);
    }
}