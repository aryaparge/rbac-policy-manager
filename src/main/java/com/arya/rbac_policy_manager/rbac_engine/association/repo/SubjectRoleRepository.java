package com.arya.rbac_policy_manager.rbac_engine.association.repo;

import com.arya.rbac_policy_manager.rbac_engine.association.entity.SubjectRole;
import com.arya.rbac_policy_manager.rbac_engine.role.entity.Role;
import com.arya.rbac_policy_manager.rbac_engine.subject.entity.Subject;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.UUID;

@Repository
public interface SubjectRoleRepository extends JpaRepository<SubjectRole, UUID> {

    List<SubjectRole> findBySubject(Subject subject);

    List<SubjectRole> findByRole(Role role);

    boolean existsBySubjectAndRole(
            Subject subject,
            Role role
    );
}
