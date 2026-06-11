package com.arya.rbac_policy_manager.rbac_engine.role.repo;

import java.time.Instant;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import com.arya.rbac_policy_manager.rbac_engine.common.Enums.Status;
import com.arya.rbac_policy_manager.rbac_engine.role.entity.Role;

public interface RoleRepository extends JpaRepository<Role, UUID> {
    Optional<Role> findByName(String name);

    boolean existsByName(String name);

    Optional<Role> findByIdAndStatus(UUID id, Status status);

    List<Role> findByStatus(Status status);

    @Modifying
    @Query("""
                update Role r
                set r.status = com.arya.rbac_policy_manager.rbac_engine.common.Enums.Status.DELETED,
                    r.deletedAt = :deletedAt
                where r.status = com.arya.rbac_policy_manager.rbac_engine.common.Enums.Status.DISABLED
                and r.disabledAt is not null
                and r.disabledAt <= :cutoff
            """)

    int markDisabledRolesAsDeleted(@Param("cutoff") Instant cutoff, @Param("deletedAt") Instant deletedAt);

    @Modifying
    @Query("""
                delete from Role r
                where r.status = com.arya.rbac_policy_manager.rbac_engine.common.Enums.Status.DELETED
                and r.deletedAt is not null
                and r.deletedAt <= :cutoff
            """)
    int hardDeleteExpiredRoles(@Param("cutoff") Instant cutoff);
}