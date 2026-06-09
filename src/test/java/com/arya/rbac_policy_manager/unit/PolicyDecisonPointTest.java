package com.arya.rbac_policy_manager.unit;

import com.arya.rbac_policy_manager.rbac_engine.action.entity.Action;
import com.arya.rbac_policy_manager.rbac_engine.common.Enums.Decision;
import com.arya.rbac_policy_manager.rbac_engine.common.Enums.Status;
import com.arya.rbac_policy_manager.rbac_engine.permission.entity.Permission;
import com.arya.rbac_policy_manager.rbac_engine.permission.pdp.AuthorizationDecision;
import com.arya.rbac_policy_manager.rbac_engine.permission.pdp.PolicyDecisionPoint;
import com.arya.rbac_policy_manager.rbac_engine.permission.resolution.PermissionResolutionService;
import com.arya.rbac_policy_manager.rbac_engine.resource.entity.Resource;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.Set;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.when;


@ExtendWith(MockitoExtension.class)
class PolicyDecisionPointTest {

    @Mock
    private PermissionResolutionService permissionResolutionService;

    @InjectMocks
    private PolicyDecisionPoint pdp;

    private UUID subjectId;
    private UUID resourceId;
    private UUID actionId;
    private Permission matchingPermission;

    @BeforeEach
    void setUp() {
        subjectId  = UUID.randomUUID();
        resourceId = UUID.randomUUID();
        actionId   = UUID.randomUUID();
        matchingPermission = buildPermission(resourceId, actionId, Status.ACTIVE);
    }

   
    @Test
    void shouldPermitWhenPermissionExists() {
        when(permissionResolutionService.resolvePermissions(subjectId))
                .thenReturn(Set.of(matchingPermission));

        AuthorizationDecision decision = pdp.evaluate(subjectId, resourceId, actionId);

        assertThat(decision.decision()).isEqualTo(Decision.ALLOW);
        assertThat(decision.isAllowed()).isTrue();
    }

   
    @Test
    void shouldDenyWhenPermissionMissing() {
        when(permissionResolutionService.resolvePermissions(subjectId))
                .thenReturn(Set.of());

        AuthorizationDecision decision = pdp.evaluate(subjectId, resourceId, actionId);

        assertThat(decision.decision()).isEqualTo(Decision.DENY);
        assertThat(decision.isAllowed()).isFalse();
    }

    @Test
    void shouldPermitForInheritedPermission() {
        // An unrelated permission is also in the set — the PDP must pick the right one
        Permission unrelatedPermission = buildPermission(UUID.randomUUID(), UUID.randomUUID(), Status.ACTIVE);

        when(permissionResolutionService.resolvePermissions(subjectId))
                .thenReturn(Set.of(unrelatedPermission, matchingPermission));

        AuthorizationDecision decision = pdp.evaluate(subjectId, resourceId, actionId);

        assertThat(decision.decision()).isEqualTo(Decision.ALLOW);
        assertThat(decision.isAllowed()).isTrue();
    }

    
    @Test
    void shouldDenyForDisabledPermission() {
        // Simulate: resolution layer already excluded the disabled permission
        when(permissionResolutionService.resolvePermissions(subjectId))
                .thenReturn(Set.of()); // disabled permission was not included

        AuthorizationDecision decision = pdp.evaluate(subjectId, resourceId, actionId);

        assertThat(decision.decision()).isEqualTo(Decision.DENY);
        assertThat(decision.isAllowed()).isFalse();
    }

   
    private Permission buildPermission(UUID resourceId, UUID actionId, Status status) {
        Resource resource = new Resource();
        resource.setId(resourceId);
        resource.setName("resource-" + resourceId);
        resource.setStatus(Status.ACTIVE);

        Action action = new Action();
        action.setId(actionId);
        action.setName("action-" + actionId);
        action.setStatus(Status.ACTIVE);

        Permission permission = new Permission();
        permission.setId(UUID.randomUUID());
        permission.setResource(resource);
        permission.setAction(action);
        permission.setStatus(status);
        return permission;
    }
}
