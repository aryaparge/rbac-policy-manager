package com.arya.rbac_policy_manager.rbac_engine.association.service;

import java.time.Instant;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

import org.springframework.stereotype.Service;

import com.arya.rbac_policy_manager.rbac_engine.association.entity.SubjectRole;
import com.arya.rbac_policy_manager.rbac_engine.association.repo.SubjectRoleRepository;
import com.arya.rbac_policy_manager.rbac_engine.common.Enums.Status;
import com.arya.rbac_policy_manager.rbac_engine.common.exception.ActiveEntityNotFoundException;
import com.arya.rbac_policy_manager.rbac_engine.common.exception.DuplicateEntityException;
import com.arya.rbac_policy_manager.rbac_engine.common.exception.EntityNotFoundException;
import com.arya.rbac_policy_manager.rbac_engine.common.exception.InvalidEntityStateException;
import com.arya.rbac_policy_manager.rbac_engine.role.entity.Role;
import com.arya.rbac_policy_manager.rbac_engine.role.service.RoleService;
import com.arya.rbac_policy_manager.rbac_engine.subject.entity.Subject;
import com.arya.rbac_policy_manager.rbac_engine.subject.service.SubjectService;

import lombok.RequiredArgsConstructor;

@Service
@RequiredArgsConstructor
public class SubjectRoleService {

    private final SubjectRoleRepository subjectRoleRepository;
    private final SubjectService subjectService;
    private final RoleService roleService;

    public SubjectRole getActiveAssignment(UUID assignmentId) {
        SubjectRole assignment = subjectRoleRepository.findById(assignmentId)
                .orElseThrow(() -> new EntityNotFoundException("SubjectRole not found"));

        if (assignment.getStatus() != Status.ACTIVE) {
            throw new ActiveEntityNotFoundException("SubjectRole", assignmentId);
        }

        return assignment;
    }

    public List<Role> getActiveRoles(UUID subjectId) {
        List<SubjectRole> assignments = subjectRoleRepository.findBySubjectId(subjectId);

        List<Role> roles = assignments.stream()
                .map(SubjectRole::getRole)
                .toList();

        return roles;
    }

    public SubjectRole assignSubjectToRole(
            UUID subjectId,
            UUID roleId) {

        Subject subject = subjectService.getSubject(subjectId);
        Role role = roleService.getRole(roleId);

        Optional<SubjectRole> existing = subjectRoleRepository.findBySubjectAndRole(subject, role);

        if (existing.isPresent()) {
            throw new DuplicateEntityException(
                    "SubjectRole",
                    subject.getName() + " -> " + role.getName(),
                    existing.get().getStatus());
        }

        SubjectRole assignment = new SubjectRole();

        assignment.setSubject(subject);
        assignment.setRole(role);
        assignment.setName(subject.getName() + "->" + role.getName());
        assignment.setStatus(Status.ACTIVE);

        return subjectRoleRepository.save(assignment);
    }

    public void disableAssignment(UUID assignmentId) {

        SubjectRole assignment = getActiveAssignment(assignmentId);

        assignment.setStatus(Status.DISABLED);
        assignment.setDisabledAt(Instant.now());
        assignment.setDeletedAt(null); // Clear deletedAt in case it was previously marked deleted.

        subjectRoleRepository.save(assignment);
    }

    public void enableAssignment(UUID assignmentId) {

        SubjectRole assignment = subjectRoleRepository.findById(assignmentId)
                .orElseThrow(() -> new EntityNotFoundException("SubjectRole not found"));

        if (assignment.getStatus() != Status.DISABLED) {
            throw new InvalidEntityStateException(
                    "Only disabled assignments can be enabled.");
        }

        assignment.setStatus(Status.ACTIVE);
        assignment.setDisabledAt(null); // Clear disabledAt.
        assignment.setDeletedAt(null); // Clear deletedAt in case it was previously marked deleted.

        subjectRoleRepository.save(assignment);
    }

    // Manual deletion of a SubjectRole assignment is not allowed. It must be
    // disabled first, then a scheduled job will permanently delete it after a
    // retention period.
}
