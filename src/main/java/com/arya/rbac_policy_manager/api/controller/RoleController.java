package com.arya.rbac_policy_manager.api.controller;

import com.arya.rbac_policy_manager.rbac_engine.association.hierarchyservice.RoleHierarchyService;
import com.arya.rbac_policy_manager.rbac_engine.association.service.RolePermissionService;
import com.arya.rbac_policy_manager.rbac_engine.common.Enums.Status;
import com.arya.rbac_policy_manager.rbac_engine.permission.entity.Permission;
import com.arya.rbac_policy_manager.rbac_engine.role.entity.Role;
import com.arya.rbac_policy_manager.rbac_engine.role.service.RoleService;
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
@RequestMapping("/api/roles")
@RequiredArgsConstructor
public class RoleController {

    private final RoleService roleService;
    private final RoleHierarchyService roleHierarchyService;
    private final RolePermissionService rolePermissionService;

    @PostMapping
    public ResponseEntity<RoleResponse> createRole(@Valid @RequestBody CreateRoleRequest request) {
        Role role = roleService.createRole(request.name(), request.description());
        return ResponseEntity.created(URI.create("/api/roles/" + role.getId())).body(toResponse(role));
    }

    @GetMapping
    public ResponseEntity<List<RoleResponse>> getRoles() {
        return ResponseEntity.ok(roleService.getAllRoles().stream()
                .map(RoleController::toResponse)
                .collect(Collectors.toList()));
    }

    @GetMapping("/{id}")
    public ResponseEntity<RoleResponse> getRole(@PathVariable("id") UUID id) {
        return ResponseEntity.ok(toResponse(roleService.getRole(id)));
    }

    @PutMapping("/{id}")
    public ResponseEntity<RoleResponse> updateRole(
            @PathVariable("id") UUID id,
            @Valid @RequestBody UpdateRoleRequest request) {
        return ResponseEntity.ok(toResponse(roleService.updateRole(id, request.name(), request.description())));
    }

    @PatchMapping("/{id}/disable")
    public ResponseEntity<Void> disableRole(@PathVariable("id") UUID id) {
        roleService.disableRole(id);
        return ResponseEntity.noContent().build();
    }

    @PatchMapping("/{id}/enable")
    public ResponseEntity<Void> enableRole(@PathVariable("id") UUID id) {
        roleService.enableRole(id);
        return ResponseEntity.noContent().build();
    }

    @GetMapping("/{id}/children")
    public ResponseEntity<Set<RoleResponse>> getRoleChildren(@PathVariable("id") UUID id) {
        Role role = roleService.getRole(id);
        return ResponseEntity.ok(roleHierarchyService.getActiveChildren(role).stream()
                .map(RoleController::toResponse)
                .collect(Collectors.toSet()));
    }

    @GetMapping("/{id}/permissions")
    public ResponseEntity<Set<Permission>> getRolePermissions(@PathVariable("id") UUID id) {
        Role role = roleService.getRole(id);
        return ResponseEntity.ok(rolePermissionService.getActivePermissions(role));
    }

    private static RoleResponse toResponse(Role role) {
        return new RoleResponse(
                role.getId(),
                role.getName(),
                role.getDescription(),
                role.getStatus(),
                role.getCreatedAt(),
                role.getCreatedBy(),
                role.getUpdatedAt(),
                role.getUpdatedBy(),
                role.getDisabledAt(),
                role.getDeletedAt());
    }

    public static record RoleResponse(
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

    public static record CreateRoleRequest(
            @NotBlank String name,
            String displayName,
            String description) {
    }

    public static record UpdateRoleRequest(
            @NotBlank String name,
            String description) {
    }
}
