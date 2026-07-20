package com.arya.rbac_policy_manager.rbac_engine.association.repo;

import com.arya.rbac_policy_manager.rbac_engine.association.entity.RoleGroup;
import com.arya.rbac_policy_manager.rbac_engine.common.Enums.Status;
import com.arya.rbac_policy_manager.rbac_engine.group.entity.Group;
import com.arya.rbac_policy_manager.rbac_engine.role.entity.Role;

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
public interface RoleGroupRepository extends JpaRepository<RoleGroup, UUID> {

    List<RoleGroup> findByRole(Role role);

    List<RoleGroup> findByGroup(Group group);

    List<RoleGroup> findByRoleAndStatus(Role role, Status status);

    Optional<RoleGroup> findByRoleAndGroup(Role role, Group group);

    boolean existsByRoleAndGroup(Role role, Group group);

    @Modifying(flushAutomatically = true, clearAutomatically = true)
    @Query("""
                update RoleGroup rg
                set rg.status = com.arya.rbac_policy_manager.rbac_engine.common.Enums.Status.DISABLED,
                    rg.disabledAt = :disabledAt,
                    rg.deletedAt = null
                where rg.role.id = :roleId
            """)
    int cascadedMarkRoleGroupsAsDisabledByRole(@Param("roleId") UUID roleId, @Param("disabledAt") Instant disabledAt);

    @Modifying(flushAutomatically = true, clearAutomatically = true)
    @Query("""
                update RoleGroup rg
                set rg.status = com.arya.rbac_policy_manager.rbac_engine.common.Enums.Status.DISABLED,
                    rg.disabledAt = :disabledAt,
                    rg.deletedAt = null
                where rg.group.id = :groupId
            """)
    int cascadedMarkRoleGroupsAsDisabledByGroup(@Param("groupId") UUID groupId, @Param("disabledAt") Instant disabledAt);

    @Modifying(flushAutomatically = true, clearAutomatically = true)
    @Query("""
                update RoleGroup rg
                set rg.status = com.arya.rbac_policy_manager.rbac_engine.common.Enums.Status.DELETED,
                    rg.deletedAt = :deletedAt
                where rg.status = com.arya.rbac_policy_manager.rbac_engine.common.Enums.Status.DISABLED
                and rg.disabledAt is not null
                and rg.disabledAt <= :cutoff
            """)
    int markDisabledRoleGroupsAsDeleted(@Param("cutoff") Instant cutoff, @Param("deletedAt") Instant deletedAt);

    @Modifying(flushAutomatically = true, clearAutomatically = true)
    @Query("""
                delete from RoleGroup rg
                where rg.status = com.arya.rbac_policy_manager.rbac_engine.common.Enums.Status.DELETED
                and rg.deletedAt is not null
                and rg.deletedAt <= :cutoff
            """)
    int hardDeleteExpiredRoleGroups(@Param("cutoff") Instant cutoff);
}
