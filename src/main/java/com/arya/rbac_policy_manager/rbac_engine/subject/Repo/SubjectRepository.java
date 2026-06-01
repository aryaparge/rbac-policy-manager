package com.arya.rbac_policy_manager.rbac_engine.subject.Repo;

import java.util.Optional;
import java.util.UUID;

import org.springframework.data.jpa.repository.JpaRepository;
import com.arya.rbac_policy_manager.rbac_engine.subject.Entity.Subject;

public interface SubjectRepository extends JpaRepository<Subject, UUID> {
    Optional<Subject> findBySubjectID(String subjectID);

    boolean existsBySubjectID(String subjectID);
}
