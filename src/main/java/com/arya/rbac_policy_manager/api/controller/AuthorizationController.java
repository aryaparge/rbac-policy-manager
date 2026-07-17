package com.arya.rbac_policy_manager.api.controller;

import com.arya.rbac_policy_manager.explainability_engine.AuthorizationPath;
import com.arya.rbac_policy_manager.explainability_engine.AuthorizationPathTraversalService;
import com.arya.rbac_policy_manager.rbac_engine.common.Enums.Decision;
import com.arya.rbac_policy_manager.rbac_engine.permission.pdp.AuthorizationDecision;
import com.arya.rbac_policy_manager.rbac_engine.permission.pdp.PolicyDecisionPoint;
import jakarta.validation.Valid;
import jakarta.validation.constraints.NotNull;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.Collections;
import java.util.Set;
import java.util.UUID;

/**
 * Public policy-decision and explanation endpoint for policy-management clients.
 * Paths are returned only for an ALLOW decision because the traversal engine
 * models positive permission evidence rather than denial evidence.
 */
@RestController
@RequestMapping("/api/authorization")
@RequiredArgsConstructor
public class AuthorizationController {

    private final PolicyDecisionPoint policyDecisionPoint;
    private final AuthorizationPathTraversalService authorizationPathTraversalService;

    @PostMapping("/check")
    public ResponseEntity<AuthorizationCheckResponse> checkAuthorization(
            @Valid @RequestBody AuthorizationCheckRequest request) {
        AuthorizationDecision authorizationDecision = policyDecisionPoint.evaluate(
                request.subjectId(), request.resourceId(), request.actionId());

        Set<AuthorizationPath> paths = authorizationDecision.isAllowed()
                ? authorizationPathTraversalService.findPaths(
                        request.subjectId(), request.resourceId(), request.actionId())
                : Collections.emptySet();

        return ResponseEntity.ok(new AuthorizationCheckResponse(
                authorizationDecision.decision(),
                paths));
    }

    public record AuthorizationCheckRequest(
            @NotNull UUID subjectId,
            @NotNull UUID resourceId,
            @NotNull UUID actionId) {
    }

    public record AuthorizationCheckResponse(
            Decision decision,
            Set<AuthorizationPath> paths) {
    }
}
