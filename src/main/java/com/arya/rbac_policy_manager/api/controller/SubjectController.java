package com.arya.rbac_policy_manager.api.controller;

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
import java.util.List;
import java.util.UUID;

@RestController
@RequestMapping("/api/subjects")
@RequiredArgsConstructor
public class SubjectController {

    private final SubjectService subjectService;

    @PostMapping
    public ResponseEntity<Subject> createSubject(@Valid @RequestBody CreateSubjectRequest request) {
        Subject subject = subjectService.createSubject(
                request.name(),
                request.displayName(),
                request.subjectType(),
                request.description());
        return ResponseEntity.created(URI.create("/api/subjects/" + subject.getId())).body(subject);
    }

    @GetMapping
    public ResponseEntity<List<Subject>> getSubjects() {
        return ResponseEntity.ok(subjectService.getAllSubjects());
    }

    @GetMapping("/{id}")
    public ResponseEntity<Subject> getSubject(@PathVariable("id") UUID id) {
        return ResponseEntity.ok(subjectService.getSubject(id));
    }

    @PutMapping("/{id}")
    public ResponseEntity<Subject> updateSubject(
            @PathVariable("id") UUID id,
            @Valid @RequestBody UpdateSubjectRequest request) {
        return ResponseEntity.ok(subjectService.updateSubject(
                id,
                request.displayName(),
                request.description()));
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
