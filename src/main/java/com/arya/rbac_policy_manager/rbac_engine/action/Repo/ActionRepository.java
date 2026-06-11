package com.arya.rbac_policy_manager.rbac_engine.action.repo;

import java.time.Instant;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import com.arya.rbac_policy_manager.rbac_engine.action.entity.Action;
import com.arya.rbac_policy_manager.rbac_engine.common.Enums.Status;

public interface ActionRepository extends JpaRepository<Action, UUID> {
    Optional<Action> findByName(String name);

    boolean existsByName(String name);

    Optional<Action> findByIdAndStatus(UUID id, Status status);

    List<Action> findByStatus(Status status);

    @Modifying(flushAutomatically = true, clearAutomatically = true)
    @Query("""
                update Action a
                set a.status = com.arya.rbac_policy_manager.rbac_engine.common.Enums.Status.DELETED,
                    a.deletedAt = :deletedAt
                where a.status = com.arya.rbac_policy_manager.rbac_engine.common.Enums.Status.DISABLED
                and a.disabledAt is not null
                and a.disabledAt <= :cutoff
            """)
    int markDisabledActionsAsDeleted(@Param("cutoff") Instant cutoff, @Param("deletedAt") Instant deletedAt);

    @Modifying(flushAutomatically = true, clearAutomatically = true)
    @Query("""
                delete from Action a
                where a.status = com.arya.rbac_policy_manager.rbac_engine.common.Enums.Status.DELETED
                and a.deletedAt is not null
                and a.deletedAt <= :cutoff
            """)
    int hardDeleteExpiredActions(@Param("cutoff") Instant cutoff);
}
