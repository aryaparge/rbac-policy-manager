package com.arya.rbac_policy_manager.api.controller;

import com.arya.rbac_policy_manager.rbac_engine.permission.entity.Permission;
import com.arya.rbac_policy_manager.rbac_engine.permission.service.PermissionService;
import jakarta.validation.Valid;
import jakarta.validation.constraints.NotNull;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.net.URI;
import java.util.List;
import java.util.UUID;

@RestController
@RequestMapping("/api/permissions")
@RequiredArgsConstructor
public class PermissionController {

    private final PermissionService permissionService;

    @PostMapping
    public ResponseEntity<Permission> createPermission(@Valid @RequestBody CreatePermissionRequest request) {
        Permission permission = permissionService.createPermission(
                request.actionId(),
                request.resourceId(),
                request.description());
        return ResponseEntity.created(URI.create("/api/permissions/" + permission.getId())).body(permission);
    }

    @GetMapping
    public ResponseEntity<List<Permission>> getPermissions() {
        return ResponseEntity.ok(permissionService.getAllPermissions());
    }

    @GetMapping("/{id}")
    public ResponseEntity<Permission> getPermission(@PathVariable("id") UUID id) {
        return ResponseEntity.ok(permissionService.getPermission(id));
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

    public static record CreatePermissionRequest(
            @NotNull UUID actionId,
            @NotNull UUID resourceId,
            String description) {
    }
}
