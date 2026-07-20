package com.arya.rbac_policy_manager.rbac_engine.association.repo;

import com.arya.rbac_policy_manager.rbac_engine.association.entity.RoleHierarchy;
import com.arya.rbac_policy_manager.rbac_engine.common.Enums.Status;
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
public interface RoleHierarchyRepository extends JpaRepository<RoleHierarchy, UUID> {

        boolean existsByParentRoleAndChildRole(Role parentRole, Role childRole);

        boolean existsByParentRoleAndChildRoleAndStatus(Role parent, Role child, Status status);

        List<RoleHierarchy> findByParentRoleAndStatus(Role parentRole, Status status);

        List<RoleHierarchy> findByParentRole(Role parentRole);

        List<RoleHierarchy> findByChildRoleAndStatus(Role childRole, Status status);

        Optional<RoleHierarchy> findByParentRoleAndChildRole(Role parentRole, Role childRole);

        Optional<RoleHierarchy> findByParentRoleAndChildRoleAndStatus(Role parentRole, Role childRole, Status status);

        @Modifying(flushAutomatically = true, clearAutomatically = true)
        @Query("""
                        update RoleHierarchy rh
                        set rh.status = com.arya.rbac_policy_manager.rbac_engine.common.Enums.Status.DISABLED,
                                rh.disabledAt = :disabledAt,
                                rh.deletedAt = null
                        where rh.childRole.id = :childId
                        """)
        int cascadedMarkRoleHierarchiesAsDisabledByChild(@Param("childId") UUID childId,@Param("disabledAt") Instant disabledAt);

        @Modifying(flushAutomatically = true, clearAutomatically = true)
        @Query("""
                        update RoleHierarchy rh
                        set rh.status = com.arya.rbac_policy_manager.rbac_engine.common.Enums.Status.DISABLED,
                                rh.disabledAt = :disabledAt,
                                rh.deletedAt = null
                        where rh.parentRole.id = :parentId
                        """)
        int cascadedMarkRoleHierarchiesAsDisabledByParent(@Param("parentId") UUID parentId, @Param("disabledAt") Instant disabledAt);

        @Modifying(flushAutomatically = true, clearAutomatically = true)
        @Query("""
                            update RoleHierarchy rh
                            set rh.status = com.arya.rbac_policy_manager.rbac_engine.common.Enums.Status.DELETED,
                                rh.deletedAt = :deletedAt
                            where rh.status = com.arya.rbac_policy_manager.rbac_engine.common.Enums.Status.DISABLED
                            and rh.disabledAt is not null
                            and rh.disabledAt <= :cutoff
                        """)
        int markDisabledRoleHierarchiesAsDeleted(@Param("cutoff") Instant cutoff,
                        @Param("deletedAt") Instant deletedAt);

        @Modifying(flushAutomatically = true, clearAutomatically = true)
        @Query("""
                            delete from RoleHierarchy rh
                            where rh.status = com.arya.rbac_policy_manager.rbac_engine.common.Enums.Status.DELETED
                            and rh.deletedAt is not null
                            and rh.deletedAt <= :cutoff
                        """)
        int hardDeleteExpiredRoleHierarchies(@Param("cutoff") Instant cutoff);
}
