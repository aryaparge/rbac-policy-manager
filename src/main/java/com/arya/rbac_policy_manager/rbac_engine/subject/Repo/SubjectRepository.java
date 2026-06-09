package com.arya.rbac_policy_manager.rbac_engine.subject.repo;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

import org.springframework.data.jpa.repository.JpaRepository;

import com.arya.rbac_policy_manager.rbac_engine.common.Enums.Status;
import com.arya.rbac_policy_manager.rbac_engine.subject.entity.Subject;

public interface SubjectRepository extends JpaRepository<Subject, UUID> {
    Optional<Subject> findByName(String name);

    boolean existsByName(String name);

    Optional<Subject> findByNameAndStatus(String name, Status status);

    Optional<Subject> findByIdAndStatus(UUID id, Status status);

    List<Subject> findByStatus(Status status);

}
