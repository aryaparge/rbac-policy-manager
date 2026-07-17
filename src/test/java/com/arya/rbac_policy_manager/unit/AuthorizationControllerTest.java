package com.arya.rbac_policy_manager.unit;

import com.arya.rbac_policy_manager.api.controller.AuthorizationController;
import com.arya.rbac_policy_manager.explainability_engine.AuthorizationPath;
import com.arya.rbac_policy_manager.explainability_engine.AuthorizationPathTraversalService;
import com.arya.rbac_policy_manager.explainability_engine.NodeType;
import com.arya.rbac_policy_manager.explainability_engine.PathNode;
import com.arya.rbac_policy_manager.rbac_engine.common.Enums.Decision;
import com.arya.rbac_policy_manager.rbac_engine.permission.pdp.AuthorizationDecision;
import com.arya.rbac_policy_manager.rbac_engine.permission.pdp.PolicyDecisionPoint;
import org.junit.jupiter.api.Test;

import java.util.List;
import java.util.Set;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyNoInteractions;
import static org.mockito.Mockito.when;

class AuthorizationControllerTest {

    @Test
    void returnsDecisionAndPositiveEvidenceForAnAllowedRequest() {
        PolicyDecisionPoint pdp = mock(PolicyDecisionPoint.class);
        AuthorizationPathTraversalService paths = mock(AuthorizationPathTraversalService.class);
        AuthorizationController controller = new AuthorizationController(pdp, paths);
        UUID subjectId = UUID.randomUUID();
        UUID resourceId = UUID.randomUUID();
        UUID actionId = UUID.randomUUID();
        AuthorizationPath path = AuthorizationPath.of(List.of(
                new PathNode("alice", subjectId, NodeType.SUBJECT)));

        when(pdp.evaluate(subjectId, resourceId, actionId))
                .thenReturn(new AuthorizationDecision(Decision.ALLOW));
        when(paths.findPaths(subjectId, resourceId, actionId)).thenReturn(Set.of(path));

        AuthorizationController.AuthorizationCheckResponse response = controller.checkAuthorization(
                new AuthorizationController.AuthorizationCheckRequest(subjectId, resourceId, actionId)).getBody();

        assertThat(response).isNotNull();
        assertThat(response.decision()).isEqualTo(Decision.ALLOW);
        assertThat(response.paths()).containsExactly(path);
        verify(paths).findPaths(subjectId, resourceId, actionId);
    }

    @Test
    void returnsNoPathsForADeniedRequest() {
        PolicyDecisionPoint pdp = mock(PolicyDecisionPoint.class);
        AuthorizationPathTraversalService paths = mock(AuthorizationPathTraversalService.class);
        AuthorizationController controller = new AuthorizationController(pdp, paths);
        UUID subjectId = UUID.randomUUID();
        UUID resourceId = UUID.randomUUID();
        UUID actionId = UUID.randomUUID();

        when(pdp.evaluate(subjectId, resourceId, actionId))
                .thenReturn(new AuthorizationDecision(Decision.DENY));

        AuthorizationController.AuthorizationCheckResponse response = controller.checkAuthorization(
                new AuthorizationController.AuthorizationCheckRequest(subjectId, resourceId, actionId)).getBody();

        assertThat(response).isNotNull();
        assertThat(response.decision()).isEqualTo(Decision.DENY);
        assertThat(response.paths()).isEmpty();
        verify(pdp).evaluate(subjectId, resourceId, actionId);
        verifyNoInteractions(paths);
    }
}
