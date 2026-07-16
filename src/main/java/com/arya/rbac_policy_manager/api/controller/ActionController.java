package com.arya.rbac_policy_manager.api.controller;

import com.arya.rbac_policy_manager.rbac_engine.action.entity.Action;
import com.arya.rbac_policy_manager.rbac_engine.action.service.ActionService;
import com.arya.rbac_policy_manager.rbac_engine.common.Enums.Status;
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
@RequestMapping("/api/actions")
@RequiredArgsConstructor
public class ActionController {

    private final ActionService actionService;

    @PostMapping
    public ResponseEntity<ActionResponse> createAction(@Valid @RequestBody CreateActionRequest request) {
        Action action = actionService.createAction(request.name(), request.description());
        return ResponseEntity.created(URI.create("/api/actions/" + action.getId())).body(toResponse(action));
    }

    @GetMapping
    public ResponseEntity<List<ActionResponse>> getActions() {
        return ResponseEntity.ok(actionService.getAllActions().stream()
                .map(ActionController::toResponse)
                .collect(Collectors.toList()));
    }

    @GetMapping("/{id}")
    public ResponseEntity<ActionResponse> getAction(@PathVariable("id") UUID id) {
        return ResponseEntity.ok(toResponse(actionService.getAction(id)));
    }

    @PutMapping("/{id}")
    public ResponseEntity<ActionResponse> updateAction(
            @PathVariable("id") UUID id,
            @Valid @RequestBody UpdateActionRequest request) {
        return ResponseEntity.ok(toResponse(actionService.updateAction(id, request.name(), request.description())));
    }

    @PatchMapping("/{id}/disable")
    public ResponseEntity<Void> disableAction(@PathVariable("id") UUID id) {
        actionService.disableAction(id);
        return ResponseEntity.noContent().build();
    }

    @PatchMapping("/{id}/enable")
    public ResponseEntity<Void> enableAction(@PathVariable("id") UUID id) {
        actionService.enableAction(id);
        return ResponseEntity.noContent().build();
    }

    private static ActionResponse toResponse(Action action) {
        return new ActionResponse(
                action.getId(),
                action.getName(),
                action.getDescription(),
                action.getStatus(),
                action.getCreatedAt(),
                action.getCreatedBy(),
                action.getUpdatedAt(),
                action.getUpdatedBy(),
                action.getDisabledAt(),
                action.getDeletedAt());
    }

    public static record ActionResponse(
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

    public static record CreateActionRequest(
            @NotBlank String name,
            String description) {
    }

    public static record UpdateActionRequest(
            @NotBlank String name,
            String description) {
    }
}
