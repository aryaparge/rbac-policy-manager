package com.arya.rbac_policy_manager.api.controller;

import com.arya.rbac_policy_manager.rbac_engine.action.entity.Action;
import com.arya.rbac_policy_manager.rbac_engine.action.service.ActionService;
import jakarta.validation.Valid;
import jakarta.validation.constraints.NotBlank;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.net.URI;
import java.util.List;
import java.util.UUID;

@RestController
@RequestMapping("/api/actions")
@RequiredArgsConstructor
public class ActionController {

    private final ActionService actionService;

    @PostMapping
    public ResponseEntity<Action> createAction(@Valid @RequestBody CreateActionRequest request) {
        Action action = actionService.createAction(request.name(), request.description());
        return ResponseEntity.created(URI.create("/api/actions/" + action.getId())).body(action);
    }

    @GetMapping
    public ResponseEntity<List<Action>> getActions() {
        return ResponseEntity.ok(actionService.getAllActions());
    }

    @GetMapping("/{id}")
    public ResponseEntity<Action> getAction(@PathVariable("id") UUID id) {
        return ResponseEntity.ok(actionService.getAction(id));
    }

    @PutMapping("/{id}")
    public ResponseEntity<Action> updateAction(
            @PathVariable("id") UUID id,
            @Valid @RequestBody UpdateActionRequest request) {
        return ResponseEntity.ok(actionService.updateAction(id, request.name(), request.description()));
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

    public static record CreateActionRequest(
            @NotBlank String name,
            String description) {
    }

    public static record UpdateActionRequest(
            @NotBlank String name,
            String description) {
    }
}
