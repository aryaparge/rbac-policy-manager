package com.arya.rbac_policy_manager.api.controller;

import com.arya.rbac_policy_manager.rbac_engine.association.hierarchyservice.GroupHierarchyService;
import com.arya.rbac_policy_manager.rbac_engine.association.service.GroupPermissionService;
import com.arya.rbac_policy_manager.rbac_engine.group.entity.Group;
import com.arya.rbac_policy_manager.rbac_engine.group.service.GroupService;
import com.arya.rbac_policy_manager.rbac_engine.permission.entity.Permission;
import com.arya.rbac_policy_manager.rbac_engine.common.Enums.Status;
import jakarta.validation.Valid;
import jakarta.validation.constraints.NotBlank;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.net.URI;
import java.time.Instant;
import java.util.List;
import java.util.Set;
import java.util.UUID;
import java.util.stream.Collectors;

@RestController
@RequestMapping("/api/groups")
@RequiredArgsConstructor
public class GroupController {

    private final GroupService groupService;
    private final GroupHierarchyService groupHierarchyService;
    private final GroupPermissionService groupPermissionService;

    @PostMapping
    public ResponseEntity<GroupResponse> createGroup(@Valid @RequestBody CreateGroupRequest request) {
        Group group = groupService.createGroup(request.name(), request.description());
        return ResponseEntity.created(URI.create("/api/groups/" + group.getId())).body(toResponse(group));
    }

    @GetMapping
    public ResponseEntity<List<GroupResponse>> getGroups() {
        return ResponseEntity.ok(groupService.getAllGroups().stream()
                .map(GroupController::toResponse)
                .collect(Collectors.toList()));
    }

    @GetMapping("/{id}")
    public ResponseEntity<GroupResponse> getGroup(@PathVariable("id") UUID id) {
        return ResponseEntity.ok(toResponse(groupService.getGroup(id)));
    }

    @PutMapping("/{id}")
    public ResponseEntity<GroupResponse> updateGroup(
            @PathVariable("id") UUID id,
            @Valid @RequestBody UpdateGroupRequest request) {
        return ResponseEntity.ok(toResponse(groupService.updateGroup(id, request.name(), request.description())));
    }

    @PatchMapping("/{id}/disable")
    public ResponseEntity<Void> disableGroup(@PathVariable("id") UUID id) {
        groupService.disableGroup(id);
        return ResponseEntity.noContent().build();
    }

    @PatchMapping("/{id}/enable")
    public ResponseEntity<Void> enableGroup(@PathVariable("id") UUID id) {
        groupService.enableGroup(id);
        return ResponseEntity.noContent().build();
    }

    @GetMapping("/{id}/children")
    public ResponseEntity<Set<GroupResponse>> getGroupChildren(@PathVariable("id") UUID id) {
        Group group = groupService.getGroup(id);
        return ResponseEntity.ok(groupHierarchyService.getActiveChildren(group).stream()
                .map(GroupController::toResponse)
                .collect(Collectors.toSet()));
    }

    @GetMapping("/{id}/permissions")
    public ResponseEntity<Set<PermissionResponse>> getGroupPermissions(@PathVariable("id") UUID id) {
        Group group = groupService.getGroup(id);
        return ResponseEntity.ok(groupPermissionService.getActivePermissions(group).stream()
                .map(GroupController::toPermissionResponse)
                .collect(Collectors.toSet()));
    }

    private static GroupResponse toResponse(Group group) {
        return new GroupResponse(
                group.getId(),
                group.getName(),
                group.getDescription(),
                group.getStatus(),
                group.getCreatedAt(),
                group.getCreatedBy(),
                group.getUpdatedAt(),
                group.getUpdatedBy(),
                group.getDisabledAt(),
                group.getDeletedAt());
    }

    private static PermissionResponse toPermissionResponse(Permission permission) {
        return new PermissionResponse(
                permission.getId(),
                permission.getName(),
                permission.getDescription(),
                permission.getStatus(),
                permission.getCreatedAt(),
                permission.getCreatedBy(),
                permission.getUpdatedAt(),
                permission.getUpdatedBy(),
                permission.getDisabledAt(),
                permission.getDeletedAt(),
                permission.getAction().getId(),
                permission.getResource().getId());
    }

    public static record GroupResponse(
            UUID id,
            String name,
            String description,
            Status status,
            Instant createdAt,
            String createdBy,
            Instant updatedAt,
            String updatedBy,
            Instant disabledAt,
            Instant deletedAt) {
    }

    public static record PermissionResponse(
            UUID id,
            String name,
            String description,
            Status status,
            Instant createdAt,
            String createdBy,
            Instant updatedAt,
            String updatedBy,
            Instant disabledAt,
            Instant deletedAt,
            UUID actionId,
            UUID resourceId) {
    }

    public static record CreateGroupRequest(
            @NotBlank String name,
            String description) {
    }

    public static record UpdateGroupRequest(
            @NotBlank String name,
            String description) {
    }
}
