package com.arya.rbac_policy_manager.integration;

import com.arya.rbac_policy_manager.lifecycle_cleanup.LifecycleCleanupService;
import com.arya.rbac_policy_manager.lifecycle_cleanup.LifecycleRetention;
import com.arya.rbac_policy_manager.rbac_engine.action.entity.Action;
import com.arya.rbac_policy_manager.rbac_engine.action.repo.ActionRepository;
import com.arya.rbac_policy_manager.rbac_engine.action.service.ActionService;
import com.arya.rbac_policy_manager.rbac_engine.common.Enums.Status;
import com.arya.rbac_policy_manager.rbac_engine.permission.entity.Permission;
import com.arya.rbac_policy_manager.rbac_engine.permission.repo.PermissionRepository;
import com.arya.rbac_policy_manager.rbac_engine.permission.service.PermissionService;
import com.arya.rbac_policy_manager.rbac_engine.resource.entity.Resource;
import com.arya.rbac_policy_manager.rbac_engine.resource.repo.ResourceRepository;
import com.arya.rbac_policy_manager.rbac_engine.resource.service.ResourceService;
import com.arya.rbac_policy_manager.rbac_engine.role.entity.Role;
import com.arya.rbac_policy_manager.rbac_engine.role.repo.RoleRepository;
import com.arya.rbac_policy_manager.rbac_engine.role.service.RoleService;

import lombok.extern.slf4j.Slf4j;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.transaction.annotation.Transactional;

import java.time.Instant;

import static org.assertj.core.api.Assertions.assertThat;

/**
 * Integration tests for LifecycleCleanupService.
 *
 * Each test is self-contained and transactional. Data is created fresh
 * per test; timestamps are backdated directly on the entity to simulate
 * time passage without sleeping or mocking the clock.
 *
 * Two-phase lifecycle under test:
 * DISABLED ──(after 60 days)──▶ DELETED ──(after 30 days)──▶ hard deleted
 */
@SpringBootTest
@Slf4j
@Transactional
@ActiveProfiles("test")
class LifecycleIT {

    // ── services under test ──────────────────────────────────────────────────
    @Autowired
    private LifecycleCleanupService cleanupService;

    // ── domain services (used to set up state) ───────────────────────────────
    @Autowired
    private ActionService actionService;
    @Autowired
    private ResourceService resourceService;
    @Autowired
    private PermissionService permissionService;
    @Autowired
    private RoleService roleService;

    // ── repos (used to inspect / manipulate raw state) ───────────────────────
    @Autowired
    private ActionRepository actionRepository;
    @Autowired
    private PermissionRepository permissionRepository;
    @Autowired
    private ResourceRepository resourceRepository;
    @Autowired
    private RoleRepository roleRepository;

    // ── shared fixtures ──────────────────────────────────────────────────────
    private Resource resource;
    private Action action;

    @BeforeEach
    void setUp() {
        resource = resourceService.createResource("res-lc-" + System.nanoTime(), "res1", "test resource");
        action = actionService.createAction("act-lc-" + System.nanoTime(), "test action");
    }

    // =========================================================================
    // 1. DISABLED → DELETED transition (Action)
    // An action that has been DISABLED for longer than DISABLED_RETENTION
    // (60 days) must be promoted to DELETED by cleanUpActions().
    // =========================================================================
    @Test
    void disabledAction_olderThanRetention_isMarkedDeleted() {
        actionService.disableAction(action.getId());

        // Backdate disabledAt to simulate 61 days ago
        Action stale = actionRepository.findById(action.getId()).orElseThrow();
        stale.setDisabledAt(Instant.now().minus(LifecycleRetention.DISABLED_RETENTION).minusSeconds(360));
        actionRepository.save(stale);

        cleanupService.cleanUpActions();

        Action result = actionRepository.findById(action.getId()).orElseThrow();
        assertThat(result.getStatus()).isEqualTo(Status.DELETED);
        assertThat(result.getDeletedAt()).isNotNull();
    }

    // =========================================================================
    // 2. DISABLED action within retention window is NOT promoted
    // An action disabled just now should remain DISABLED after cleanup.
    // =========================================================================
    @Test
    void disabledAction_withinRetentionWindow_isNotPromoted() {
        actionService.disableAction(action.getId());

        cleanupService.cleanUpActions();

        Action result = actionRepository.findById(action.getId()).orElseThrow();
        assertThat(result.getStatus()).isEqualTo(Status.DISABLED);
    }

    // =========================================================================
    // 3. Hard delete (Action)
    // A DELETED action whose deletedAt is older than DELETED_RETENTION
    // (30 days) must be physically removed from the database.
    // =========================================================================
    @Test
    void deletedAction_olderThanRetention_isHardDeleted() {
        // Fast-track to DELETED: disable then backdate both timestamps
        actionService.disableAction(action.getId());

        Action stale = actionRepository.findById(action.getId()).orElseThrow();
        stale.setStatus(Status.DELETED);
        stale.setDeletedAt(Instant.now().minus(LifecycleRetention.DELETED_RETENTION).minusSeconds(3600));
        actionRepository.save(stale);

        cleanupService.cleanUpActions();

        assertThat(actionRepository.findById(action.getId())).isEmpty();
    }

    // =========================================================================
    // 4. ACTIVE action is never touched by cleanup
    // =========================================================================
    @Test
    void activeAction_isNeverTouchedByCleanup() {
        cleanupService.cleanUpActions();

        Action result = actionRepository.findById(action.getId()).orElseThrow();
        assertThat(result.getStatus()).isEqualTo(Status.ACTIVE);
    }

    // =========================================================================
    // 5. DISABLED → DELETED transition (Role) – verifying the same lifecycle
    // holds for a different entity type (coverage of cleanUpRoles).
    // =========================================================================
    @Test
    void disabledRole_olderThanRetention_isMarkedDeleted() {
        Role role = roleService.createRole("role-lc-" + System.nanoTime(), "test role");
        roleService.disableRole(role.getId());

        Role stale = roleRepository.findById(role.getId()).orElseThrow();
        stale.setDisabledAt(Instant.now().minus(LifecycleRetention.DISABLED_RETENTION).minusSeconds(3600));
        roleRepository.save(stale);

        cleanupService.cleanUpRoles();

        Role result = roleRepository.findById(role.getId()).orElseThrow();
        assertThat(result.getStatus()).isEqualTo(Status.DELETED);
    }

    // =========================================================================
    // 6. Permission cleanup: stand-alone permission disabled long enough
    // is promoted to DELETED.
    // =========================================================================
    @Test
    void disabledPermission_olderThanRetention_isMarkedDeleted() {
        Permission permission = permissionService.createPermission(action.getId(), resource.getId(), "test perm");
        permissionService.disablePermission(permission.getId());

        Permission stale = permissionRepository.findById(permission.getId()).orElseThrow();
        stale.setDisabledAt(Instant.now().minus(LifecycleRetention.DISABLED_RETENTION).minusSeconds(3600));
        permissionRepository.save(stale);

        cleanupService.cleanUpPermissions();

        Permission result = permissionRepository.findById(permission.getId()).orElseThrow();
        assertThat(result.getStatus()).isEqualTo(Status.DELETED);
    }

    // =========================================================================
    // 7. Action → Permission edge: disabling an Action cascades DISABLED status
    // to its dependent Permissions (via ActionService.disableAction).
    // When cleanup runs after the retention window, both the Action and
    // its cascaded Permission are promoted to DELETED.
    //
    // This is the "Action → Permission → edge disable propagation" test.
    // =========================================================================
    @Test
    void disablingAction_cascadesDisabledToPermission_andCleanupDeletesBoth() {
        // Create a permission that depends on our shared action
        Permission permission = permissionService.createPermission(action.getId(), resource.getId(), "cascade perm");

        // Disabling the action must cascade DISABLED to the permission
        actionService.disableAction(action.getId());

        log.info("DISABLED ACTION id={} status={} disabledAt={}",
                action.getId(), action.getStatus(), action.getDisabledAt());

        Permission afterCascade = permissionRepository.findById(permission.getId()).orElseThrow();
        assertThat(afterCascade.getStatus())
                .as("Permission should be DISABLED after its Action is disabled")
                .isEqualTo(Status.DISABLED);
        assertThat(afterCascade.getDisabledAt()).isNotNull();

        // Backdate both entities so they are beyond the retention window
        Instant pastCutoff = Instant.now()
                .minus(LifecycleRetention.DISABLED_RETENTION)
                .minusSeconds(3600);

        Action staleAction = actionRepository.findById(action.getId()).orElseThrow();
        actionService.disableAction(staleAction.getId());

        afterCascade.setDisabledAt(pastCutoff);
        permissionRepository.save(afterCascade);

        // Run cleanup for both entity types
        cleanupService.cleanUpPermissions();
        cleanupService.cleanUpActions();

        // Both should now be DELETED
        Action resultAction = actionRepository.findById(action.getId()).orElseThrow();
        assertThat(resultAction.getStatus())
                .as("Action should be DELETED after cleanup")
                .isEqualTo(Status.DELETED);

        Permission resultPerm = permissionRepository.findById(permission.getId()).orElseThrow();
        assertThat(resultPerm.getStatus())
                .as("Permission should be DELETED after cleanup")
                .isEqualTo(Status.DELETED);
    }
}