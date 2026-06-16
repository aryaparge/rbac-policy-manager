package com.arya.rbac_policy_manager.unit;

import com.arya.rbac_policy_manager.explainability_engine.AuthorizationPath;
import com.arya.rbac_policy_manager.explainability_engine.AuthorizationPathTraversalService;
import com.arya.rbac_policy_manager.explainability_engine.NodeType;
import com.arya.rbac_policy_manager.explainability_engine.PathNode;
import com.arya.rbac_policy_manager.rbac_engine.association.hierarchyservice.GroupHierarchyService;
import com.arya.rbac_policy_manager.rbac_engine.association.hierarchyservice.RoleHierarchyService;
import com.arya.rbac_policy_manager.rbac_engine.association.service.GroupPermissionService;
import com.arya.rbac_policy_manager.rbac_engine.association.service.RoleGroupService;
import com.arya.rbac_policy_manager.rbac_engine.association.service.RolePermissionService;
import com.arya.rbac_policy_manager.rbac_engine.association.service.SubjectRoleService;
import com.arya.rbac_policy_manager.rbac_engine.group.entity.Group;
import com.arya.rbac_policy_manager.rbac_engine.permission.entity.Permission;
import com.arya.rbac_policy_manager.rbac_engine.permission.service.PermissionService;
import com.arya.rbac_policy_manager.rbac_engine.role.entity.Role;
import com.arya.rbac_policy_manager.rbac_engine.subject.entity.Subject;
import com.arya.rbac_policy_manager.rbac_engine.subject.service.SubjectService;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.List;
import java.util.Set;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
@DisplayName("AuthorizationPathTraversalService")
class AuthorizationPathTraversalServiceTest {

    @Mock
    SubjectService subjectService;
    @Mock
    PermissionService permissionService;
    @Mock
    SubjectRoleService subjectRoleService;
    @Mock
    RoleHierarchyService roleHierarchyService;
    @Mock
    RolePermissionService rolePermissionService;
    @Mock
    RoleGroupService roleGroupService;
    @Mock
    GroupHierarchyService groupHierarchyService;
    @Mock
    GroupPermissionService groupPermissionService;

    @InjectMocks
    AuthorizationPathTraversalService service;

    // ── Fixture IDs ────────────────────────────────────────────────────────────
    final UUID subjectId = UUID.randomUUID();
    final UUID resourceId = UUID.randomUUID();
    final UUID actionId = UUID.randomUUID();
    final UUID permissionId = UUID.randomUUID();
    final UUID roleId = UUID.randomUUID();
    final UUID groupId = UUID.randomUUID();

    Subject subject;
    Permission targetPermission;

    @BeforeEach
    void baseSetup() {
        subject = subject("alice", subjectId);
        targetPermission = permission("docs:read", permissionId);

        when(subjectService.getActiveSubject(subjectId)).thenReturn(subject);
        when(permissionService.getActivePermission(resourceId, actionId)).thenReturn(targetPermission);
    }

    // ── Helpers ────────────────────────────────────────────────────────────────

    static Subject subject(String name, UUID id) {
        Subject s = new Subject();
        s.setId(id);
        s.setName(name);
        return s;
    }

    static Role role(String name, UUID id) {
        Role r = new Role();
        r.setId(id);
        r.setName(name);
        return r;
    }

    static Group group(String name, UUID id) {
        Group g = new Group();
        g.setId(id);
        g.setName(name);
        return g;
    }

    static Permission permission(String name, UUID id) {
        Permission p = new Permission();
        p.setId(id);
        p.setName(name);
        return p;
    }

    /** Stub a role with no children, no groups, and no permissions by default. */
    void stubEmptyRole(Role r) {
        lenient().when(rolePermissionService.getActivePermissions(r)).thenReturn(Set.of());
        lenient().when(roleHierarchyService.getActiveChildren(r)).thenReturn(Set.of());
        lenient().when(roleGroupService.getActiveGroups(r)).thenReturn(Set.of());
    }

    /** Stub a group with no children and no permissions by default. */
    void stubEmptyGroup(Group g) {
        lenient().when(groupPermissionService.getActivePermissions(g)).thenReturn(Set.of());
        lenient().when(groupHierarchyService.getActiveChildren(g)).thenReturn(Set.of());
    }

    // ══════════════════════════════════════════════════════════════════════════
    // 1. No paths
    // ══════════════════════════════════════════════════════════════════════════

    @Nested
    @DisplayName("No authorization paths")
    class NoPaths {

        @Test
        @DisplayName("returns empty set when subject has no roles")
        void noRoles() {
            when(subjectRoleService.getActiveRoles(subjectId)).thenReturn(List.of());

            Set<AuthorizationPath> paths = service.findPaths(subjectId, resourceId, actionId);

            assertThat(paths).isEmpty();
        }

        @Test
        @DisplayName("returns empty set when role has no matching permission and no children or groups")
        void roleWithNoPermissions() {
            Role r = role("editor", roleId);
            when(subjectRoleService.getActiveRoles(subjectId)).thenReturn(List.of(r));
            stubEmptyRole(r);

            assertThat(service.findPaths(subjectId, resourceId, actionId)).isEmpty();
        }

        @Test
        @DisplayName("returns empty set when role has a different permission only")
        void roleWithDifferentPermission() {
            Role r = role("editor", roleId);
            Permission other = permission("docs:write", UUID.randomUUID());

            when(subjectRoleService.getActiveRoles(subjectId)).thenReturn(List.of(r));
            when(rolePermissionService.getActivePermissions(r)).thenReturn(Set.of(other));
            when(roleHierarchyService.getActiveChildren(r)).thenReturn(Set.of());
            when(roleGroupService.getActiveGroups(r)).thenReturn(Set.of());

            assertThat(service.findPaths(subjectId, resourceId, actionId)).isEmpty();
        }

        @Test
        @DisplayName("returns empty set when group has no matching permission")
        void groupWithNoMatchingPermission() {
            Role r = role("editor", roleId);
            Group g = group("team-a", groupId);
            Permission other = permission("docs:write", UUID.randomUUID());

            when(subjectRoleService.getActiveRoles(subjectId)).thenReturn(List.of(r));
            when(rolePermissionService.getActivePermissions(r)).thenReturn(Set.of());
            when(roleHierarchyService.getActiveChildren(r)).thenReturn(Set.of());
            when(roleGroupService.getActiveGroups(r)).thenReturn(Set.of(g));
            when(groupPermissionService.getActivePermissions(g)).thenReturn(Set.of(other));
            when(groupHierarchyService.getActiveChildren(g)).thenReturn(Set.of());

            assertThat(service.findPaths(subjectId, resourceId, actionId)).isEmpty();
        }
    }

    // ══════════════════════════════════════════════════════════════════════════
    // 2. Direct role → permission
    // ══════════════════════════════════════════════════════════════════════════

    @Nested
    @DisplayName("Direct role → permission path")
    class DirectRolePath {

        @Test
        @DisplayName("finds single path: subject → role → permission")
        void singleDirectPath() {
            Role r = role("editor", roleId);

            when(subjectRoleService.getActiveRoles(subjectId)).thenReturn(List.of(r));
            when(rolePermissionService.getActivePermissions(r)).thenReturn(Set.of(targetPermission));
            when(roleHierarchyService.getActiveChildren(r)).thenReturn(Set.of());
            when(roleGroupService.getActiveGroups(r)).thenReturn(Set.of());

            Set<AuthorizationPath> paths = service.findPaths(subjectId, resourceId, actionId);

            assertThat(paths).hasSize(1);
            AuthorizationPath path = paths.iterator().next();
            assertThat(path.getNodes()).extracting(PathNode::getType)
                    .containsExactly(NodeType.SUBJECT, NodeType.ROLE, NodeType.PERMISSION);
            assertThat(path.getNodes()).extracting(PathNode::getName)
                    .containsExactly("alice", "editor", "docs:read");
        }

        @Test
        @DisplayName("finds multiple paths when subject has two roles that both grant permission")
        void twoDirectPaths() {
            Role r1 = role("editor", UUID.randomUUID());
            Role r2 = role("admin", UUID.randomUUID());

            when(subjectRoleService.getActiveRoles(subjectId)).thenReturn(List.of(r1, r2));
            when(rolePermissionService.getActivePermissions(r1)).thenReturn(Set.of(targetPermission));
            when(rolePermissionService.getActivePermissions(r2)).thenReturn(Set.of(targetPermission));
            when(roleHierarchyService.getActiveChildren(r1)).thenReturn(Set.of());
            when(roleHierarchyService.getActiveChildren(r2)).thenReturn(Set.of());
            when(roleGroupService.getActiveGroups(r1)).thenReturn(Set.of());
            when(roleGroupService.getActiveGroups(r2)).thenReturn(Set.of());

            Set<AuthorizationPath> paths = service.findPaths(subjectId, resourceId, actionId);

            assertThat(paths).hasSize(2);
        }
    }

    // ══════════════════════════════════════════════════════════════════════════
    // 3. Role hierarchy path
    // ══════════════════════════════════════════════════════════════════════════

    @Nested
    @DisplayName("Role hierarchy path")
    class RoleHierarchyPath {

        @Test
        @DisplayName("finds path through parent → child role")
        void parentChildRole() {
            Role parent = role("manager", UUID.randomUUID());
            Role child = role("employee", UUID.randomUUID());

            when(subjectRoleService.getActiveRoles(subjectId)).thenReturn(List.of(parent));
            when(rolePermissionService.getActivePermissions(parent)).thenReturn(Set.of());
            when(roleHierarchyService.getActiveChildren(parent)).thenReturn(Set.of(child));
            when(roleGroupService.getActiveGroups(parent)).thenReturn(Set.of());

            when(rolePermissionService.getActivePermissions(child)).thenReturn(Set.of(targetPermission));
            when(roleHierarchyService.getActiveChildren(child)).thenReturn(Set.of());
            when(roleGroupService.getActiveGroups(child)).thenReturn(Set.of());

            Set<AuthorizationPath> paths = service.findPaths(subjectId, resourceId, actionId);

            assertThat(paths).hasSize(1);
            assertThat(paths.iterator().next().getNodes()).extracting(PathNode::getName)
                    .containsExactly("alice", "manager", "employee", "docs:read");
        }

        @Test
        @DisplayName("finds both paths when parent and child role both grant permission")
        void parentAndChildBothHavePermission() {
            Role parent = role("manager", UUID.randomUUID());
            Role child = role("employee", UUID.randomUUID());

            when(subjectRoleService.getActiveRoles(subjectId)).thenReturn(List.of(parent));
            when(rolePermissionService.getActivePermissions(parent)).thenReturn(Set.of(targetPermission));
            when(roleHierarchyService.getActiveChildren(parent)).thenReturn(Set.of(child));
            when(roleGroupService.getActiveGroups(parent)).thenReturn(Set.of());

            when(rolePermissionService.getActivePermissions(child)).thenReturn(Set.of(targetPermission));
            when(roleHierarchyService.getActiveChildren(child)).thenReturn(Set.of());
            when(roleGroupService.getActiveGroups(child)).thenReturn(Set.of());

            Set<AuthorizationPath> paths = service.findPaths(subjectId, resourceId, actionId);

            assertThat(paths).hasSize(2);
        }
    }

    // ══════════════════════════════════════════════════════════════════════════
    // 4. Role → group → permission
    // ══════════════════════════════════════════════════════════════════════════

    @Nested
    @DisplayName("Role → group → permission path")
    class RoleGroupPath {

        @Test
        @DisplayName("finds path: subject → role → group → permission")
        void roleToGroup() {
            Role r = role("editor", roleId);
            Group g = group("team-a", groupId);

            when(subjectRoleService.getActiveRoles(subjectId)).thenReturn(List.of(r));
            when(rolePermissionService.getActivePermissions(r)).thenReturn(Set.of());
            when(roleHierarchyService.getActiveChildren(r)).thenReturn(Set.of());
            when(roleGroupService.getActiveGroups(r)).thenReturn(Set.of(g));

            when(groupPermissionService.getActivePermissions(g)).thenReturn(Set.of(targetPermission));
            when(groupHierarchyService.getActiveChildren(g)).thenReturn(Set.of());

            Set<AuthorizationPath> paths = service.findPaths(subjectId, resourceId, actionId);

            assertThat(paths).hasSize(1);
            assertThat(paths.iterator().next().getNodes()).extracting(PathNode::getType)
                    .containsExactly(NodeType.SUBJECT, NodeType.ROLE, NodeType.GROUP, NodeType.PERMISSION);
        }
    }

    // ══════════════════════════════════════════════════════════════════════════
    // 5. Group hierarchy path
    // ══════════════════════════════════════════════════════════════════════════

    @Nested
    @DisplayName("Group hierarchy path")
    class GroupHierarchyPath {

        @Test
        @DisplayName("finds path through parent → child group")
        void parentChildGroup() {
            Role r = role("editor", roleId);
            Group parentGroup = group("org", UUID.randomUUID());
            Group childGroup = group("team-a", UUID.randomUUID());

            when(subjectRoleService.getActiveRoles(subjectId)).thenReturn(List.of(r));
            when(rolePermissionService.getActivePermissions(r)).thenReturn(Set.of());
            when(roleHierarchyService.getActiveChildren(r)).thenReturn(Set.of());
            when(roleGroupService.getActiveGroups(r)).thenReturn(Set.of(parentGroup));

            when(groupPermissionService.getActivePermissions(parentGroup)).thenReturn(Set.of());
            when(groupHierarchyService.getActiveChildren(parentGroup)).thenReturn(Set.of(childGroup));

            when(groupPermissionService.getActivePermissions(childGroup)).thenReturn(Set.of(targetPermission));
            when(groupHierarchyService.getActiveChildren(childGroup)).thenReturn(Set.of());

            Set<AuthorizationPath> paths = service.findPaths(subjectId, resourceId, actionId);

            assertThat(paths).hasSize(1);
            assertThat(paths.iterator().next().getNodes()).extracting(PathNode::getName)
                    .containsExactly("alice", "editor", "org", "team-a", "docs:read");
        }
    }

    // ══════════════════════════════════════════════════════════════════════════
    // 6. Cycle detection
    // ══════════════════════════════════════════════════════════════════════════

    @Nested
    @DisplayName("Cycle detection")
    class CycleDetection {

        @Test
        @DisplayName("does not loop infinitely when role hierarchy contains a cycle")
        void roleCycle() {
            Role r1 = role("role-a", UUID.randomUUID());
            Role r2 = role("role-b", UUID.randomUUID());

            when(subjectRoleService.getActiveRoles(subjectId)).thenReturn(List.of(r1));

            // r1 → r2 → r1 (cycle)
            when(rolePermissionService.getActivePermissions(r1)).thenReturn(Set.of());
            when(roleHierarchyService.getActiveChildren(r1)).thenReturn(Set.of(r2));
            when(roleGroupService.getActiveGroups(r1)).thenReturn(Set.of());

            when(rolePermissionService.getActivePermissions(r2)).thenReturn(Set.of());
            when(roleHierarchyService.getActiveChildren(r2)).thenReturn(Set.of(r1)); // cycle back
            when(roleGroupService.getActiveGroups(r2)).thenReturn(Set.of());

            // Should terminate and find no paths
            Set<AuthorizationPath> paths = service.findPaths(subjectId, resourceId, actionId);
            assertThat(paths).isEmpty();
        }

        @Test
        @DisplayName("does not loop infinitely when group hierarchy contains a cycle")
        void groupCycle() {
            Role r = role("editor", roleId);
            Group g1 = group("group-a", UUID.randomUUID());
            Group g2 = group("group-b", UUID.randomUUID());

            when(subjectRoleService.getActiveRoles(subjectId)).thenReturn(List.of(r));
            when(rolePermissionService.getActivePermissions(r)).thenReturn(Set.of());
            when(roleHierarchyService.getActiveChildren(r)).thenReturn(Set.of());
            when(roleGroupService.getActiveGroups(r)).thenReturn(Set.of(g1));

            // g1 → g2 → g1 (cycle)
            when(groupPermissionService.getActivePermissions(g1)).thenReturn(Set.of());
            when(groupHierarchyService.getActiveChildren(g1)).thenReturn(Set.of(g2));

            when(groupPermissionService.getActivePermissions(g2)).thenReturn(Set.of());
            when(groupHierarchyService.getActiveChildren(g2)).thenReturn(Set.of(g1)); // cycle back

            Set<AuthorizationPath> paths = service.findPaths(subjectId, resourceId, actionId);
            assertThat(paths).isEmpty();
        }

        @Test
        @DisplayName("still finds permission when a role cycle exists on a sibling branch")
        void roleCycleOnSiblingBranchDoesNotBlockOtherPaths() {
            Role rCycle = role("role-cycle", UUID.randomUUID());
            Role rDirect = role("role-direct", UUID.randomUUID());

            when(subjectRoleService.getActiveRoles(subjectId)).thenReturn(List.of(rCycle, rDirect));

            // cycle branch – no permission found
            when(rolePermissionService.getActivePermissions(rCycle)).thenReturn(Set.of());
            when(roleHierarchyService.getActiveChildren(rCycle)).thenReturn(Set.of(rCycle)); // self-loop
            when(roleGroupService.getActiveGroups(rCycle)).thenReturn(Set.of());

            // direct branch – permission found
            when(rolePermissionService.getActivePermissions(rDirect)).thenReturn(Set.of(targetPermission));
            when(roleHierarchyService.getActiveChildren(rDirect)).thenReturn(Set.of());
            when(roleGroupService.getActiveGroups(rDirect)).thenReturn(Set.of());

            Set<AuthorizationPath> paths = service.findPaths(subjectId, resourceId, actionId);
            assertThat(paths).hasSize(1);
        }
    }

    // ══════════════════════════════════════════════════════════════════════════
    // 7. Shared group across multiple roles
    // ══════════════════════════════════════════════════════════════════════════

    @Nested
    @DisplayName("Shared group across roles")
    class SharedGroup {

        @Test
        @DisplayName("same group reachable via two roles produces two distinct paths")
        void sharedGroupTwoRoles() {
            Role r1 = role("role-a", UUID.randomUUID());
            Role r2 = role("role-b", UUID.randomUUID());
            Group g = group("shared-group", groupId);

            when(subjectRoleService.getActiveRoles(subjectId)).thenReturn(List.of(r1, r2));

            when(rolePermissionService.getActivePermissions(r1)).thenReturn(Set.of());
            when(roleHierarchyService.getActiveChildren(r1)).thenReturn(Set.of());
            when(roleGroupService.getActiveGroups(r1)).thenReturn(Set.of(g));

            when(rolePermissionService.getActivePermissions(r2)).thenReturn(Set.of());
            when(roleHierarchyService.getActiveChildren(r2)).thenReturn(Set.of());
            when(roleGroupService.getActiveGroups(r2)).thenReturn(Set.of(g));

            when(groupPermissionService.getActivePermissions(g)).thenReturn(Set.of(targetPermission));
            when(groupHierarchyService.getActiveChildren(g)).thenReturn(Set.of());

            Set<AuthorizationPath> paths = service.findPaths(subjectId, resourceId, actionId);

            // Each role forms its own path: alice→r1→g→perm and alice→r2→g→perm
            assertThat(paths).hasSize(2);
        }
    }

    // ══════════════════════════════════════════════════════════════════════════
    // 8. explain() – display strings
    // ══════════════════════════════════════════════════════════════════════════

    @Nested
    @DisplayName("explain() returns display strings")
    class ExplainMethod {

        @Test
        @DisplayName("returns non-empty set of strings for a found path")
        void returnsStringsForFoundPath() {
            Role r = role("editor", roleId);

            when(subjectRoleService.getActiveRoles(subjectId)).thenReturn(List.of(r));
            when(rolePermissionService.getActivePermissions(r)).thenReturn(Set.of(targetPermission));
            when(roleHierarchyService.getActiveChildren(r)).thenReturn(Set.of());
            when(roleGroupService.getActiveGroups(r)).thenReturn(Set.of());

            Set<String> explanations = service.explain(subjectId, resourceId, actionId);

            assertThat(explanations).isNotEmpty();
            assertThat(explanations.iterator().next()).isNotBlank();
        }

        @Test
        @DisplayName("returns empty set when no paths exist")
        void returnsEmptyWhenNoPaths() {
            when(subjectRoleService.getActiveRoles(subjectId)).thenReturn(List.of());

            assertThat(service.explain(subjectId, resourceId, actionId)).isEmpty();
        }
    }

    // ══════════════════════════════════════════════════════════════════════════
    // 9. Deep / mixed hierarchy
    // ══════════════════════════════════════════════════════════════════════════

    @Nested
    @DisplayName("Deep mixed hierarchy")
    class DeepHierarchy {

        @Test
        @DisplayName("finds path through role chain then group chain")
        void roleChainToGroupChain() {
            Role r1 = role("r1", UUID.randomUUID());
            Role r2 = role("r2", UUID.randomUUID());
            Group g1 = group("g1", UUID.randomUUID());
            Group g2 = group("g2", UUID.randomUUID());

            when(subjectRoleService.getActiveRoles(subjectId)).thenReturn(List.of(r1));

            when(rolePermissionService.getActivePermissions(r1)).thenReturn(Set.of());
            when(roleHierarchyService.getActiveChildren(r1)).thenReturn(Set.of(r2));
            when(roleGroupService.getActiveGroups(r1)).thenReturn(Set.of());

            when(rolePermissionService.getActivePermissions(r2)).thenReturn(Set.of());
            when(roleHierarchyService.getActiveChildren(r2)).thenReturn(Set.of());
            when(roleGroupService.getActiveGroups(r2)).thenReturn(Set.of(g1));

            when(groupPermissionService.getActivePermissions(g1)).thenReturn(Set.of());
            when(groupHierarchyService.getActiveChildren(g1)).thenReturn(Set.of(g2));

            when(groupPermissionService.getActivePermissions(g2)).thenReturn(Set.of(targetPermission));
            when(groupHierarchyService.getActiveChildren(g2)).thenReturn(Set.of());

            Set<AuthorizationPath> paths = service.findPaths(subjectId, resourceId, actionId);

            assertThat(paths).hasSize(1);
            assertThat(paths.iterator().next().getNodes()).extracting(PathNode::getName)
                    .containsExactly("alice", "r1", "r2", "g1", "g2", "docs:read");
        }
    }
}
