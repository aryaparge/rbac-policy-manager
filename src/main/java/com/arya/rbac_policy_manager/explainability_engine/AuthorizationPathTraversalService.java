package com.arya.rbac_policy_manager.explainability_engine;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.UUID;
import java.util.stream.Collectors;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

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

import lombok.RequiredArgsConstructor;

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class AuthorizationPathTraversalService {

        private final SubjectService subjectService;
        private final PermissionService permissionService;
        private final SubjectRoleService subjectRoleService;
        private final RoleHierarchyService roleHierarchyService;
        private final RolePermissionService rolePermissionService;
        private final RoleGroupService roleGroupService;
        private final GroupHierarchyService groupHierarchyService;
        private final GroupPermissionService groupPermissionService;

        public Set<String> explain(
                        UUID subjectId,
                        UUID resourceId,
                        UUID actionId) {
                return findPaths(subjectId, resourceId, actionId)
                                .stream()
                                .map(AuthorizationPath::toDisplayString)
                                .collect(Collectors.toSet());
        }

        public Set<AuthorizationPath> findPaths(UUID subjectId, UUID resourceId, UUID actionId) {
                Subject subject = subjectService.getActiveSubject(subjectId);
                Permission target = permissionService.getActivePermission(resourceId, actionId);

                Set<AuthorizationPath> results = new HashSet<>();
                List<PathNode> currentPath = new ArrayList<>();

                currentPath.add(new PathNode(subject.getName(), subject.getId(), NodeType.SUBJECT));

                Set<UUID> visitedRoles = new HashSet<>();
                Set<UUID> visitedGroups = new HashSet<>();

                for (Role role : subjectRoleService.getActiveRoles(subjectId)) {
                        dfsRole(role, target, currentPath, results, visitedRoles, new HashSet<>(visitedGroups));
                }

                return results;
        }

        private void dfsRole(
                        Role role,
                        Permission target,
                        List<PathNode> path,
                        Set<AuthorizationPath> results,
                        Set<UUID> visitedRoles,
                        Set<UUID> visitedGroups) {

                if (!visitedRoles.add(role.getId()))
                        return;

                path.add(new PathNode(role.getName(), role.getId(), NodeType.ROLE));

                if (rolePermissionService.getActivePermissions(role).stream()
                                .anyMatch(permission -> permission.getId().equals(target.getId()))) {
                        path.add(new PathNode(target.getName(), target.getId(), NodeType.PERMISSION));
                        results.add(AuthorizationPath.of(path));
                        path.remove(path.size() - 1);
                }

                for (Role child : roleHierarchyService.getActiveChildren(role)) {
                        dfsRole(child, target, path, results, new HashSet<>(visitedRoles), visitedGroups);
                }

                for (Group group : roleGroupService.getActiveGroups(role)) {
                        dfsGroup(group, target, path, results, visitedGroups);
                }

                path.remove(path.size() - 1);
                visitedRoles.remove(role.getId());
        }

        private void dfsGroup(
                        Group group,
                        Permission target,
                        List<PathNode> path,
                        Set<AuthorizationPath> results,
                        Set<UUID> visitedGroups) {

                if (!visitedGroups.add(group.getId()))
                        return;

                path.add(new PathNode(group.getName(), group.getId(), NodeType.GROUP));

                if (groupPermissionService.getActivePermissions(group).stream()
                                .anyMatch(permission -> permission.getId().equals(target.getId()))) {
                        path.add(new PathNode(target.getName(), target.getId(), NodeType.PERMISSION));
                        results.add(AuthorizationPath.of(path));
                        path.remove(path.size() - 1);
                }

                for (Group child : groupHierarchyService.getActiveChildren(group)) {
                        dfsGroup(child, target, path, results, new HashSet<>(visitedGroups));
                }

                path.remove(path.size() - 1);
                visitedGroups.remove(group.getId());
        }
}