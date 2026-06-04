package com.arya.rbac_policy_manager.rbac_engine.subject.repo;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

import org.springframework.data.jpa.repository.JpaRepository;

import com.arya.rbac_policy_manager.rbac_engine.common.Enum.Status;
import com.arya.rbac_policy_manager.rbac_engine.subject.entity.Subject;

public interface SubjectRepository extends JpaRepository<Subject, UUID> {
    Optional<Subject> findBySubjectId(String subjectId);

    boolean existsBySubjectId(String subjectId);

    Optional<Subject> findBySubjectIdAndStatus(String subjectId, Status status);

    List<Subject> findByStatus(Status status);

    Subject findBySubjectIdandStatus(String subjectId);
}
