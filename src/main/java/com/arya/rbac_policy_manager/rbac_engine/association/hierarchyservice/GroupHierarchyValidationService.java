package com.arya.rbac_policy_manager.rbac_engine.association.hierarchyservice;

import com.arya.rbac_policy_manager.rbac_engine.association.repo.GroupHierarchyRepository;
import com.arya.rbac_policy_manager.rbac_engine.group.entity.Group;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class GroupHierarchyValidationService {

        private static final int MAX_DEPTH = 10;

        private final GroupHierarchyRepository groupHierarchyRepository;
        private final GroupHierarchyTraversalService traversalService;

        public void validateSelfReference(Group parent, Group child) {
                if (parent.getId().equals(child.getId())) {
                        throw new IllegalArgumentException("Group cannot inherit from itself");
                }
        }

        public void validateNoCycle(Group parent, Group child) {
                if (traversalService.isReachable(child, parent)) {
                        throw new IllegalArgumentException(
                                        "Relationship would create cycle");
                }
        }

        public void validateDepthLimit(Group parent, Group child) { // Maximum hierarchy depth is defined as the longest
                                                                  // path in the DAG.
                int ancestorHeight = traversalService.getAncestorHeight(parent);
                int descendantDepth = traversalService.getMaxDescendantDepth(child);

                int resultingDepth = ancestorHeight + 1 + descendantDepth;
                if (resultingDepth >= MAX_DEPTH) {
                        throw new IllegalArgumentException("Maximum hierarchy depth exceeded");
                }
        }
}