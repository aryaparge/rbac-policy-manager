package com.arya.rbac_policy_manager.api.controller;

import com.arya.rbac_policy_manager.rbac_engine.association.entity.RoleHierarchy;
import com.arya.rbac_policy_manager.rbac_engine.association.hierarchyservice.RoleHierarchyService;
import com.arya.rbac_policy_manager.rbac_engine.common.Enums.Status;
import jakarta.validation.Valid;
import jakarta.validation.constraints.NotNull;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PatchMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.net.URI;
import java.time.Instant;
import java.util.UUID;

@RestController
@RequestMapping("/api/role-hierarchies")
@RequiredArgsConstructor
public class RoleHierarchyController {

    private final RoleHierarchyService roleHierarchyService;

    @PostMapping
    public ResponseEntity<RoleHierarchyResponse> assignChildToParent(
            @Valid @RequestBody CreateRoleHierarchyRequest request) {
        RoleHierarchy hierarchy = roleHierarchyService
                .createRelationship(request.parentRoleId(), request.childRoleId())
                .orElseThrow();

        return ResponseEntity.created(URI.create("/api/role-hierarchies/" + hierarchy.getId()))
                .body(toResponse(hierarchy));
    }

    @PatchMapping("/{parentRoleId}/{childRoleId}/disable")
    public ResponseEntity<Void> disableRelationship(
            @PathVariable UUID parentRoleId,
            @PathVariable UUID childRoleId) {
        roleHierarchyService.disableRelationship(parentRoleId, childRoleId);
        return ResponseEntity.noContent().build();
    }

    @PatchMapping("/{parentRoleId}/{childRoleId}/enable")
    public ResponseEntity<Void> enableRelationship(
            @PathVariable UUID parentRoleId,
            @PathVariable UUID childRoleId) {
        roleHierarchyService.enableRelationship(parentRoleId, childRoleId);
        return ResponseEntity.noContent().build();
    }

    private static RoleHierarchyResponse toResponse(RoleHierarchy hierarchy) {
        return new RoleHierarchyResponse(
                hierarchy.getId(), hierarchy.getName(), hierarchy.getStatus(), hierarchy.getCreatedAt(),
                hierarchy.getCreatedBy(), hierarchy.getUpdatedAt(), hierarchy.getUpdatedBy(),
                hierarchy.getDisabledAt(), hierarchy.getDeletedAt(), hierarchy.getParentRole().getId(),
                hierarchy.getChildRole().getId());
    }

    public record CreateRoleHierarchyRequest(@NotNull UUID parentRoleId, @NotNull UUID childRoleId) {
    }

    public record RoleHierarchyResponse(
            UUID id,
            String name,
            Status status,
            Instant createdAt,
            String createdBy,
            Instant updatedAt,
            String updatedBy,
            Instant disabledAt,
            Instant deletedAt,
            UUID parentRoleId,
            UUID childRoleId) {
    }
}
