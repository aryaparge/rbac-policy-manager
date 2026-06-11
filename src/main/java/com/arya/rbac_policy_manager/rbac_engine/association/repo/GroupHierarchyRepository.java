package com.arya.rbac_policy_manager.rbac_engine.association.repo;

import com.arya.rbac_policy_manager.rbac_engine.association.entity.GroupHierarchy;
import com.arya.rbac_policy_manager.rbac_engine.common.Enums.Status;
import com.arya.rbac_policy_manager.rbac_engine.group.entity.Group;

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
public interface GroupHierarchyRepository extends JpaRepository<GroupHierarchy, UUID> {

        boolean existsByParentGroupAndChildGroup(Group parentGroup, Group childGroup);

        List<GroupHierarchy> findByParentGroupAndStatus(Group parentGroup, Status status);

        List<GroupHierarchy> findByChildGroupAndStatus(Group childGroup, Status status);

        Optional<GroupHierarchy> findByParentGroupAndChildGroup(Group parentGroup, Group childGroup);

        Optional<GroupHierarchy> findByParentGroupAndChildGroupAndStatus(Group parentGroup, Group childGroup,
                        Status status);

        @Modifying(flushAutomatically = true, clearAutomatically = true)
        @Query("""
                        update GroupHierarchy gh
                        set gh.status = com.arya.rbac_policy_manager.rbac_engine.common.Enums.Status.DISABLED,
                                gh.disabledAt = :disabledAt,
                                gh.deletedAt = null
                        where gh.parentGroup.id = :parentId 
                        """)
                        //identity is stable, status is derived and timing-sensitive.
        int cascadedMarkGroupHierarchiesAsDisabledByParent(@Param("parentId") UUID parentId, @Param("disabledAt") Instant disabledAt);

        @Modifying(flushAutomatically = true, clearAutomatically = true)
        @Query("""
                        update GroupHierarchy gh
                        set gh.status = com.arya.rbac_policy_manager.rbac_engine.common.Enums.Status.DISABLED,
                                gh.disabledAt = :disabledAt,
                                gh.deletedAt = null
                        where gh.childGroup.id = :childId
                        """)
        int cascadedMarkGroupHierarchiesAsDisabledByChild(@Param("childId") UUID childId, @Param("disabledAt") Instant disabledAt);

        @Modifying(flushAutomatically = true, clearAutomatically = true)
        @Query("""
                            update GroupHierarchy gh
                            set gh.status = com.arya.rbac_policy_manager.rbac_engine.common.Enums.Status.DELETED,
                                    gh.deletedAt = :deletedAt
                            where gh.status = com.arya.rbac_policy_manager.rbac_engine.common.Enums.Status.DISABLED
                            and gh.disabledAt is not null
                            and gh.disabledAt <= :cutoff
                        """)
        int markDisabledGroupHierarchiesAsDeleted(@Param("cutoff") Instant cutoff, @Param("deletedAt") Instant deletedAt);

        @Modifying(flushAutomatically = true, clearAutomatically = true)
        @Query("""
                            delete from GroupHierarchy gh
                            where gh.status = com.arya.rbac_policy_manager.rbac_engine.common.Enums.Status.DELETED
                            and gh.deletedAt is not null
                            and gh.deletedAt <= :cutoff
                        """)
        int hardDeleteExpiredGroupHierarchies(@Param("cutoff") Instant cutoff);
}
