package com.arya.rbac_policy_manager.rbac_engine.permission.pep;

import java.util.UUID;

import org.springframework.stereotype.Service;

import com.arya.rbac_policy_manager.rbac_engine.common.exception.AccessDeniedException;
import com.arya.rbac_policy_manager.rbac_engine.permission.pdp.AuthorizationDecision;
import com.arya.rbac_policy_manager.rbac_engine.permission.pdp.PolicyDecisionPoint;

import lombok.RequiredArgsConstructor;

@Service
@RequiredArgsConstructor
public class PolicyEnforcementPoint {
    private final PolicyDecisionPoint pdp;

    public void enforce(
            UUID subjectId,
            UUID resourceId,
            UUID actionId) {
        AuthorizationDecision decision = pdp.evaluate(
                subjectId,
                resourceId,
                actionId);

        if (!decision.isAllowed()) {
            throw new AccessDeniedException(
                    "Access denied.");
        }
    }

}
