package com.arya.rbac_policy_manager.api.controller;

import com.arya.rbac_policy_manager.rbac_engine.resource.entity.Resource;
import com.arya.rbac_policy_manager.rbac_engine.resource.service.ResourceService;
import jakarta.validation.Valid;
import jakarta.validation.constraints.NotBlank;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.net.URI;
import java.util.List;
import java.util.UUID;

@RestController
@RequestMapping("/api/resources")
@RequiredArgsConstructor
public class ResourceController {

    private final ResourceService resourceService;

    @PostMapping
    public ResponseEntity<Resource> createResource(@Valid @RequestBody CreateResourceRequest request) {
        Resource resource = resourceService.createResource(
                request.name(),
                request.displayName(),
                request.description());
        return ResponseEntity.created(URI.create("/api/resources/" + resource.getId())).body(resource);
    }

    @GetMapping
    public ResponseEntity<List<Resource>> getResources() {
        return ResponseEntity.ok(resourceService.getAllResources());
    }

    @GetMapping("/{id}")
    public ResponseEntity<Resource> getResource(@PathVariable("id") UUID id) {
        return ResponseEntity.ok(resourceService.getResource(id));
    }

    @PutMapping("/{id}")
    public ResponseEntity<Resource> updateResource(
            @PathVariable("id") UUID id,
            @Valid @RequestBody UpdateResourceRequest request) {
        return ResponseEntity.ok(resourceService.updateResource(id, request.displayName(), request.description()));
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
