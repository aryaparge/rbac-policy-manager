package com.arya.rbac_policy_manager.api.controller;

import com.arya.rbac_policy_manager.rbac_engine.association.hierarchyservice.RoleHierarchyService;
import com.arya.rbac_policy_manager.rbac_engine.association.service.RolePermissionService;
import com.arya.rbac_policy_manager.rbac_engine.permission.entity.Permission;
import com.arya.rbac_policy_manager.rbac_engine.role.entity.Role;
import com.arya.rbac_policy_manager.rbac_engine.role.service.RoleService;
import jakarta.validation.Valid;
import jakarta.validation.constraints.NotBlank;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.net.URI;
import java.util.List;
import java.util.Set;
import java.util.UUID;

@RestController
@RequestMapping("/api/roles")
@RequiredArgsConstructor
public class RoleController {

    private final RoleService roleService;
    private final RoleHierarchyService roleHierarchyService;
    private final RolePermissionService rolePermissionService;

    @PostMapping
    public ResponseEntity<Role> createRole(@Valid @RequestBody CreateRoleRequest request) {
        Role role = roleService.createRole(request.name(), request.description());
        return ResponseEntity.created(URI.create("/api/roles/" + role.getId())).body(role);
    }

    @GetMapping
    public ResponseEntity<List<Role>> getRoles() {
        return ResponseEntity.ok(roleService.getAllRoles());
    }

    @GetMapping("/{id}")
    public ResponseEntity<Role> getRole(@PathVariable("id") UUID id) {
        return ResponseEntity.ok(roleService.getRole(id));
    }

    @PutMapping("/{id}")
    public ResponseEntity<Role> updateRole(
            @PathVariable("id") UUID id,
            @Valid @RequestBody UpdateRoleRequest request) {
        return ResponseEntity.ok(roleService.updateRole(id, request.name(), request.description()));
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
    public ResponseEntity<Set<Role>> getRoleChildren(@PathVariable("id") UUID id) {
        Role role = roleService.getRole(id);
        return ResponseEntity.ok(roleHierarchyService.getActiveChildren(role));
    }

    @GetMapping("/{id}/permissions")
    public ResponseEntity<Set<Permission>> getRolePermissions(@PathVariable("id") UUID id) {
        Role role = roleService.getRole(id);
        return ResponseEntity.ok(rolePermissionService.getActivePermissions(role));
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
