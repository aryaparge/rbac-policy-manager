package com.arya.rbac_policy_manager.api.controller;

import com.arya.rbac_policy_manager.rbac_engine.association.entity.SubjectRole;
import com.arya.rbac_policy_manager.rbac_engine.association.service.SubjectRoleService;
import com.arya.rbac_policy_manager.rbac_engine.common.Enums.Status;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PatchMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.time.Instant;
import java.util.UUID;

@RestController
@RequestMapping("/api/subject-roles")
@RequiredArgsConstructor
public class SubjectRoleAssignmentController {

    private final SubjectRoleService subjectRoleService;

    @GetMapping("/{assignmentId}")
    public ResponseEntity<SubjectRoleResponse> getAssignment(@PathVariable UUID assignmentId) {
        return ResponseEntity.ok(toResponse(subjectRoleService.getActiveAssignment(assignmentId)));
    }

    @PatchMapping("/{assignmentId}/disable")
    public ResponseEntity<Void> disableAssignment(@PathVariable UUID assignmentId) {
        subjectRoleService.disableAssignment(assignmentId);
        return ResponseEntity.noContent().build();
    }

    @PatchMapping("/{assignmentId}/enable")
    public ResponseEntity<Void> enableAssignment(@PathVariable UUID assignmentId) {
        subjectRoleService.enableAssignment(assignmentId);
        return ResponseEntity.noContent().build();
    }

    private static SubjectRoleResponse toResponse(SubjectRole assignment) {
        return new SubjectRoleResponse(assignment.getId(), assignment.getName(), assignment.getStatus(),
                assignment.getCreatedAt(), assignment.getCreatedBy(), assignment.getUpdatedAt(), assignment.getUpdatedBy(),
                assignment.getDisabledAt(), assignment.getDeletedAt(), assignment.getSubject().getId(), assignment.getRole().getId());
    }

    public record SubjectRoleResponse(UUID id, String name, Status status, Instant createdAt, String createdBy,
                                      Instant updatedAt, String updatedBy, Instant disabledAt, Instant deletedAt,
                                      UUID subjectId, UUID roleId) {}
}
