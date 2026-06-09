package com.arya.rbac_policy_manager.rbac_engine.permission.pdp;

import com.arya.rbac_policy_manager.rbac_engine.common.Enums.Decision;

public record AuthorizationDecision( Decision decision )
{
    public boolean isAllowed()
    {
        return decision == Decision.ALLOW;
    }
}