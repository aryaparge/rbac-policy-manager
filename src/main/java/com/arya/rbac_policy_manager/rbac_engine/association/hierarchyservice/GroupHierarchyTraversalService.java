package com.arya.rbac_policy_manager.rbac_engine.association.hierarchyservice;

import com.arya.rbac_policy_manager.rbac_engine.association.entity.GroupHierarchy;
import com.arya.rbac_policy_manager.rbac_engine.association.repo.GroupHierarchyRepository;
import com.arya.rbac_policy_manager.rbac_engine.common.Enum.Status;
import com.arya.rbac_policy_manager.rbac_engine.group.entity.Group;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.*;

@Service
@RequiredArgsConstructor
public class GroupHierarchyTraversalService {
    private final GroupHierarchyRepository groupHierarchyRepository;

    public boolean isReachable(Group source, Group target) {
        Set<UUID> visited = new HashSet<>();
        Deque<Group> stack = new ArrayDeque<>();

        stack.push(source);

        while (!stack.isEmpty()) {
            Group current = stack.pop();

            if (!visited.add(current.getId())) {
                continue;
            }
            if (current.getId().equals(target.getId())) {
                return true;
            }

            List<GroupHierarchy> children = groupHierarchyRepository.findByParentGroupAndStatus(current, Status.ACTIVE);
            for (GroupHierarchy edge : children) {
                stack.push(edge.getChildGroup());
            }
        }
        return false;
    }

    public Set<Group> getDescendants(Group group) {
        Set<Group> descendants = new HashSet<>();
        Deque<Group> stack = new ArrayDeque<>();

        stack.push(group);

        while (!stack.isEmpty()) {
            Group current = stack.pop();

            List<GroupHierarchy> children = groupHierarchyRepository.findByParentGroupAndStatus(current, Status.ACTIVE);
            for (GroupHierarchy edge : children) {
                Group child = edge.getChildGroup();
                if (descendants.add(child)) {
                    stack.push(child);
                }
            }
        }
        return descendants;
    }

    public Set<Group> getGroupClosure(Group group) {
        Set<Group> descendants = new HashSet<>();
        Deque<Group> stack = new ArrayDeque<>();
        descendants.add(group);
        stack.push(group);

        while (!stack.isEmpty()) {
            Group current = stack.pop();

            List<GroupHierarchy> children = groupHierarchyRepository.findByParentGroupAndStatus(current, Status.ACTIVE);
            for (GroupHierarchy edge : children) {
                Group child = edge.getChildGroup();
                if (descendants.add(child)) {
                    stack.push(child);
                }
            }
        }
        return descendants;
    }

    private Set<Group> getAncestors(Group group) {
        Set<Group> ancestors = new HashSet<>();
        Deque<Group> stack = new ArrayDeque<>();

        stack.push(group);

        while (!stack.isEmpty()) {
            Group current = stack.pop();

            List<GroupHierarchy> parents = groupHierarchyRepository.findByChildGroupAndStatus(current, Status.ACTIVE);
            for (GroupHierarchy edge : parents) {
                Group parent = edge.getParentGroup();
                if (ancestors.add(parent)) {
                    stack.push(parent);
                }
            }
        }
        return ancestors;
    }

    public int getMaxDescendantDepth(Group group) {
        return getMaxDescendantDepth(group, new HashMap<>());
    }

    private int getMaxDescendantDepth(Group group, Map<UUID, Integer> memo) {
        if (memo.containsKey(group.getId())) {
            return memo.get(group.getId());
        }

        List<GroupHierarchy> children = groupHierarchyRepository.findByParentGroupAndStatus(group, Status.ACTIVE);
        if (children.isEmpty()) {
            memo.put(group.getId(), 0);
            return 0;
        }

        int max = 0;
        for (GroupHierarchy edge : children) {
            max = Math.max(max, 1 + getMaxDescendantDepth(edge.getChildGroup(), memo));
        }

        memo.put(group.getId(), max);
        return max;
    }

    public int getAncestorHeight(Group group) {
        return getAncestorHeight(group, new HashMap<>());
    }

    private int getAncestorHeight(Group group, Map<UUID, Integer> memo) {
        if (memo.containsKey(group.getId())) {
            return memo.get(group.getId());
        }

        List<GroupHierarchy> parents = groupHierarchyRepository.findByChildGroupAndStatus(group, Status.ACTIVE);

        if (parents.isEmpty()) {
            memo.put(group.getId(), 0);
            return 0;
        }

        int max = 0;
        for (GroupHierarchy edge : parents) {

            max = Math.max(max, 1 + getAncestorHeight(edge.getParentGroup(), memo));
        }

        memo.put(group.getId(), max);
        return max;
    }
}