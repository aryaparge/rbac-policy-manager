package com.arya.rbac_policy_manager.rbac_engine.association.service;

import com.arya.rbac_policy_manager.rbac_engine.association.entity.RoleHierarchy;
import com.arya.rbac_policy_manager.rbac_engine.association.repo.RoleHierarchyRepository;
import com.arya.rbac_policy_manager.rbac_engine.common.Enum.Status;
import com.arya.rbac_policy_manager.rbac_engine.role.entity.Role;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.*;

@Service
@RequiredArgsConstructor
public class RoleHierarchyTraversalService {
    private final RoleHierarchyRepository roleHierarchyRepository;

    public boolean isReachable(Role source, Role target) {
        Set<UUID> visited = new HashSet<>();
        Deque<Role> stack = new ArrayDeque<>();

        stack.push(source);

        while (!stack.isEmpty()) {
            Role current = stack.pop();

            if (!visited.add(current.getId())) {
                continue;
            }
            if (current.getId().equals(target.getId())) {
                return true;
            }

            List<RoleHierarchy> children = roleHierarchyRepository.findByParentRoleAndStatus(current, Status.ACTIVE);
            for (RoleHierarchy edge : children) {
                stack.push(edge.getChildRole());
            }
        }
        return false;
    }

    public Set<Role> getDescendants(Role role) {
        Set<Role> descendants = new HashSet<>();
        Deque<Role> stack = new ArrayDeque<>();

        stack.push(role);

        while (!stack.isEmpty()) {
            Role current = stack.pop();

            List<RoleHierarchy> children = roleHierarchyRepository.findByParentRoleAndStatus(current, Status.ACTIVE);
            for (RoleHierarchy edge : children) {
                Role child = edge.getChildRole();
                if (descendants.add(child)) {
                    stack.push(child);
                }
            }
        }
        return descendants;
    }

    public Set<Role> getRoleClosure(Role role) {
        Set<Role> descendants = new HashSet<>();
        Deque<Role> stack = new ArrayDeque<>();
        descendants.add(role);
        stack.push(role);

        while (!stack.isEmpty()) {
            Role current = stack.pop();

            List<RoleHierarchy> children = roleHierarchyRepository.findByParentRoleAndStatus(current, Status.ACTIVE);
            for (RoleHierarchy edge : children) {
                Role child = edge.getChildRole();
                if (descendants.add(child)) {
                    stack.push(child);
                }
            }
        }
        return descendants;
    }

    private Set<Role> getAncestors(Role role) {
        Set<Role> ancestors = new HashSet<>();
        Deque<Role> stack = new ArrayDeque<>();

        stack.push(role);

        while (!stack.isEmpty()) {
            Role current = stack.pop();

            List<RoleHierarchy> parents = roleHierarchyRepository.findByChildRoleAndStatus(current, Status.ACTIVE);
            for (RoleHierarchy edge : parents) {
                Role parent = edge.getParentRole();
                if (ancestors.add(parent)) {
                    stack.push(parent);
                }
            }
        }
        return ancestors;
    }

    public int getMaxDescendantDepth(Role role) {
        return getMaxDescendantDepth(role, new HashMap<>());
    }

    private int getMaxDescendantDepth(Role role, Map<UUID, Integer> memo) {
        if (memo.containsKey(role.getId())) {
            return memo.get(role.getId());
        }

        List<RoleHierarchy> children = roleHierarchyRepository.findByParentRoleAndStatus(role, Status.ACTIVE);
        if (children.isEmpty()) {
            memo.put(role.getId(), 0);
            return 0;
        }

        int max = 0;
        for (RoleHierarchy edge : children) {
            max = Math.max(max, 1 + getMaxDescendantDepth(edge.getChildRole(), memo));
        }

        memo.put(role.getId(), max);
        return max;
    }

    public int getAncestorHeight(Role role) {
        return getAncestorHeight(role, new HashMap<>());
    }

    private int getAncestorHeight(Role role, Map<UUID, Integer> memo) {
        if (memo.containsKey(role.getId())) {
            return memo.get(role.getId());
        }

        List<RoleHierarchy> parents = roleHierarchyRepository.findByChildRoleAndStatus(role, Status.ACTIVE);

        if (parents.isEmpty()) {
            memo.put(role.getId(), 0);
            return 0;
        }

        int max = 0;
        for (RoleHierarchy edge : parents) {

            max = Math.max(max, 1 + getAncestorHeight(edge.getParentRole(), memo));
        }

        memo.put(role.getId(), max);
        return max;
    }
}