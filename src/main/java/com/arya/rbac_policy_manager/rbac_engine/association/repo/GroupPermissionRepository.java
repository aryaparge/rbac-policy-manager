package com.arya.rbac_policy_manager.rbac_engine.association.repo;

import com.arya.rbac_policy_manager.rbac_engine.association.entity.GroupPermission;
import com.arya.rbac_policy_manager.rbac_engine.common.Enums.Status;
import com.arya.rbac_policy_manager.rbac_engine.group.entity.Group;
import com.arya.rbac_policy_manager.rbac_engine.permission.entity.Permission;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.time.Instant;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

@Repository
public interface GroupPermissionRepository extends JpaRepository<GroupPermission, UUID> {

    List<GroupPermission> findByGroup(Group group);

    List<GroupPermission> findByGroupAndStatus(Group group, Status status);

    List<GroupPermission> findAllByPermission(Permission permission);

    Optional<GroupPermission> findByGroupAndPermission(Group group, Permission permission);

    List<GroupPermission> findByStatus(Status status);

    boolean existsByGroupAndPermission(Group group, Permission permission);

    @Modifying(flushAutomatically = true, clearAutomatically = true)
    @Query("""
                update GroupPermission gp
                set gp.status = com.arya.rbac_policy_manager.rbac_engine.common.Enums.Status.DISABLED,
                    gp.disabledAt = :disabledAt,
                    gp.deletedAt = null
                where gp.group.id = :groupId
            """)
    int cascadedMarkGroupPermissionsAsDisabledByGroup(@Param("groupId") UUID groupId, @Param("disabledAt") Instant disabledAt);

    @Modifying(flushAutomatically = true, clearAutomatically = true)
    @Query("""
                update GroupPermission gp
                set gp.status = com.arya.rbac_policy_manager.rbac_engine.common.Enums.Status.DISABLED,
                    gp.disabledAt = :disabledAt,
                    gp.deletedAt = null
                where gp.permission.id = :permissionId
            """)
    int cascadedMarkGroupPermissionsAsDisabledByPermission(@Param("permissionId") UUID permissionId, @Param("disabledAt") Instant disabledAt);

    @Modifying(flushAutomatically = true, clearAutomatically = true)
    @Query("""
                update GroupPermission gp
                set gp.status = com.arya.rbac_policy_manager.rbac_engine.common.Enums.Status.DELETED,
                    gp.deletedAt = :deletedAt
                where gp.status = com.arya.rbac_policy_manager.rbac_engine.common.Enums.Status.DISABLED
                and gp.disabledAt is not null
                and gp.disabledAt <= :cutoff
            """)
    int markDisabledGroupPermissionsAsDeleted(@Param("cutoff") Instant cutoff, @Param("deletedAt") Instant deletedAt);

    @Modifying(flushAutomatically = true, clearAutomatically = true)
    @Query("""
                delete from GroupPermission gp
                where gp.status = com.arya.rbac_policy_manager.rbac_engine.common.Enums.Status.DELETED
                and gp.deletedAt is not null
                and gp.deletedAt <= :cutoff
            """)
    int hardDeleteExpiredGroupPermissions(@Param("cutoff") Instant cutoff);
}