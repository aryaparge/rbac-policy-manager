package com.arya.rbac_policy_manager.rbac_engine.association.repo;

import com.arya.rbac_policy_manager.rbac_engine.association.entity.RoleHierarchy;
import com.arya.rbac_policy_manager.rbac_engine.common.Enums.Status;
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
public interface RoleHierarchyRepository extends JpaRepository<RoleHierarchy, UUID> {

        boolean existsByParentRoleAndChildRole(Role parentRole, Role childRole);

        boolean existsByParentRoleAndChildRoleAndStatus(Role parent, Role child, Status status);

        List<RoleHierarchy> findByParentRoleAndStatus(Role parentRole, Status status);

        List<RoleHierarchy> findByChildRoleAndStatus(Role childRole, Status status);

        Optional<RoleHierarchy> findByParentRoleAndChildRole(Role parentRole, Role childRole);

        @Modifying
        @Query("""
                        update RoleHierarchy rh
                        set rh.status = com.arya.rbac_policy_manager.common.Enums.Status.DISABLED,
                                rh.disabledAt = :disabledAt,
                                rh.deletedAt = null -- Clear deletedAt when marking as disabled
                        where rh.status <> com.arya.rbac_policy_manager.common.Enums.Status.DISABLED
                        and (
                                rh.parentRole.status = com.arya.rbac_policy_manager.common.Enums.Status.DISABLED
                                or rh.childRole.status = com.arya.rbac_policy_manager.common.Enums.Status.DISABLED
                        )
                        """)
        int cascadedMarkRoleHierarchiesAsDisabled(Instant disabledAt);

        @Modifying
        @Query("""
                            update RoleHierarchy rh
                            set rh.status = com.arya.rbac_policy_manager.common.Enums.Status.DELETED,
                                rh.deletedAt = :deletedAt
                            where rh.status = com.arya.rbac_policy_manager.common.Enums.Status.DISABLED
                            and rh.disabledAt is not null
                            and rh.disabledAt <= :cutoff
                        """)
        int markDisabledRoleHierarchiesAsDeleted(Instant cutoff, Instant deletedAt);

        @Modifying
        @Query("""
                            delete from RoleHierarchy rh
                            where rh.status = com.arya.rbac_policy_manager.common.Enums.Status.DELETED
                            and rh.deletedAt is not null
                            and rh.deletedAt <= :cutoff
                        """)
        int hardDeleteExpiredRoleHierarchies(Instant cutoff);
}
