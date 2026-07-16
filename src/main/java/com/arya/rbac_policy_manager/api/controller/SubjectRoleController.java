package com.arya.rbac_policy_manager.api.controller;

import com.arya.rbac_policy_manager.rbac_engine.association.service.SubjectRoleService;
import com.arya.rbac_policy_manager.rbac_engine.role.entity.Role;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.UUID;

@RestController
@RequestMapping("/api/subjects/{subjectId}/roles")
@RequiredArgsConstructor
public class SubjectRoleController {

    private final SubjectRoleService subjectRoleService;

    @PostMapping("/{roleId}")
    public ResponseEntity<Void> assignRoleToSubject(
            @PathVariable("subjectId") UUID subjectId,
            @PathVariable("roleId") UUID roleId) {
        subjectRoleService.assignSubjectToRole(subjectId, roleId);
        return ResponseEntity.noContent().build();
    }

    @DeleteMapping("/{roleId}")
    public ResponseEntity<Void> removeRoleFromSubject(
            @PathVariable("subjectId") UUID subjectId,
            @PathVariable("roleId") UUID roleId) {
        // The service only disables by assignment ID. The API cannot remove by role
        // mapping
        // without an assignment identifier. A future service method is required for
        // direct subject-role deletion.
        return ResponseEntity.status(501).build();
    }

    @GetMapping
    public ResponseEntity<List<Role>> getRolesForSubject(@PathVariable("subjectId") UUID subjectId) {
        return ResponseEntity.ok(subjectRoleService.getActiveRoles(subjectId));
    }
}
