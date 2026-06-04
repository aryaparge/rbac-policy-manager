package com.arya.rbac_policy_manager.rbac_engine.association.hierarchyservice;

import com.arya.rbac_policy_manager.rbac_engine.association.repo.RoleHierarchyRepository;
import com.arya.rbac_policy_manager.rbac_engine.role.entity.Role;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class RoleHierarchyValidationService {

        private static final int MAX_DEPTH = 10;

        private final RoleHierarchyRepository roleHierarchyRepository;
        private final RoleHierarchyTraversalService traversalService;

        public void validateSelfReference(Role parent, Role child) {
                if (parent.getId().equals(child.getId())) {
                        throw new IllegalArgumentException("Role cannot inherit from itself");
                }
        }


        public void validateNoCycle(Role parent, Role child) {
                if (traversalService.isReachable(child, parent)) {
                        throw new IllegalArgumentException(
                                        "Relationship would create cycle");
                }
        }

        public void validateDepthLimit(Role parent, Role child) { // Maximum hierarchy depth is defined as the longest
                                                                  // path in the DAG.
                int ancestorHeight = traversalService.getAncestorHeight(parent);
                int descendantDepth = traversalService.getMaxDescendantDepth(child);

                int resultingDepth = ancestorHeight + 1 + descendantDepth;
                if (resultingDepth >= MAX_DEPTH) {
                        throw new IllegalArgumentException("Maximum hierarchy depth exceeded");
                }
        }
}