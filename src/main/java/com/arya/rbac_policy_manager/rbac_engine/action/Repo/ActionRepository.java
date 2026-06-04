package com.arya.rbac_policy_manager.rbac_engine.action.repo;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

import org.springframework.data.jpa.repository.JpaRepository;

import com.arya.rbac_policy_manager.rbac_engine.action.entity.Action;
import com.arya.rbac_policy_manager.rbac_engine.common.Enum.Status;

public interface ActionRepository extends JpaRepository<Action, UUID> {
    Optional<Action> findByName(String name);

    boolean existsByName(String name);

    Optional<Action> findByNameAndStatus(String name, Status status);

    List<Action> findByStatus(Status status);
}

