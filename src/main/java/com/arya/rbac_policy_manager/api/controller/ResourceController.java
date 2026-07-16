package com.arya.rbac_policy_manager.api.controller;

import com.arya.rbac_policy_manager.rbac_engine.common.Enums.Status;
import com.arya.rbac_policy_manager.rbac_engine.resource.entity.Resource;
import com.arya.rbac_policy_manager.rbac_engine.resource.service.ResourceService;
import jakarta.validation.Valid;
import jakarta.validation.constraints.NotBlank;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.net.URI;
import java.time.Instant;
import java.util.List;
import java.util.UUID;
import java.util.stream.Collectors;

@RestController
@RequestMapping("/api/resources")
@RequiredArgsConstructor
public class ResourceController {

    private final ResourceService resourceService;

    @PostMapping
    public ResponseEntity<ResourceResponse> createResource(@Valid @RequestBody CreateResourceRequest request) {
        Resource resource = resourceService.createResource(
                request.name(),
                request.displayName(),
                request.description());
        return ResponseEntity.created(URI.create("/api/resources/" + resource.getId())).body(toResponse(resource));
    }

    @GetMapping
    public ResponseEntity<List<ResourceResponse>> getResources() {
        return ResponseEntity.ok(resourceService.getAllResources().stream()
                .map(ResourceController::toResponse)
                .collect(Collectors.toList()));
    }

    @GetMapping("/{id}")
    public ResponseEntity<ResourceResponse> getResource(@PathVariable("id") UUID id) {
        return ResponseEntity.ok(toResponse(resourceService.getResource(id)));
    }

    @PutMapping("/{id}")
    public ResponseEntity<ResourceResponse> updateResource(
            @PathVariable("id") UUID id,
            @Valid @RequestBody UpdateResourceRequest request) {
        return ResponseEntity
                .ok(toResponse(resourceService.updateResource(id, request.displayName(), request.description())));
    }

    @PatchMapping("/{id}/disable")
    public ResponseEntity<Void> disableResource(@PathVariable("id") UUID id) {
        resourceService.disableResource(id);
        return ResponseEntity.noContent().build();
    }

    @PatchMapping("/{id}/enable")
    public ResponseEntity<Void> enableResource(@PathVariable("id") UUID id) {
        resourceService.enableResource(id);
        return ResponseEntity.noContent().build();
    }

    private static ResourceResponse toResponse(Resource resource) {
        return new ResourceResponse(
                resource.getId(),
                resource.getName(),
                resource.getDisplayName(),
                resource.getDescription(),
                resource.getStatus(),
                resource.getCreatedAt(),
                resource.getCreatedBy(),
                resource.getUpdatedAt(),
                resource.getUpdatedBy(),
                resource.getDisabledAt(),
                resource.getDeletedAt());
    }

    public static record ResourceResponse(
            UUID id,
            String name,
            String displayName,
            String description,
            Status status,
            Instant createdAt,
            String createdBy,
            Instant updatedAt,
            String updatedBy,
            Instant disabledAt,
            Instant deletedAt) {
    }

    public static record CreateResourceRequest(
            @NotBlank String name,
            @NotBlank String displayName,
            String description) {
    }

    public static record UpdateResourceRequest(
            @NotBlank String displayName,
            String description) {
    }
}
