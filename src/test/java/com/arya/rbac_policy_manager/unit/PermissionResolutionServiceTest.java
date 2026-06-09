package com.arya.rbac_policy_manager.unit;

import com.arya.rbac_policy_manager.rbac_engine.association.entity.RolePermission;
import com.arya.rbac_policy_manager.rbac_engine.association.entity.SubjectRole;
import com.arya.rbac_policy_manager.rbac_engine.association.hierarchyservice.GroupHierarchyTraversalService;
import com.arya.rbac_policy_manager.rbac_engine.association.hierarchyservice.RoleHierarchyTraversalService;
import com.arya.rbac_policy_manager.rbac_engine.association.repo.GroupPermissionRepository;
import com.arya.rbac_policy_manager.rbac_engine.association.repo.RoleGroupRepository;
import com.arya.rbac_policy_manager.rbac_engine.association.repo.RolePermissionRepository;
import com.arya.rbac_policy_manager.rbac_engine.association.repo.SubjectRoleRepository;
import com.arya.rbac_policy_manager.rbac_engine.common.Enums.Status;
import com.arya.rbac_policy_manager.rbac_engine.permission.entity.Permission;
import com.arya.rbac_policy_manager.rbac_engine.permission.resolution.PermissionResolutionService;
import com.arya.rbac_policy_manager.rbac_engine.role.entity.Role;
import com.arya.rbac_policy_manager.rbac_engine.subject.entity.Subject;
import com.arya.rbac_policy_manager.rbac_engine.subject.repo.SubjectRepository;
import com.arya.rbac_policy_manager.rbac_engine.action.entity.Action;
import com.arya.rbac_policy_manager.rbac_engine.resource.entity.Resource;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.List;
import java.util.Optional;
import java.util.Set;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.when;


@ExtendWith(MockitoExtension.class)
class PermissionResolutionServiceTest {

    @Mock private SubjectRepository subjectRepository;
    @Mock private SubjectRoleRepository subjectRoleRepository;
    @Mock private RolePermissionRepository rolePermissionRepository;
    @Mock private RoleGroupRepository roleGroupRepository;
    @Mock private GroupPermissionRepository groupPermissionRepository;
    @Mock private RoleHierarchyTraversalService roleHierarchyTraversalService;
    @Mock private GroupHierarchyTraversalService groupHierarchyTraversalService;

    @InjectMocks
    private PermissionResolutionService permissionResolutionService;

    private Subject subject;
    private Role roleAdmin;
    private Role roleEditor;
    private Permission permRead;
    private Permission permWrite;

    @BeforeEach
    void setUp() {
        subject  = subject("alice");
        roleAdmin  = role("ADMIN");
        roleEditor = role("EDITOR");
        permRead   = permission("documents", "READ");
        permWrite  = permission("documents", "WRITE");
    }

    
    @Test
    void shouldResolveDirectRolePermissions() {
        when(subjectRepository.findById(subject.getId())).thenReturn(Optional.of(subject));
        SubjectRole sr = subjectRole(subject, roleAdmin);
        when(subjectRoleRepository.findBySubjectAndStatus(eq(subject), eq(Status.ACTIVE)))
                .thenReturn(List.of(sr));

        // Role closure = {roleAdmin} only
        when(roleHierarchyTraversalService.getRoleClosure(roleAdmin))
                .thenReturn(Set.of(roleAdmin));

        RolePermission rp = rolePermission(roleAdmin, permRead);
        when(rolePermissionRepository.findByRoleAndStatus(eq(roleAdmin), eq(Status.ACTIVE)))
                .thenReturn(Set.of(rp));

        // No groups attached to this role
        when(roleGroupRepository.findByRoleAndStatus(eq(roleAdmin), eq(Status.ACTIVE)))
                .thenReturn(List.of());

        Set<Permission> resolved = permissionResolutionService.resolvePermissions(subject.getId());

        assertThat(resolved).containsExactly(permRead);
    }

   
    @Test
    void shouldResolveInheritedPermissions() {
        when(subjectRepository.findById(subject.getId())).thenReturn(Optional.of(subject));
        SubjectRole sr = subjectRole(subject, roleEditor);
        when(subjectRoleRepository.findBySubjectAndStatus(eq(subject), eq(Status.ACTIVE)))
                .thenReturn(List.of(sr));

        // Editor's closure includes itself and ADMIN (its parent in hierarchy)
        when(roleHierarchyTraversalService.getRoleClosure(roleEditor))
                .thenReturn(Set.of(roleEditor, roleAdmin));

        // ADMIN → READ, EDITOR → WRITE
        RolePermission rpAdmin  = rolePermission(roleAdmin, permRead);
        RolePermission rpEditor = rolePermission(roleEditor, permWrite);
        when(rolePermissionRepository.findByRoleAndStatus(eq(roleAdmin), eq(Status.ACTIVE)))
                .thenReturn(Set.of(rpAdmin));
        when(rolePermissionRepository.findByRoleAndStatus(eq(roleEditor), eq(Status.ACTIVE)))
                .thenReturn(Set.of(rpEditor));

        when(roleGroupRepository.findByRoleAndStatus(eq(roleAdmin), eq(Status.ACTIVE)))
                .thenReturn(List.of());
        when(roleGroupRepository.findByRoleAndStatus(eq(roleEditor), eq(Status.ACTIVE)))
                .thenReturn(List.of());

        Set<Permission> resolved = permissionResolutionService.resolvePermissions(subject.getId());

        assertThat(resolved).containsExactlyInAnyOrder(permRead, permWrite);
    }

    @Test
    void shouldRemoveDuplicatePermissions() {
        when(subjectRepository.findById(subject.getId())).thenReturn(Optional.of(subject));

        SubjectRole srAdmin  = subjectRole(subject, roleAdmin);
        SubjectRole srEditor = subjectRole(subject, roleEditor);
        when(subjectRoleRepository.findBySubjectAndStatus(eq(subject), eq(Status.ACTIVE)))
                .thenReturn(List.of(srAdmin, srEditor));

        // Both role closures include their own role only
        when(roleHierarchyTraversalService.getRoleClosure(roleAdmin))
                .thenReturn(Set.of(roleAdmin));
        when(roleHierarchyTraversalService.getRoleClosure(roleEditor))
                .thenReturn(Set.of(roleEditor));

        // Both roles grant the SAME permRead object (same UUID)
        RolePermission rp1 = rolePermission(roleAdmin, permRead);
        RolePermission rp2 = rolePermission(roleEditor, permRead);
        when(rolePermissionRepository.findByRoleAndStatus(eq(roleAdmin), eq(Status.ACTIVE)))
                .thenReturn(Set.of(rp1));
        when(rolePermissionRepository.findByRoleAndStatus(eq(roleEditor), eq(Status.ACTIVE)))
                .thenReturn(Set.of(rp2));

        when(roleGroupRepository.findByRoleAndStatus(eq(roleAdmin), eq(Status.ACTIVE)))
                .thenReturn(List.of());
        when(roleGroupRepository.findByRoleAndStatus(eq(roleEditor), eq(Status.ACTIVE)))
                .thenReturn(List.of());

        Set<Permission> resolved = permissionResolutionService.resolvePermissions(subject.getId());

        // Set semantics — permRead (same object) appears once
        assertThat(resolved).hasSize(1);
        assertThat(resolved).contains(permRead);
    }

    private Subject subject(String name) {
        Subject s = new Subject();
        s.setId(UUID.randomUUID());
        s.setName(name);
        s.setDisplayName(name);
        s.setStatus(Status.ACTIVE);
        return s;
    }

    private Role role(String name) {
        Role r = new Role();
        r.setId(UUID.randomUUID());
        r.setName(name);
        r.setStatus(Status.ACTIVE);
        return r;
    }

    private Permission permission(String resourceName, String actionName) {
        Resource resource = new Resource();
        resource.setId(UUID.randomUUID());
        resource.setName(resourceName);
        resource.setStatus(Status.ACTIVE);

        Action action = new Action();
        action.setId(UUID.randomUUID());
        action.setName(actionName);
        action.setStatus(Status.ACTIVE);

        Permission p = new Permission();
        p.setId(UUID.randomUUID());
        p.setResource(resource);
        p.setAction(action);
        p.setStatus(Status.ACTIVE);
        return p;
    }

    private SubjectRole subjectRole(Subject subject, Role role) {
        SubjectRole sr = new SubjectRole();
        sr.setId(UUID.randomUUID());
        sr.setSubject(subject);
        sr.setRole(role);
        sr.setStatus(Status.ACTIVE);
        return sr;
    }

    private RolePermission rolePermission(Role role, Permission permission) {
        RolePermission rp = new RolePermission();
        rp.setId(UUID.randomUUID());
        rp.setRole(role);
        rp.setPermission(permission);
        rp.setStatus(Status.ACTIVE);
        return rp;
    }
}