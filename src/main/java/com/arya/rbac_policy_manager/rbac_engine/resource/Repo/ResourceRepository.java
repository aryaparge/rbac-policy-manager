package com.arya.rbac_policy_manager.rbac_engine.resource.repo;

import java.time.Instant;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import com.arya.rbac_policy_manager.rbac_engine.common.Enums.Status;
import com.arya.rbac_policy_manager.rbac_engine.resource.entity.Resource;

public interface ResourceRepository extends JpaRepository<Resource, UUID> {
    Optional<Resource> findByName(String name);

    boolean existsByName(String name);

    Optional<Resource> findByIdAndStatus(UUID id, Status status);

    List<Resource> findByStatus(Status status);

    @Modifying
    @Query("""
                update Resource r
                set r.status = com.arya.rbac_policy_manager.rbac_engine.common.Enums.Status.DELETED,
                    r.deletedAt = :deletedAt
                where r.status = com.arya.rbac_policy_manager.rbac_engine.common.Enums.Status.DISABLED
                and r.disabledAt is not null
                and r.disabledAt <= :cutoff
            """)
    int markDisabledResourcesAsDeleted(@Param("cutoff") Instant cutoff, @Param("deletedAt") Instant deletedAt);

    @Modifying
    @Query("""
                delete from Resource r
                where r.status = com.arya.rbac_policy_manager.rbac_engine.common.Enums.Status.DELETED
                and r.deletedAt is not null
                and r.deletedAt <= :cutoff
            """)
    int hardDeleteExpiredResources(@Param("cutoff") Instant cutoff);
}
