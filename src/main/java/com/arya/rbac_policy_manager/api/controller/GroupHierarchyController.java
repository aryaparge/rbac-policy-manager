package com.arya.rbac_policy_manager.api.controller;

import com.arya.rbac_policy_manager.rbac_engine.association.entity.GroupHierarchy;
import com.arya.rbac_policy_manager.rbac_engine.association.hierarchyservice.GroupHierarchyService;
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
@RequestMapping("/api/group-hierarchies")
@RequiredArgsConstructor
public class GroupHierarchyController {

    private final GroupHierarchyService groupHierarchyService;

    @PostMapping
    public ResponseEntity<GroupHierarchyResponse> assignChildToParent(
            @Valid @RequestBody CreateGroupHierarchyRequest request) {
        GroupHierarchy hierarchy = groupHierarchyService
                .createRelationship(request.parentGroupId(), request.childGroupId())
                .orElseThrow();

        return ResponseEntity.created(URI.create("/api/group-hierarchies/" + hierarchy.getId()))
                .body(toResponse(hierarchy));
    }

    @PatchMapping("/{parentGroupId}/{childGroupId}/disable")
    public ResponseEntity<Void> disableRelationship(
            @PathVariable UUID parentGroupId,
            @PathVariable UUID childGroupId) {
        groupHierarchyService.disableRelationship(parentGroupId, childGroupId);
        return ResponseEntity.noContent().build();
    }

    @PatchMapping("/{parentGroupId}/{childGroupId}/enable")
    public ResponseEntity<Void> enableRelationship(
            @PathVariable UUID parentGroupId,
            @PathVariable UUID childGroupId) {
        groupHierarchyService.enableRelationship(parentGroupId, childGroupId);
        return ResponseEntity.noContent().build();
    }

    private static GroupHierarchyResponse toResponse(GroupHierarchy hierarchy) {
        return new GroupHierarchyResponse(
                hierarchy.getId(), hierarchy.getName(), hierarchy.getStatus(), hierarchy.getCreatedAt(),
                hierarchy.getCreatedBy(), hierarchy.getUpdatedAt(), hierarchy.getUpdatedBy(),
                hierarchy.getDisabledAt(), hierarchy.getDeletedAt(), hierarchy.getParentGroup().getId(),
                hierarchy.getChildGroup().getId());
    }

    public record CreateGroupHierarchyRequest(@NotNull UUID parentGroupId, @NotNull UUID childGroupId) {
    }

    public record GroupHierarchyResponse(
            UUID id,
            String name,
            Status status,
            Instant createdAt,
            String createdBy,
            Instant updatedAt,
            String updatedBy,
            Instant disabledAt,
            Instant deletedAt,
            UUID parentGroupId,
            UUID childGroupId) {
    }
}
