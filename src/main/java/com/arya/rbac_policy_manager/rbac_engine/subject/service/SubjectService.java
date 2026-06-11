package com.arya.rbac_policy_manager.rbac_engine.subject.service;

import com.arya.rbac_policy_manager.rbac_engine.association.repo.SubjectRoleRepository;
import com.arya.rbac_policy_manager.rbac_engine.common.Enums.Status;
import com.arya.rbac_policy_manager.rbac_engine.common.Enums.SubjectType;
import com.arya.rbac_policy_manager.rbac_engine.common.exception.ActiveEntityNotFoundException;
import com.arya.rbac_policy_manager.rbac_engine.common.exception.DuplicateEntityException;
import com.arya.rbac_policy_manager.rbac_engine.common.exception.EntityNotFoundException;
import com.arya.rbac_policy_manager.rbac_engine.common.exception.InvalidEntityStateException;
import com.arya.rbac_policy_manager.rbac_engine.subject.entity.Subject;
import com.arya.rbac_policy_manager.rbac_engine.subject.repo.SubjectRepository;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.Instant;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

@Service
@Transactional
@RequiredArgsConstructor
public class SubjectService {
    private final SubjectRepository subjectRepository;

    private final SubjectRoleRepository subjectRoleRepository;

    public Subject getActiveSubject(UUID subjectId) {
        return subjectRepository.findByIdAndStatus(subjectId, Status.ACTIVE)
                .orElseThrow(() -> new ActiveEntityNotFoundException("Subject", subjectId));
    }

    public Subject createSubject(
            String name,
            String displayName,
            SubjectType subjectType,
            String description) {
        Optional<Subject> existing = subjectRepository.findByName(name);

        if (existing.isPresent()) {
            throw new DuplicateEntityException("Subject", name, existing.get().getStatus());
        }

        Subject subject = new Subject();

        subject.setName(name);
        subject.setDisplayName(displayName);
        subject.setDescription(description);
        subject.setSubjectType(subjectType);
        subject.setStatus(Status.ACTIVE);

        return subjectRepository.save(subject);
    }

    public Subject updateSubject(
            UUID subjectId,
            // name is immutable
            String displayName,
            String description) {

        Subject subject = getActiveSubject(subjectId);

        subject.setDisplayName(displayName);
        subject.setDescription(description);

        return subjectRepository.save(subject);
    }

    @Transactional(readOnly = true)
    public Subject getSubject(UUID subjectId) {
        return getActiveSubject(subjectId);
    }

    @Transactional(readOnly = true)
    public List<Subject> getAllSubjects() {
        return subjectRepository.findByStatus(Status.ACTIVE);
    }

    public void disableSubject(UUID subjectId) {
        Subject subject = getActiveSubject(subjectId);

        Instant now = Instant.now();

        subject.setStatus(Status.DISABLED);
        subject.setDisabledAt(now);
        subject.setDeletedAt(null);
        subjectRepository.save(subject);

        subjectRoleRepository.cascadedMarkSubjectRolesAsDisabledBySubject(now);
    }

    public void enableSubject(UUID subjectId) {
        // Enabling a subject does not automatically enable its associated subject-role
        // relationships. They need to be enabled separately if needed.
        Subject subject = subjectRepository.findById(subjectId)
                .orElseThrow(() -> new EntityNotFoundException("Subject not found"));

        if (subject.getStatus() == Status.ACTIVE) {
            throw new InvalidEntityStateException("Subject is already active.");
        }

        else if (subject.getStatus() == Status.DELETED) {
            throw new InvalidEntityStateException(
                    "Deleted subject cannot be enabled. New subject can be created after retention period.");
        }

        subject.setStatus(Status.ACTIVE);
        subject.setDisabledAt(null);
        subject.setDeletedAt(null);

        subjectRepository.save(subject);
    }
    // Manual deletion of subjects is not allowed. It must be disabled first, then a
    // scheduled job will permanently delete it after a retention period.
}