package com.arya.rbac_policy_manager.unit;

import com.arya.rbac_policy_manager.rbac_engine.association.entity.RoleHierarchy;
import com.arya.rbac_policy_manager.rbac_engine.association.hierarchyservice.RoleHierarchyTraversalService;
import com.arya.rbac_policy_manager.rbac_engine.association.hierarchyservice.RoleHierarchyValidationService;
import com.arya.rbac_policy_manager.rbac_engine.association.repo.RoleHierarchyRepository;
import com.arya.rbac_policy_manager.rbac_engine.common.Enums.Status;
import com.arya.rbac_policy_manager.rbac_engine.role.entity.Role;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.List;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThatCode;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.when;


@ExtendWith(MockitoExtension.class)
class CycleDetectionServiceTest {

    @Mock
    private RoleHierarchyRepository roleHierarchyRepository;

    @InjectMocks
    private RoleHierarchyTraversalService traversalService;

    private RoleHierarchyValidationService validationService;

    // Roles used across tests
    private Role roleA;
    private Role roleB;
    private Role roleC;
    private Role roleD;

    @BeforeEach
    void setUp() {
        // validationService depends on the traversalService — wire manually
        // because Mockito @InjectMocks would need the repo in both.
        validationService = new RoleHierarchyValidationService(roleHierarchyRepository, traversalService);

        roleA = role("A");
        roleB = role("B");
        roleC = role("C");
        roleD = role("D");
    }

    /**
     * A → B is a fresh, valid edge. No existing edges, so isReachable(B, A)
     * returns false and validation must not throw.
     */
    @Test
    void shouldAllowValidHierarchy() {
        // No edges exist yet — DFS from B finds nothing.
        when(roleHierarchyRepository.findByParentRoleAndStatus(eq(roleB), eq(Status.ACTIVE)))
                .thenReturn(List.of());

        assertThatCode(() -> validationService.validateNoCycle(roleA, roleB))
                .doesNotThrowAnyException();
    }

    
    /**
     * Existing edge: B → A.
     * Attempting to add A → B would create the cycle A ↔ B.
     * isReachable(B, A) must return true, so validateNoCycle must throw.
     */
    @Test
    void shouldDetectSimpleCycle() {
        // B → A already exists
        RoleHierarchy bToA = hierarchy(roleB, roleA);
        when(roleHierarchyRepository.findByParentRoleAndStatus(eq(roleB), eq(Status.ACTIVE)))
                .thenReturn(List.of(bToA));

        // Adding A → B must be rejected
        assertThatThrownBy(() -> validationService.validateNoCycle(roleA, roleB))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessageContaining("cycle");
    }

    
    /**
     * Existing chain: C → B → A.
     * Attempting to add A → C would create the cycle A → C → B → A.
     */
    @Test
    void shouldDetectIndirectCycle() {
        // C → B → A
        RoleHierarchy cToB = hierarchy(roleC, roleB);
        RoleHierarchy bToA = hierarchy(roleB, roleA);

        when(roleHierarchyRepository.findByParentRoleAndStatus(eq(roleC), eq(Status.ACTIVE)))
                .thenReturn(List.of(cToB));
        when(roleHierarchyRepository.findByParentRoleAndStatus(eq(roleB), eq(Status.ACTIVE)))
                .thenReturn(List.of(bToA));

        // Adding A → C: validateNoCycle(parent=A, child=C)
        // → check isReachable(C, A): DFS from C reaches B then A → true → throw
        assertThatThrownBy(() -> validationService.validateNoCycle(roleA, roleC))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessageContaining("cycle");
    }

    /**
     * Diamond graph:   A → B, A → C, B → D, C → D  (all existing edges).
     * Attempting to add D → A would close a cycle through multiple paths.
     * isReachable(D, A) must return true.
     */
    @Test
        void shouldDetectCycleInDiamondGraph() {

        // Existing edges:
        // A → B
        // A → C
        // C → D
        RoleHierarchy aToB = hierarchy(roleA, roleB);
        RoleHierarchy aToC = hierarchy(roleA, roleC);
        RoleHierarchy cToD = hierarchy(roleC, roleD);

        // DFS starts from A
        when(roleHierarchyRepository.findByParentRoleAndStatus(eq(roleA), eq(Status.ACTIVE)))
                .thenReturn(List.of(aToB, aToC));

        // DFS visits C first and reaches D
        when(roleHierarchyRepository.findByParentRoleAndStatus(eq(roleC), eq(Status.ACTIVE)))
                .thenReturn(List.of(cToD));

        // Adding D → A must be rejected because
        // A can already reach D.
        assertThatThrownBy(() -> validationService.validateNoCycle(roleD, roleA))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessageContaining("cycle");
        }

    private Role role(String name) {
        Role r = new Role();
        r.setId(UUID.randomUUID());
        r.setName(name);
        r.setStatus(Status.ACTIVE);
        return r;
    }

    private RoleHierarchy hierarchy(Role parent, Role child) {
        RoleHierarchy h = new RoleHierarchy();
        h.setId(UUID.randomUUID());
        h.setParentRole(parent);
        h.setChildRole(child);
        h.setStatus(Status.ACTIVE);
        return h;
    }
}