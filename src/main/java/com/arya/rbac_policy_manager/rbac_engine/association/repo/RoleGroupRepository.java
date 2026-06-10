package com.arya.rbac_policy_manager.rbac_engine.association.repo;

import com.arya.rbac_policy_manager.rbac_engine.association.entity.RoleGroup;
import com.arya.rbac_policy_manager.rbac_engine.common.Enums.Status;
import com.arya.rbac_policy_manager.rbac_engine.group.entity.Group;
import com.arya.rbac_policy_manager.rbac_engine.role.entity.Role;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

import java.time.Instant;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

@Repository
public interface RoleGroupRepository extends JpaRepository<RoleGroup, UUID> {

    List<RoleGroup> findByRole(Role role);

    List<RoleGroup> findByRoleAndStatus(Role role, Status status);

    Optional<RoleGroup> findByRoleAndGroup(Role role, Group group);

    boolean existsByRoleAndGroup(Role role, Group group);

    @Modifying
    @Query("""
                update RoleGroup rg
                set rg.status = com.arya.rbac_policy_manager.common.Enums.Status.DISABLED,
                    rg.disabledAt = :disabledAt,
                    rg.deletedAt = null -- Clear deletedAt when marking as disabled
                where rg.status <> com.arya.rbac_policy_manager.common.Enums.Status.DISABLED
                and (
                        rg.role.status = com.arya.rbac_policy_manager.common.Enums.Status.DISABLED
                     or rg.group.status = com.arya.rbac_policy_manager.common.Enums.Status.DISABLED
                )
            """)
    int cascadedMarkRoleGroupsAsDisabled(Instant disabledAt);

    @Modifying
    @Query("""
                update RoleGroup rg
                set rg.status = com.arya.rbac_policy_manager.common.Enums.Status.DELETED,
                    rg.deletedAt = :deletedAt
                where rg.status = com.arya.rbac_policy_manager.common.Enums.Status.DISABLED
                and rg.disabledAt is not null
                and rg.disabledAt <= :cutoff
            """)
    int markDisabledRoleGroupsAsDeleted(Instant cutoff, Instant deletedAt);

    @Modifying
    @Query("""
                delete from RoleGroup rg
                where rg.status = com.arya.rbac_policy_manager.common.Enums.Status.DELETED
                and rg.deletedAt is not null
                and rg.deletedAt <= :cutoff
            """)
    int hardDeleteExpiredRoleGroups(Instant cutoff);
}