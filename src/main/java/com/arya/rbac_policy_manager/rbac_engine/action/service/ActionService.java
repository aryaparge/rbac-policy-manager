package com.arya.rbac_policy_manager.rbac_engine.action.service;

import com.arya.rbac_policy_manager.rbac_engine.common.Enum.Status;
import com.arya.rbac_policy_manager.rbac_engine.action.entity.Action;
import com.arya.rbac_policy_manager.rbac_engine.action.repo.ActionRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import java.util.List;

@Service
@Transactional
@RequiredArgsConstructor
public class ActionService {
    private final ActionRepository actionRepository;

    private Action getActiveAction(String name) { //find by name works as nems is unique.
        return actionRepository.findByNameAndStatus(
                name,
                Status.ACTIVE).orElseThrow(() -> new RuntimeException("Action not found"));
    }

    public Action createAction(
            String name,
            String description) {
        if (actionRepository.existsByName(name)) {
            throw new RuntimeException("Action already exists");
        }

        Action action = new Action();

        action.setName(name);
        action.setDescription(description);
        action.setStatus(Status.ACTIVE);

        return actionRepository.save(action);
    }

    public Action updateAction(
            String name,
            String description) {
        Action action = getActiveAction(name);

        action.setName(name);
        action.setDescription(description);

        return actionRepository.save(action);
    }

    @Transactional(readOnly = true)
    public Action getAction(String name) {
        return getActiveAction(name);
    }

    @Transactional(readOnly = true)
    public List<Action> getAllActions() {
        return actionRepository.findByStatus( Status.ACTIVE);
    }

    public void disableAction(String name) {
        Action action = getActiveAction(name);

        action.setStatus(Status.DISABLED);

        actionRepository.save(action);
    }

    public void deleteAction(String name) {
        Action action = getActiveAction(name);

        action.setStatus(Status.DELETED);

        actionRepository.save(action);
    }
}