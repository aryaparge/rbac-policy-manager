package com.arya.rbac_policy_manager.api.controller;

import com.arya.rbac_policy_manager.rbac_engine.common.Enums.Status;
import com.arya.rbac_policy_manager.rbac_engine.common.Enums.SubjectType;
import com.arya.rbac_policy_manager.rbac_engine.subject.entity.Subject;
import com.arya.rbac_policy_manager.rbac_engine.subject.service.SubjectService;
import jakarta.validation.Valid;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.net.URI;
import java.time.Instant;
import java.util.List;
import java.util.UUID;
import java.util.stream.Collectors;

@RestController
@RequestMapping("/api/subjects")
@RequiredArgsConstructor
public class SubjectController {

    private final SubjectService subjectService;

    @PostMapping
    public ResponseEntity<SubjectResponse> createSubject(@Valid @RequestBody CreateSubjectRequest request) {
        Subject subject = subjectService.createSubject(
                request.name(),
                request.displayName(),
                request.subjectType(),
                request.description());
        return ResponseEntity.created(URI.create("/api/subjects/" + subject.getId())).body(toResponse(subject));
    }

    @GetMapping
    public ResponseEntity<List<SubjectResponse>> getSubjects() {
        return ResponseEntity.ok(subjectService.getAllSubjects().stream()
                .map(SubjectController::toResponse)
                .collect(Collectors.toList()));
    }

    @GetMapping("/{id}")
    public ResponseEntity<SubjectResponse> getSubject(@PathVariable("id") UUID id) {
        return ResponseEntity.ok(toResponse(subjectService.getSubject(id)));
    }

    @PutMapping("/{id}")
    public ResponseEntity<SubjectResponse> updateSubject(
            @PathVariable("id") UUID id,
            @Valid @RequestBody UpdateSubjectRequest request) {
        return ResponseEntity.ok(toResponse(subjectService.updateSubject(
                id,
                request.displayName(),
                request.description())));
    }

    @PatchMapping("/{id}/disable")
    public ResponseEntity<Void> disableSubject(@PathVariable("id") UUID id) {
        subjectService.disableSubject(id);
        return ResponseEntity.noContent().build();
    }

    @PatchMapping("/{id}/enable")
    public ResponseEntity<Void> enableSubject(@PathVariable("id") UUID id) {
        subjectService.enableSubject(id);
        return ResponseEntity.noContent().build();
    }

    private static SubjectResponse toResponse(Subject subject) {
        return new SubjectResponse(
                subject.getId(),
                subject.getName(),
                subject.getDisplayName(),
                subject.getSubjectType(),
                subject.getDescription(),
                subject.getStatus(),
                subject.getCreatedAt(),
                subject.getCreatedBy(),
                subject.getUpdatedAt(),
                subject.getUpdatedBy(),
                subject.getDisabledAt(),
                subject.getDeletedAt());
    }

    public static record SubjectResponse(
            UUID id,
            String name,
            String displayName,
            SubjectType subjectType,
            String description,
            Status status,
            Instant createdAt,
            String createdBy,
            Instant updatedAt,
            String updatedBy,
            Instant disabledAt,
            Instant deletedAt) {
    }

    public static record CreateSubjectRequest(
            @NotBlank String name,
            @NotBlank String displayName,
            @NotNull SubjectType subjectType,
            String description) {
    }

    public static record UpdateSubjectRequest(
            @NotBlank String displayName,
            String description) {
    }
}
