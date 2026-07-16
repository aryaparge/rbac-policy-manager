package com.arya.rbac_policy_manager.api.controller;

import com.arya.rbac_policy_manager.rbac_engine.association.hierarchyservice.GroupHierarchyService;
import com.arya.rbac_policy_manager.rbac_engine.association.service.GroupPermissionService;
import com.arya.rbac_policy_manager.rbac_engine.group.entity.Group;
import com.arya.rbac_policy_manager.rbac_engine.group.service.GroupService;
import com.arya.rbac_policy_manager.rbac_engine.permission.entity.Permission;
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
@RequestMapping("/api/groups")
@RequiredArgsConstructor
public class GroupController {

    private final GroupService groupService;
    private final GroupHierarchyService groupHierarchyService;
    private final GroupPermissionService groupPermissionService;

    @PostMapping
    public ResponseEntity<Group> createGroup(@Valid @RequestBody CreateGroupRequest request) {
        Group group = groupService.createGroup(request.name(), request.description());
        return ResponseEntity.created(URI.create("/api/groups/" + group.getId())).body(group);
    }

    @GetMapping
    public ResponseEntity<List<Group>> getGroups() {
        return ResponseEntity.ok(groupService.getAllGroups());
    }

    @GetMapping("/{id}")
    public ResponseEntity<Group> getGroup(@PathVariable("id") UUID id) {
        return ResponseEntity.ok(groupService.getGroup(id));
    }

    @PutMapping("/{id}")
    public ResponseEntity<Group> updateGroup(
            @PathVariable("id") UUID id,
            @Valid @RequestBody UpdateGroupRequest request) {
        return ResponseEntity.ok(groupService.updateGroup(id, request.name(), request.description()));
    }

    @PatchMapping("/{id}/disable")
    public ResponseEntity<Void> disableGroup(@PathVariable("id") UUID id) {
        groupService.disableGroup(id);
        return ResponseEntity.noContent().build();
    }

    @PatchMapping("/{id}/enable")
    public ResponseEntity<Void> enableGroup(@PathVariable("id") UUID id) {
        groupService.enableGroup(id);
        return ResponseEntity.noContent().build();
    }

    @GetMapping("/{id}/children")
    public ResponseEntity<Set<Group>> getGroupChildren(@PathVariable("id") UUID id) {
        Group group = groupService.getGroup(id);
        return ResponseEntity.ok(groupHierarchyService.getActiveChildren(group));
    }

    @GetMapping("/{id}/permissions")
    public ResponseEntity<Set<Permission>> getGroupPermissions(@PathVariable("id") UUID id) {
        Group group = groupService.getGroup(id);
        return ResponseEntity.ok(groupPermissionService.getActivePermissions(group));
    }

    public static record CreateGroupRequest(
            @NotBlank String name,
            String displayName,
            String description) {
    }

    public static record UpdateGroupRequest(
            @NotBlank String name,
            String description) {
    }
}
