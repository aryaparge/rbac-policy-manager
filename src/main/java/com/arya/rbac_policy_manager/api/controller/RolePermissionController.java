package com.arya.rbac_policy_manager.api.controller;

import com.arya.rbac_policy_manager.rbac_engine.association.entity.RolePermission;
import com.arya.rbac_policy_manager.rbac_engine.association.service.RolePermissionService;
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
@RequestMapping("/api/role-permissions")
@RequiredArgsConstructor
public class RolePermissionController {

    private final RolePermissionService rolePermissionService;

    @PostMapping
    public ResponseEntity<RolePermissionResponse> assignPermissionToRole(
            @Valid @RequestBody CreateRolePermissionRequest request) {
        RolePermission assignment = rolePermissionService.assignPermissionToRole(request.permissionId(), request.roleId());
        return ResponseEntity.created(URI.create("/api/role-permissions/" + assignment.getId()))
                .body(toResponse(assignment));
    }

    @GetMapping
    public ResponseEntity<List<RolePermissionResponse>> getAssignmentsForRole(@RequestParam UUID roleId) {
        return ResponseEntity.ok(rolePermissionService.getAssignmentsForRole(roleId).stream()
                .map(RolePermissionController::toResponse)
                .toList());
    }

    @GetMapping("/{assignmentId}")
    public ResponseEntity<RolePermissionResponse> getAssignment(@PathVariable UUID assignmentId) {
        return ResponseEntity.ok(toResponse(rolePermissionService.getActiveAssignment(assignmentId)));
    }

    @PatchMapping("/{assignmentId}/disable")
    public ResponseEntity<Void> disableAssignment(@PathVariable UUID assignmentId) {
        rolePermissionService.disableAssignment(assignmentId);
        return ResponseEntity.noContent().build();
    }

    @PatchMapping("/{assignmentId}/enable")
    public ResponseEntity<Void> enableAssignment(@PathVariable UUID assignmentId) {
        rolePermissionService.enableAssignment(assignmentId);
        return ResponseEntity.noContent().build();
    }

    private static RolePermissionResponse toResponse(RolePermission assignment) {
        return new RolePermissionResponse(assignment.getId(), assignment.getName(), assignment.getStatus(),
                assignment.getCreatedAt(), assignment.getCreatedBy(), assignment.getUpdatedAt(), assignment.getUpdatedBy(),
                assignment.getDisabledAt(), assignment.getDeletedAt(), assignment.getRole().getId(), assignment.getPermission().getId());
    }

    public record CreateRolePermissionRequest(@NotNull UUID roleId, @NotNull UUID permissionId) {}

    public record RolePermissionResponse(UUID id, String name, Status status, Instant createdAt, String createdBy,
                                         Instant updatedAt, String updatedBy, Instant disabledAt, Instant deletedAt,
                                         UUID roleId, UUID permissionId) {}
}
