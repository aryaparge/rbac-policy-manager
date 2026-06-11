package com.arya.rbac_policy_manager.rbac_engine.group.repo;

import java.time.Instant;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import com.arya.rbac_policy_manager.rbac_engine.common.Enums.Status;
import com.arya.rbac_policy_manager.rbac_engine.group.entity.Group;

public interface GroupRepository extends JpaRepository<Group, UUID> {
    Optional<Group> findByName(String name);

    boolean existsByName(String name);

    Optional<Group> findByIdAndStatus(UUID id, Status status);

    List<Group> findByStatus(Status status);

    @Modifying(flushAutomatically = true, clearAutomatically = true)
    @Query(value = """
                update group
                set status = 'DELETED',
                    deleted_at = :deletedAt
                where status = 'DISABLED'
                and disabled_at is not null
                and disabled_at <= :cutoff
            """, nativeQuery = true)
    int markDisabledGroupsAsDeleted(@Param("cutoff") Instant cutoff, @Param("deletedAt") Instant deletedAt);

    @Modifying(flushAutomatically = true, clearAutomatically = true)
    @Query("""
                delete from Group g
                where g.status = com.arya.rbac_policy_manager.rbac_engine.common.Enums.Status.DELETED
                and g.deletedAt is not null
                and g.deletedAt <= :cutoff
            """)
    int hardDeleteExpiredGroups(@Param("cutoff") Instant cutoff);
}
