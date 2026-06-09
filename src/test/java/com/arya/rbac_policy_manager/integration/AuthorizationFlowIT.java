package com.arya.rbac_policy_manager.integration;

import com.arya.rbac_policy_manager.rbac_engine.action.entity.Action;
import com.arya.rbac_policy_manager.rbac_engine.action.service.ActionService;
import com.arya.rbac_policy_manager.rbac_engine.association.entity.RolePermission;
import com.arya.rbac_policy_manager.rbac_engine.association.entity.SubjectRole;
import com.arya.rbac_policy_manager.rbac_engine.association.hierarchyservice.GroupHierarchyService;
import com.arya.rbac_policy_manager.rbac_engine.association.hierarchyservice.RoleHierarchyService;
import com.arya.rbac_policy_manager.rbac_engine.association.service.GroupPermissionService;
import com.arya.rbac_policy_manager.rbac_engine.association.service.RoleGroupService;
import com.arya.rbac_policy_manager.rbac_engine.association.service.RolePermissionService;
import com.arya.rbac_policy_manager.rbac_engine.association.service.SubjectRoleService;
import com.arya.rbac_policy_manager.rbac_engine.common.Enums.Decision;
import com.arya.rbac_policy_manager.rbac_engine.common.Enums.SubjectType;
import com.arya.rbac_policy_manager.rbac_engine.group.entity.Group;
import com.arya.rbac_policy_manager.rbac_engine.group.service.GroupService;
import com.arya.rbac_policy_manager.rbac_engine.permission.entity.Permission;
import com.arya.rbac_policy_manager.rbac_engine.permission.pdp.AuthorizationDecision;
import com.arya.rbac_policy_manager.rbac_engine.permission.pdp.PolicyDecisionPoint;
import com.arya.rbac_policy_manager.rbac_engine.permission.service.PermissionService;
import com.arya.rbac_policy_manager.rbac_engine.resource.entity.Resource;
import com.arya.rbac_policy_manager.rbac_engine.resource.service.ResourceService;
import com.arya.rbac_policy_manager.rbac_engine.role.entity.Role;
import com.arya.rbac_policy_manager.rbac_engine.role.service.RoleService;
import com.arya.rbac_policy_manager.rbac_engine.subject.entity.Subject;
import com.arya.rbac_policy_manager.rbac_engine.subject.service.SubjectService;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.transaction.annotation.Transactional;

import static org.assertj.core.api.Assertions.assertThat;


@SpringBootTest
@Transactional
@ActiveProfiles("test")
class AuthorizationFlowIT {

    @Autowired
    private SubjectService subjectService;
    @Autowired
    private RoleService roleService;
    @Autowired
    private GroupService groupService;
    @Autowired
    private ResourceService resourceService;
    @Autowired
    private ActionService actionService;
    @Autowired
    private PermissionService permissionService;

    @Autowired
    private SubjectRoleService subjectRoleService;
    @Autowired
    private RolePermissionService rolePermissionService;
    @Autowired
    private RoleGroupService roleGroupService;
    @Autowired
    private GroupPermissionService groupPermissionService;

    @Autowired
    private RoleHierarchyService roleHierarchyService;
    @Autowired
    private GroupHierarchyService groupHierarchyService;

    @Autowired
    private PolicyDecisionPoint pdp;

    @Autowired
    private com.arya.rbac_policy_manager.rbac_engine.group.repo.GroupRepository groupRepository;

    // Shared across all tests
    private Resource documents;
    private Action read;
    private Action write;

    @BeforeEach
    void setUp() {
        documents = resourceService.createResource("documents", "Document store");
        read = actionService.createAction("READ", "Read access");
        write = actionService.createAction("WRITE", "Write access");
    }

    @Test
    void shouldPermitDirectRolePermission() {
        Subject alice = subjectService.createSubject("alice", "Alice", SubjectType.HUMAN, null);
        Role editor = roleService.createRole("EDITOR", null);
        Permission pRead = permissionService.createPermission(read.getId(), documents.getId(), null);

        subjectRoleService.assignSubjectToRole(alice.getId(), editor.getId());
        rolePermissionService.assignPermissionToRole(pRead.getId(), editor.getId());

        AuthorizationDecision decision = pdp.evaluate(alice.getId(), documents.getId(), read.getId());
        assertThat(decision.decision()).isEqualTo(Decision.ALLOW);
    }

    @Test
    void shouldPermitInheritedRolePermission() {
        Subject bob = subjectService.createSubject("bob", "Bob", SubjectType.HUMAN, null);
        Role senior = roleService.createRole("SENIOR", null);
        Role junior = roleService.createRole("JUNIOR", null);
        Permission pRead = permissionService.createPermission(read.getId(), documents.getId(), null);

        roleHierarchyService.createRelationship(senior.getId(), junior.getId());
        subjectRoleService.assignSubjectToRole(bob.getId(), senior.getId());
        rolePermissionService.assignPermissionToRole(pRead.getId(), junior.getId());

        AuthorizationDecision decision = pdp.evaluate(bob.getId(), documents.getId(), read.getId());
        assertThat(decision.decision()).isEqualTo(Decision.ALLOW);
    }

    @Test
    void shouldPermitInheritedGroupPermission() {
        Subject bob = subjectService.createSubject("bob", "Bob", SubjectType.HUMAN, null);
        Role senior = roleService.createRole("SENIOR", null);
        Group readers = groupService.createGroup("READERS", null);
        Group editors = groupService.createGroup("EDITORS", null);
        Permission pRead = permissionService.createPermission(read.getId(), documents.getId(), null);

        subjectRoleService.assignSubjectToRole(bob.getId(), senior.getId());
        groupHierarchyService.createRelationship(editors.getId(), readers.getId());
        roleGroupService.assignRoleToGroup(senior.getId(), editors.getId());
        groupPermissionService.assignPermissionToGroup(pRead.getId(), readers.getId());

        AuthorizationDecision decision = pdp.evaluate(bob.getId(), documents.getId(), read.getId());
        assertThat(decision.decision()).isEqualTo(Decision.ALLOW);
    }

    @Test
    void shouldPermitTransitiveGroupPermission() {
        Subject carol = subjectService.createSubject("carol", "Carol", SubjectType.HUMAN, null);
        Role viewer = roleService.createRole("VIEWER", null);
        Group readers = groupService.createGroup("READERS", null);
        Permission pRead = permissionService.createPermission(read.getId(), documents.getId(), null);

        subjectRoleService.assignSubjectToRole(carol.getId(), viewer.getId());
        roleGroupService.assignRoleToGroup(viewer.getId(), readers.getId());
        groupPermissionService.assignPermissionToGroup(pRead.getId(), readers.getId());

        AuthorizationDecision decision = pdp.evaluate(carol.getId(), documents.getId(), read.getId());
        assertThat(decision.decision()).isEqualTo(Decision.ALLOW);
    }

    @Test
    void shouldDenyWhenRoleAssignmentIsDisabled() {
        Subject dave = subjectService.createSubject("dave", "Dave", SubjectType.HUMAN, null);
        Role editor = roleService.createRole("EDITOR_D", null);
        Permission pWrite = permissionService.createPermission(write.getId(), documents.getId(), null);

        SubjectRole assignment = subjectRoleService.assignSubjectToRole(dave.getId(), editor.getId());
        rolePermissionService.assignPermissionToRole(pWrite.getId(), editor.getId());

        // Disable the subject-role link
        subjectRoleService.disableAssignment(assignment.getId());

        AuthorizationDecision decision = pdp.evaluate(dave.getId(), documents.getId(), write.getId());
        assertThat(decision.decision()).isEqualTo(Decision.DENY);
    }

    @Test
    void shouldDenyWhenPermissionIsDisabled() {
        Subject eve = subjectService.createSubject("eve", "Eve", SubjectType.HUMAN, null);
        Role mgr = roleService.createRole("MANAGER_E", null);
        Permission pWrite = permissionService.createPermission(write.getId(), documents.getId(), null);

        subjectRoleService.assignSubjectToRole(eve.getId(), mgr.getId());
        RolePermission assignment = rolePermissionService.assignPermissionToRole(pWrite.getId(), mgr.getId());

        // Disable the role-permission link
        rolePermissionService.disableAssignment(assignment.getId());

        AuthorizationDecision decision = pdp.evaluate(eve.getId(), documents.getId(), write.getId());
        assertThat(decision.decision()).isEqualTo(Decision.DENY);
    }

    @Test
    void shouldResolveComplexAuthorizationGraph() {
        Subject frank = subjectService.createSubject("frank", "Frank", SubjectType.HUMAN, null);
        Role junior = roleService.createRole("JUNIOR_C", null);
        Role senior = roleService.createRole("SENIOR_C", null);
        Group teamLeads = groupService.createGroup("TEAM_LEADS", null);
        Group leadsChild = groupService.createGroup("LEADS_CHILD", null);
        Permission pWrite = permissionService.createPermission(write.getId(), documents.getId(), null);

        roleHierarchyService.createRelationship(senior.getId(), junior.getId());
        subjectRoleService.assignSubjectToRole(frank.getId(), senior.getId());
        roleGroupService.assignRoleToGroup(senior.getId(), teamLeads.getId());
        groupHierarchyService.createRelationship(teamLeads.getId(), leadsChild.getId());
        groupPermissionService.assignPermissionToGroup(pWrite.getId(), leadsChild.getId());

        AuthorizationDecision decision = pdp.evaluate(frank.getId(), documents.getId(), write.getId());
        assertThat(decision.decision()).isEqualTo(Decision.ALLOW);
    }
}