package com.arya.rbac_policy_manager.rbac_engine.resource.Repo;

import java.util.Optional;
import java.util.UUID;

import org.springframework.data.jpa.repository.JpaRepository;
import com.arya.rbac_policy_manager.rbac_engine.resource.Entity.Resource;

public interface ResourceRepository extends JpaRepository<Resource, UUID> {
    Optional<Resource> findByName(String name);

    boolean existsByName(String name);
}

