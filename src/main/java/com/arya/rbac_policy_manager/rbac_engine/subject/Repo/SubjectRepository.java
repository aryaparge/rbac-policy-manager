package com.arya.rbac_policy_manager.rbac_engine.subject.repo;

import java.time.Instant;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;

import com.arya.rbac_policy_manager.rbac_engine.common.Enums.Status;
import com.arya.rbac_policy_manager.rbac_engine.subject.entity.Subject;

public interface SubjectRepository extends JpaRepository<Subject, UUID> {
    Optional<Subject> findByName(String name);

    boolean existsByName(String name);

    Optional<Subject> findByNameAndStatus(String name, Status status);

    Optional<Subject> findByIdAndStatus(UUID id, Status status);

    List<Subject> findByStatus(Status status);

    @Modifying
    @Query("""
                update Subject s
                set s.status = com.arya.rbac_policy_manager.rbac_engine.common.Enums.Status.DELETED,
                    s.deletedAt = :deletedAt
                where s.status = com.arya.rbac_policy_manager.rbac_engine.common.Enums.Status.DISABLED
                and s.disabledAt is not null
                and s.disabledAt <= :cutoff
            """)
    int markDisabledSubjectsAsDeleted(Instant cutoff, Instant deletedAt);

    @Modifying
    @Query("""
                delete from Subject s
                where s.status = com.arya.rbac_policy_manager.rbac_engine.common.Enums.Status.DELETED
                and s.deletedAt is not null
                and s.deletedAt <= :cutoff
            """)
    int hardDeleteExpiredSubjects(Instant cutoff);
}
