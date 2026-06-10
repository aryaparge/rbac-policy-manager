package com.arya.rbac_policy_manager.rbac_engine.association.repo;

import com.arya.rbac_policy_manager.rbac_engine.association.entity.SubjectRole;
import com.arya.rbac_policy_manager.rbac_engine.common.Enums.Status;
import com.arya.rbac_policy_manager.rbac_engine.role.entity.Role;
import com.arya.rbac_policy_manager.rbac_engine.subject.entity.Subject;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

import java.time.Instant;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

@Repository
public interface SubjectRoleRepository extends JpaRepository<SubjectRole, UUID> {

    List<SubjectRole> findBySubject(Subject subject);

    List<SubjectRole> findBySubjectAndStatus(Subject subject, Status status);

    Optional<SubjectRole> findBySubjectAndRole(Subject subject, Role role);

    boolean existsBySubjectAndRole(Subject subject, Role role);

    @Modifying
    @Query("""
                update SubjectRole sr
                set sr.status = com.arya.rbac_policy_manager.common.Enums.Status.DISABLED,
                    sr.disabledAt = :disabledAt,
                    sr.deletedAt = null -- Clear deletedAt when marking as disabled
                where sr.status <> com.arya.rbac_policy_manager.common.Enums.Status.DISABLED
                and (
                        sr.subject.status = com.arya.rbac_policy_manager.rbac_engine.common.Enums.Status.DISABLED
                     or sr.role.status = com.arya.rbac_policy_manager.rbac_engine.common.Enums.Status.DISABLED
                )
            """)
    int cascadedMarkSubjectRolesAsDisabled(Instant disabledAt);

    @Modifying
    @Query("""
                update SubjectRole sr
                set sr.status = com.arya.rbac_policy_manager.common.Enums.Status.DELETED,
                    sr.deletedAt = :deletedAt
                where sr.status = com.arya.rbac_policy_manager.common.Enums.Status.DISABLED
                and sr.disabledAt is not null
                and sr.disabledAt <= :cutoff
            """)
    int markDisabledSubjectRolesAsDeleted(Instant cutoff, Instant deletedAt);  

    @Modifying
    @Query("""
                delete from SubjectRole sr
                where sr.status = com.arya.rbac_policy_manager.common.Enums.Status.DELETED
                and sr.deletedAt is not null
                and sr.deletedAt <= :cutoff
            """)
    int hardDeleteExpiredSubjectRoles(Instant cutoff);
}
