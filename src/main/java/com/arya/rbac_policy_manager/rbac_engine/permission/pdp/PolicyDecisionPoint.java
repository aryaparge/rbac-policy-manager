package com.arya.rbac_policy_manager.rbac_engine.permission.pdp;

import java.util.Set;
import java.util.UUID;

import org.springframework.stereotype.Service;

import com.arya.rbac_policy_manager.rbac_engine.common.Enums.Decision;
import com.arya.rbac_policy_manager.rbac_engine.permission.entity.Permission;
import com.arya.rbac_policy_manager.rbac_engine.permission.resolution.PermissionResolutionService;

import lombok.RequiredArgsConstructor;

@Service
@RequiredArgsConstructor
public class PolicyDecisionPoint
{
    private final PermissionResolutionService permissionResolutionService;

    public AuthorizationDecision evaluate(
        UUID subjectId,
        UUID resourceId,
        UUID actionId
    )
    {
        Set<Permission> permissions = permissionResolutionService.resolvePermissions(subjectId);

        boolean allowed =
            permissions.stream()
                .anyMatch(permission -> permission.getResource().getId().equals(resourceId) && permission.getAction().getId().equals(actionId));

        return new AuthorizationDecision(
            allowed ? Decision.ALLOW : Decision.DENY
        );
    }
}