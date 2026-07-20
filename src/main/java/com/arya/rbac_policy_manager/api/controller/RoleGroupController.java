package com.arya.rbac_policy_manager.api.controller;

import com.arya.rbac_policy_manager.rbac_engine.association.entity.RoleGroup;
import com.arya.rbac_policy_manager.rbac_engine.association.service.RoleGroupService;
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
@RequestMapping("/api/role-groups")
@RequiredArgsConstructor
public class RoleGroupController {

    private final RoleGroupService roleGroupService;

    @PostMapping
    public ResponseEntity<RoleGroupResponse> assignRoleToGroup(@Valid @RequestBody CreateRoleGroupRequest request) {
        RoleGroup assignment = roleGroupService.assignRoleToGroup(request.roleId(), request.groupId());
        return ResponseEntity.created(URI.create("/api/role-groups/" + assignment.getId()))
                .body(toResponse(assignment));
    }

    @GetMapping
    public ResponseEntity<List<RoleGroupResponse>> getAssignments(
            @RequestParam(required = false) UUID roleId,
            @RequestParam(required = false) UUID groupId) {
        if ((roleId == null) == (groupId == null)) {
            throw new IllegalArgumentException("Specify exactly one of roleId or groupId");
        }
        List<RoleGroup> assignments = roleId != null
                ? roleGroupService.getAssignmentsForRole(roleId)
                : roleGroupService.getAssignmentsForGroup(groupId);
        return ResponseEntity.ok(assignments.stream().map(RoleGroupController::toResponse).toList());
    }

    @GetMapping("/{assignmentId}")
    public ResponseEntity<RoleGroupResponse> getAssignment(@PathVariable UUID assignmentId) {
        return ResponseEntity.ok(toResponse(roleGroupService.getActiveAssignment(assignmentId)));
    }

    @PatchMapping("/{assignmentId}/disable")
    public ResponseEntity<Void> disableAssignment(@PathVariable UUID assignmentId) {
        roleGroupService.disableAssignment(assignmentId);
        return ResponseEntity.noContent().build();
    }

    @PatchMapping("/{assignmentId}/enable")
    public ResponseEntity<Void> enableAssignment(@PathVariable UUID assignmentId) {
        roleGroupService.enableAssignment(assignmentId);
        return ResponseEntity.noContent().build();
    }

    private static RoleGroupResponse toResponse(RoleGroup assignment) {
        return new RoleGroupResponse(assignment.getId(), assignment.getName(), assignment.getStatus(),
                assignment.getCreatedAt(), assignment.getCreatedBy(), assignment.getUpdatedAt(), assignment.getUpdatedBy(),
                assignment.getDisabledAt(), assignment.getDeletedAt(), assignment.getRole().getId(), assignment.getGroup().getId());
    }

    public record CreateRoleGroupRequest(@NotNull UUID roleId, @NotNull UUID groupId) {}

    public record RoleGroupResponse(UUID id, String name, Status status, Instant createdAt, String createdBy,
                                    Instant updatedAt, String updatedBy, Instant disabledAt, Instant deletedAt,
                                    UUID roleId, UUID groupId) {}
}
