package com.arya.rbac_policy_manager.rbac_engine.subject.service;

import com.arya.rbac_policy_manager.rbac_engine.common.Enums.Status;
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

    private Subject getActiveSubject(UUID subjectId) {
        return subjectRepository.findByIdAndStatus(subjectId, Status.ACTIVE)
                .orElseThrow(() -> new ActiveEntityNotFoundException("Subject", subjectId));
    }

    public Subject createSubject(
        UUID subjectId, 
        String name, 
        String displayName, 
        String description) {
        Optional<Subject> existing = subjectRepository.findByName(name);

         if (existing.isPresent()) {
            throw new DuplicateEntityException("Subject", name, existing.get().getStatus());
        }

        Subject subject = new Subject();

        subject.setName(name);
        subject.setDisplayName(displayName);
        subject.setDescription(description);
        subject.setStatus(Status.ACTIVE);

        return subjectRepository.save(subject);
    }

    public Subject updateSubject(
            UUID subjectId,
            //name is immutable
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

        subject.setStatus(Status.DISABLED);
        subject.setDisabledAt(Instant.now());

        subjectRepository.save(subject);
    }

    public void deleteSubject(UUID subjectId) {
        Subject subject = subjectRepository.findById(subjectId)
                .orElseThrow(() -> new EntityNotFoundException("Subject not found"));

        if (subject.getStatus() == Status.ACTIVE) {
            throw new InvalidEntityStateException("Active subject cannot be deleted. Consider disabling instead");
        }

        else if (subject.getStatus() == Status.DELETED) {
            throw new InvalidEntityStateException("Subject already deleted.");
        }

        subject.setStatus(Status.DELETED);
        subject.setDeletedAt(Instant.now());

        subjectRepository.save(subject);
    }
}