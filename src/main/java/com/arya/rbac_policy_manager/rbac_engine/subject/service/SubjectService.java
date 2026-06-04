package com.arya.rbac_policy_manager.rbac_engine.subject.service;

import com.arya.rbac_policy_manager.rbac_engine.common.Enum.Status;
import com.arya.rbac_policy_manager.rbac_engine.subject.entity.Subject;
import com.arya.rbac_policy_manager.rbac_engine.subject.repo.SubjectRepository;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import java.util.List;

@Service
@Transactional
@RequiredArgsConstructor
public class SubjectService {
    private final SubjectRepository subjectRepository;

    private Subject getActiveSubject(String subjectId) {
        return subjectRepository.findBySubjectIdAndStatus(subjectId, Status.ACTIVE)
                .orElseThrow(() -> new RuntimeException("Subject not found"));
    }

    public Subject createSubject(String subjectId, String displayName, String description) {
        if (subjectRepository.existsBySubjectId(subjectId)) {
            throw new RuntimeException("Subject already exists");
        }

        Subject subject = new Subject();

        subject.setSubjectId(subjectId);
        subject.setDisplayName(displayName);
        subject.setDescription(description);
        subject.setStatus(Status.ACTIVE);

        return subjectRepository.save(subject);
    }

    public Subject updateSubject(
            String subjectId,
            String displayName,
            String description) {

        Subject subject = getActiveSubject(subjectId);

        subject.setDisplayName(displayName);
        subject.setDescription(description);

        return subjectRepository.save(subject);
    }

    @Transactional(readOnly = true)
    public Subject getSubject(String subjectId) {
        return getActiveSubject(subjectId);
    }

    @Transactional(readOnly = true)
    public List<Subject> getAllSubjects() {
        return subjectRepository.findByStatus(Status.ACTIVE);
    }

    public void disableSubject(String subjectId) {
        Subject subject = getActiveSubject(subjectId);

        subject.setStatus(Status.DISABLED);

        subjectRepository.save(subject);
    }

    public void deleteSubject(String subjectId) {
        Subject subject = getActiveSubject(subjectId);

        subject.setStatus(Status.DELETED);

        subjectRepository.save(subject);
    }
}