package com.arya.rbac_policy_manager.rbac_engine.association.repo;

import com.arya.rbac_policy_manager.rbac_engine.association.entity.RolePermission;
import com.arya.rbac_policy_manager.rbac_engine.common.Enums.Status;
import com.arya.rbac_policy_manager.rbac_engine.permission.entity.Permission;
import com.arya.rbac_policy_manager.rbac_engine.role.entity.Role;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.time.Instant;
import java.util.List;
import java.util.Optional;
import java.util.Set;
import java.util.UUID;

@Repository
public interface RolePermissionRepository extends JpaRepository<RolePermission, UUID> {

    List<RolePermission> findByRole(Role role);

    List<RolePermission> findAllByPermission(Permission permission);

    Set<RolePermission> findByRoleAndStatus(Role role, Status status);

    Optional<RolePermission> findByRoleAndPermission(Role role, Permission permission);

    boolean existsByRoleAndPermission(Role role, Permission permission);

    @Modifying(flushAutomatically = true, clearAutomatically = true)
    @Query("""
                update RolePermission rp
                set rp.status = com.arya.rbac_policy_manager.rbac_engine.common.Enums.Status.DISABLED,
                    rp.disabledAt = :disabledAt,
                    rp.deletedAt = null
                where rp.role.id = :roleId
            """)
    int cascadedMarkRolePermissionsAsDisabledByRole(@Param("roleId") UUID roleId, @Param("disabledAt") Instant disabledAt);

    @Modifying(flushAutomatically = true, clearAutomatically = true)
    @Query("""
                update RolePermission rp
                set rp.status = com.arya.rbac_policy_manager.rbac_engine.common.Enums.Status.DISABLED,
                    rp.disabledAt = :disabledAt,
                    rp.deletedAt = null
                where rp.permission.id = :permissionId
            """)
    int cascadedMarkRolePermissionsAsDisabledByPermission(@Param("permissionId") UUID permissionId, @Param("disabledAt") Instant disabledAt);

    @Modifying(flushAutomatically = true, clearAutomatically = true)
    @Query("""
                update RolePermission rp
                set rp.status = com.arya.rbac_policy_manager.rbac_engine.common.Enums.Status.DELETED,
                    rp.deletedAt = :deletedAt
                where rp.status = com.arya.rbac_policy_manager.rbac_engine.common.Enums.Status.DISABLED
                and rp.disabledAt is not null
                and rp.disabledAt <= :cutoff
            """)
    int markDisabledRolePermissionsAsDeleted(@Param("cutoff") Instant cutoff, @Param("deletedAt") Instant deletedAt);

    @Modifying(flushAutomatically = true, clearAutomatically = true)
    @Query("""
                delete from RolePermission rp
                where rp.status = com.arya.rbac_policy_manager.rbac_engine.common.Enums.Status.DELETED
                and rp.deletedAt is not null
                and rp.deletedAt <= :cutoff
            """)
    int hardDeleteExpiredRolePermissions(@Param("cutoff") Instant cutoff);
}