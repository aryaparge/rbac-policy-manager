package com.arya.rbac_policy_manager.rbac_engine.permission.repo;

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
import com.arya.rbac_policy_manager.rbac_engine.permission.entity.Permission;
import com.arya.rbac_policy_manager.rbac_engine.resource.entity.Resource;

public interface PermissionRepository extends JpaRepository<Permission, UUID> {
    Optional<Permission> findByResourceAndAction(Resource resource, Action action);

    boolean existsByResourceAndAction(Resource resource, Action action);

    List<Permission> findByActionAndStatus(Action action, Status status);

    List<Permission> findByResourceAndStatus(Resource resource, Status status);

    Optional<Permission> findByResourceAndActionAndStatus(Resource resource, Action action, Status status);

    Optional<Permission> findByIdAndStatus(UUID id, Status status);

    List<Permission> findByStatus(Status status);

    @Modifying(flushAutomatically = true, clearAutomatically = true)
    // because bulk JPQL updates bypass the persistence context
    @Query("""
                update Permission p
                set p.status = com.arya.rbac_policy_manager.rbac_engine.common.Enums.Status.DISABLED,
                    p.disabledAt = :disabledAt,
                    p.deletedAt = null
                where p.action.id = :actionId
            """)
    int cascadedMarkPermissionsAsDisabledByAction(@Param("actionId") UUID actionId,
            @Param("disabledAt") Instant disabledAt);

    @Modifying(flushAutomatically = true, clearAutomatically = true)
    @Query("""
                update Permission p
                set p.status = com.arya.rbac_policy_manager.rbac_engine.common.Enums.Status.DISABLED,
                    p.disabledAt = :disabledAt,
                    p.deletedAt = null
                where p.resource.id = :resourceId
            """)
    int cascadedMarkPermissionsAsDisabledByResource(@Param("resourceId") UUID resourceId,
            @Param("disabledAt") Instant disabledAt);

    @Modifying(flushAutomatically = true, clearAutomatically = true)
    // because bulk JPQL updates bypass the persistence context
    @Query("""
                update Permission p
                set p.status = com.arya.rbac_policy_manager.rbac_engine.common.Enums.Status.DELETED,
                    p.deletedAt = :deletedAt
                where p.status = com.arya.rbac_policy_manager.rbac_engine.common.Enums.Status.DISABLED
                and p.disabledAt is not null
                and p.disabledAt <= :cutoff
            """)
    int markDisabledPermissionsAsDeleted(@Param("cutoff") Instant cutoff, @Param("deletedAt") Instant deletedAt);

    @Modifying(flushAutomatically = true, clearAutomatically = true)
    @Query("""
                delete from Permission p
                where p.status = com.arya.rbac_policy_manager.rbac_engine.common.Enums.Status.DELETED
                and p.deletedAt is not null
                and p.deletedAt <= :cutoff
            """)
    int hardDeleteExpiredPermissions(@Param("cutoff") Instant cutoff);
}
