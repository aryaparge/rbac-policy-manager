package com.arya.rbac_policy_manager.rbac_engine.resource.repo;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

import org.springframework.data.jpa.repository.JpaRepository;

import com.arya.rbac_policy_manager.rbac_engine.common.Enums.Status;
import com.arya.rbac_policy_manager.rbac_engine.resource.entity.Resource;

public interface ResourceRepository extends JpaRepository<Resource, UUID> {
    Optional<Resource> findByName(String name);

    boolean existsByName(String name);

    Optional<Resource> findByIdAndStatus( UUID id, Status status );

    List<Resource> findByStatus(Status status);
}

