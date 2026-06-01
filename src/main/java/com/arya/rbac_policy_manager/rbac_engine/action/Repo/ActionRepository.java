package com.arya.rbac_policy_manager.rbac_engine.action.repo;

import java.util.Optional;
import java.util.UUID;

import org.springframework.data.jpa.repository.JpaRepository;

import com.arya.rbac_policy_manager.rbac_engine.action.entity.Action;

public interface ActionRepository extends JpaRepository<Action, UUID> {
    Optional<Action> findByName(String name);

    boolean existsByName(String name);
}

