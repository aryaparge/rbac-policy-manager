package com.arya.rbac_policy_manager.api.controller;

import com.arya.rbac_policy_manager.rbac_engine.association.entity.GroupPermission;
import com.arya.rbac_policy_manager.rbac_engine.association.service.GroupPermissionService;
import com.arya.rbac_policy_manager.rbac_engine.common.Enums.Status;
import jakarta.validation.Valid;
import jakarta.validation.constraints.NotNull;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PatchMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.net.URI;
import java.time.Instant;
import java.util.List;
import java.util.UUID;

@RestController
@RequestMapping("/api/group-permissions")
@RequiredArgsConstructor
public class GroupPermissionController {

    private final GroupPermissionService groupPermissionService;

    @PostMapping
    public ResponseEntity<GroupPermissionResponse> assignPermissionToGroup(
            @Valid @RequestBody CreateGroupPermissionRequest request) {
        GroupPermission assignment = groupPermissionService.assignPermissionToGroup(request.permissionId(), request.groupId());
        return ResponseEntity.created(URI.create("/api/group-permissions/" + assignment.getId()))
                .body(toResponse(assignment));
    }

    @GetMapping
    public ResponseEntity<List<GroupPermissionResponse>> getAssignmentsForGroup(@RequestParam UUID groupId) {
        return ResponseEntity.ok(groupPermissionService.getAssignmentsForGroup(groupId).stream()
                .map(GroupPermissionController::toResponse)
                .toList());
    }

    @GetMapping("/{assignmentId}")
    public ResponseEntity<GroupPermissionResponse> getAssignment(@PathVariable UUID assignmentId) {
        return ResponseEntity.ok(toResponse(groupPermissionService.getActiveAssignment(assignmentId)));
    }

    @PatchMapping("/{assignmentId}/disable")
    public ResponseEntity<Void> disableAssignment(@PathVariable UUID assignmentId) {
        groupPermissionService.disableAssignment(assignmentId);
        return ResponseEntity.noContent().build();
    }

    @PatchMapping("/{assignmentId}/enable")
    public ResponseEntity<Void> enableAssignment(@PathVariable UUID assignmentId) {
        groupPermissionService.enableAssignment(assignmentId);
        return ResponseEntity.noContent().build();
    }

    private static GroupPermissionResponse toResponse(GroupPermission assignment) {
        return new GroupPermissionResponse(assignment.getId(), assignment.getName(), assignment.getStatus(),
                assignment.getCreatedAt(), assignment.getCreatedBy(), assignment.getUpdatedAt(), assignment.getUpdatedBy(),
                assignment.getDisabledAt(), assignment.getDeletedAt(), assignment.getGroup().getId(), assignment.getPermission().getId());
    }

    public record CreateGroupPermissionRequest(@NotNull UUID groupId, @NotNull UUID permissionId) {}

    public record GroupPermissionResponse(UUID id, String name, Status status, Instant createdAt, String createdBy,
                                          Instant updatedAt, String updatedBy, Instant disabledAt, Instant deletedAt,
                                          UUID groupId, UUID permissionId) {}
}
