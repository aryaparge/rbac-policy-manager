package com.arya.rbac_policy_manager.api.controller;

import com.arya.rbac_policy_manager.rbac_engine.common.Enums.Status;
import com.arya.rbac_policy_manager.rbac_engine.permission.entity.Permission;
import com.arya.rbac_policy_manager.rbac_engine.permission.service.PermissionService;
import jakarta.validation.Valid;
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
@RequestMapping("/api/permissions")
@RequiredArgsConstructor
public class PermissionController {

    private final PermissionService permissionService;

    @PostMapping
    public ResponseEntity<PermissionResponse> createPermission(@Valid @RequestBody CreatePermissionRequest request) {
        Permission permission = permissionService.createPermission(
                request.actionId(),
                request.resourceId(),
                request.description());
        return ResponseEntity.created(URI.create("/api/permissions/" + permission.getId()))
                .body(toResponse(permission));
    }

    @GetMapping
    public ResponseEntity<List<PermissionResponse>> getPermissions() {
        return ResponseEntity.ok(permissionService.getAllPermissions().stream()
                .map(PermissionController::toResponse)
                .collect(Collectors.toList()));
    }

    @GetMapping("/{id}")
    public ResponseEntity<PermissionResponse> getPermission(@PathVariable("id") UUID id) {
        return ResponseEntity.ok(toResponse(permissionService.getPermission(id)));
    }

    @PatchMapping("/{id}/disable")
    public ResponseEntity<Void> disablePermission(@PathVariable("id") UUID id) {
        permissionService.disablePermission(id);
        return ResponseEntity.noContent().build();
    }

    @PatchMapping("/{id}/enable")
    public ResponseEntity<Void> enablePermission(@PathVariable("id") UUID id) {
        permissionService.enablePermission(id);
        return ResponseEntity.noContent().build();
    }

    private static PermissionResponse toResponse(Permission permission) {
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

    public static record CreatePermissionRequest(
            @NotNull UUID actionId,
            @NotNull UUID resourceId,
            String description) {
    }
}
