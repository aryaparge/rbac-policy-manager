package com.arya.rbac_policy_manager.integration;

import com.arya.rbac_policy_manager.lifecycle_cleanup.LifecycleCleanupService;
import com.arya.rbac_policy_manager.lifecycle_cleanup.LifecycleRetention;
import com.arya.rbac_policy_manager.rbac_engine.action.entity.Action;
import com.arya.rbac_policy_manager.rbac_engine.action.repo.ActionRepository;
import com.arya.rbac_policy_manager.rbac_engine.action.service.ActionService;
import com.arya.rbac_policy_manager.rbac_engine.association.entity.GroupPermission;
import com.arya.rbac_policy_manager.rbac_engine.association.entity.RolePermission;
import com.arya.rbac_policy_manager.rbac_engine.association.repo.GroupPermissionRepository;
import com.arya.rbac_policy_manager.rbac_engine.association.repo.RolePermissionRepository;
import com.arya.rbac_policy_manager.rbac_engine.common.Enums.Status;
import com.arya.rbac_policy_manager.rbac_engine.group.entity.Group;
import com.arya.rbac_policy_manager.rbac_engine.group.repo.GroupRepository;
import com.arya.rbac_policy_manager.rbac_engine.permission.entity.Permission;
import com.arya.rbac_policy_manager.rbac_engine.permission.repo.PermissionRepository;
import com.arya.rbac_policy_manager.rbac_engine.permission.service.PermissionService;
import com.arya.rbac_policy_manager.rbac_engine.resource.entity.Resource;
import com.arya.rbac_policy_manager.rbac_engine.resource.repo.ResourceRepository;
import com.arya.rbac_policy_manager.rbac_engine.resource.service.ResourceService;
import com.arya.rbac_policy_manager.rbac_engine.role.entity.Role;
import com.arya.rbac_policy_manager.rbac_engine.role.repo.RoleRepository;
import com.arya.rbac_policy_manager.rbac_engine.role.service.RoleService;

import jakarta.persistence.EntityManager;
import lombok.extern.slf4j.Slf4j;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.transaction.annotation.Transactional;

import java.time.Instant;
import java.util.List;

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

    @Autowired
    private EntityManager entityManager;

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
    @Autowired
    private GroupRepository groupRepository;
    @Autowired
    private RolePermissionRepository rolePermissionRepository;
    @Autowired
    private GroupPermissionRepository groupPermissionRepository;

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
        stale.setDisabledAt(Instant.now().minus(LifecycleRetention.DISABLED_RETENTION).minusSeconds(3600));
        actionRepository.save(stale);

        cleanupService.cleanUpActions();

        Action result = actionRepository.findById(stale.getId()).orElseThrow();
        assertThat(result.getStatus()).isEqualTo(Status.DELETED);
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

        assertThat(actionRepository.findById(stale.getId())).isEmpty();
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
    // =========================================================================
    @Test
    void disablingAction_cascadesDisabledToPermission() {

        Permission permission = permissionService.createPermission(
                action.getId(),
                resource.getId(),
                "cascade perm");

        Role role = new Role();
        role.setName("ADMIN");
        role.setStatus(Status.ACTIVE);
        role.setCreatedAt(Instant.now());

        role = roleRepository.save(role);

        Group group = new Group();
        group.setName("SECURITY_TEAM");
        group.setStatus(Status.ACTIVE);
        group.setCreatedAt(Instant.now());

        group = groupRepository.save(group);

        RolePermission rp1 = new RolePermission();
        rp1.setRole(role);
        rp1.setName("rp1");
        rp1.setPermission(permission);
        rp1.setStatus(Status.ACTIVE);
        rolePermissionRepository.save(rp1);

        GroupPermission gp1 = new GroupPermission();
        gp1.setGroup(group);
        gp1.setName("gp1");
        gp1.setPermission(permission);
        gp1.setStatus(Status.ACTIVE);
        groupPermissionRepository.save(gp1);

        actionService.disableAction(action.getId());

        Permission afterCascade = permissionRepository.findById(permission.getId()).orElseThrow();

        assertThat(afterCascade.getStatus())
                .as("Permission should be DISABLED after its Action is disabled")
                .isEqualTo(Status.DISABLED);

        assertThat(afterCascade.getDisabledAt()).isNotNull();

        // ---- RolePermission cascade check ----
        List<RolePermission> rolePermissions = rolePermissionRepository.findAllByPermission(permission);

        assertThat(rolePermissions)
                .as("RolePermission should be DISABLED after Permission is disabled")
                .isNotEmpty();

        for (RolePermission rp : rolePermissions) {
            assertThat(rp.getStatus()).isEqualTo(Status.DISABLED);
            assertThat(rp.getDisabledAt()).isNotNull();
        }

        // ---- GroupPermission cascade check ----
        List<GroupPermission> groupPermissions = groupPermissionRepository.findAllByPermission(permission);

        assertThat(groupPermissions)
                .as("GroupPermission should be DISABLED after Permission is disabled")
                .isNotEmpty();

        for (GroupPermission gp : groupPermissions) {
            assertThat(gp.getStatus()).isEqualTo(Status.DISABLED);
            assertThat(gp.getDisabledAt()).isNotNull();

        }
    }
}
