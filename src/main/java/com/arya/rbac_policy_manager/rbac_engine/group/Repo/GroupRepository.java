package com.arya.rbac_policy_manager.rbac_engine.group.Repo;
import java.util.Optional;
import java.util.UUID;

import org.springframework.data.jpa.repository.JpaRepository;
import com.arya.rbac_policy_manager.rbac_engine.group.Entity.Group;

public interface GroupRepository extends JpaRepository<Group, UUID> {
    Optional<Group> findByName(String name);

    boolean existsByName(String name);
}
