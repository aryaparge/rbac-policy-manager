package com.arya.rbac_policy_manager.rbac_engine.permission.resolution;

import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.UUID;
import java.util.stream.Collectors;

import org.springframework.stereotype.Service;

import com.arya.rbac_policy_manager.rbac_engine.association.entity.GroupPermission;
import com.arya.rbac_policy_manager.rbac_engine.association.entity.RoleGroup;
import com.arya.rbac_policy_manager.rbac_engine.association.entity.RolePermission;
import com.arya.rbac_policy_manager.rbac_engine.association.entity.SubjectRole;

import com.arya.rbac_policy_manager.rbac_engine.association.hierarchyservice.GroupHierarchyTraversalService;
import com.arya.rbac_policy_manager.rbac_engine.association.hierarchyservice.RoleHierarchyTraversalService;

import com.arya.rbac_policy_manager.rbac_engine.association.repo.GroupPermissionRepository;
import com.arya.rbac_policy_manager.rbac_engine.association.repo.RoleGroupRepository;
import com.arya.rbac_policy_manager.rbac_engine.association.repo.RolePermissionRepository;
import com.arya.rbac_policy_manager.rbac_engine.association.repo.SubjectRoleRepository;
import com.arya.rbac_policy_manager.rbac_engine.common.Enums.Status;
import com.arya.rbac_policy_manager.rbac_engine.common.exception.ActiveEntityNotFoundException;
import com.arya.rbac_policy_manager.rbac_engine.common.exception.EntityNotFoundException;

import com.arya.rbac_policy_manager.rbac_engine.group.entity.Group;
import com.arya.rbac_policy_manager.rbac_engine.permission.entity.Permission;
import com.arya.rbac_policy_manager.rbac_engine.role.entity.Role;
import com.arya.rbac_policy_manager.rbac_engine.subject.entity.Subject;

import com.arya.rbac_policy_manager.rbac_engine.subject.repo.SubjectRepository;

import lombok.RequiredArgsConstructor;

@Service
@RequiredArgsConstructor
public class PermissionResolutionService {
    private final SubjectRoleRepository subjectRoleRepository;

    private final SubjectRepository subjectRepository;

    private final RolePermissionRepository rolePermissionRepository;
    private final RoleGroupRepository roleGroupRepository;

    private final GroupPermissionRepository groupPermissionRepository;

    private final RoleHierarchyTraversalService roleHierarchyTraversalService;
    private final GroupHierarchyTraversalService groupHierarchyTraversalService;

    public Set<Permission> resolvePermissions(UUID subjectId) {
        Set<Role> reachableRoles = new HashSet<>();

        Subject subject = subjectRepository.findById(subjectId)
                .orElseThrow(() -> new EntityNotFoundException("Subject not found."));

        if (subject.getStatus() != Status.ACTIVE) {
            throw new ActiveEntityNotFoundException("Subject", subjectId);
        }

        List<SubjectRole> srAssignments = subjectRoleRepository.findBySubjectAndStatus(subject, Status.ACTIVE);
        List<Role> assignedRoles = srAssignments.stream().map(SubjectRole::getRole).toList();

        for (Role role : assignedRoles) {
            // all roles are active. checked in traversal service.
            reachableRoles.addAll(roleHierarchyTraversalService.getRoleClosure(role));
        }

        Set<Permission> permissions = new HashSet<>();

        for (Role role : reachableRoles) {
            Set<RolePermission> rpAssignments = rolePermissionRepository.findByRoleAndStatus(role, Status.ACTIVE);
            permissions.addAll(rpAssignments.stream().map(RolePermission::getPermission).collect(Collectors.toSet()));
        }

        Set<Group> reachableGroups = new HashSet<>();

        for (Role role : reachableRoles) {
            // all groups are active. checked in traversal service.
            List<RoleGroup> rgAssignments = roleGroupRepository.findByRoleAndStatus(role, Status.ACTIVE);
            List<Group> directGroups = rgAssignments.stream().map(RoleGroup::getGroup).toList();

            for (Group group : directGroups) {
                reachableGroups.addAll(groupHierarchyTraversalService.getGroupClosure(group));
            }
        }

        for (Group group : reachableGroups) {
            List<GroupPermission> gpAssignments = groupPermissionRepository.findByGroupAndStatus(group, Status.ACTIVE);
            permissions.addAll(gpAssignments.stream().map(GroupPermission::getPermission).collect(Collectors.toSet()));
        }
        return permissions;
    }
}
