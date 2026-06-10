package com.arya.rbac_policy_manager.lifecycle_cleanup;

import java.time.Instant;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.arya.rbac_policy_manager.rbac_engine.action.repo.ActionRepository;
import com.arya.rbac_policy_manager.rbac_engine.association.repo.GroupHierarchyRepository;
import com.arya.rbac_policy_manager.rbac_engine.association.repo.GroupPermissionRepository;
import com.arya.rbac_policy_manager.rbac_engine.association.repo.RoleGroupRepository;
import com.arya.rbac_policy_manager.rbac_engine.association.repo.RoleHierarchyRepository;
import com.arya.rbac_policy_manager.rbac_engine.association.repo.RolePermissionRepository;
import com.arya.rbac_policy_manager.rbac_engine.association.repo.SubjectRoleRepository;
import com.arya.rbac_policy_manager.rbac_engine.group.repo.GroupRepository;
import com.arya.rbac_policy_manager.rbac_engine.permission.repo.PermissionRepository;
import com.arya.rbac_policy_manager.rbac_engine.resource.repo.ResourceRepository;
import com.arya.rbac_policy_manager.rbac_engine.role.repo.RoleRepository;
import com.arya.rbac_policy_manager.rbac_engine.subject.repo.SubjectRepository;

import lombok.RequiredArgsConstructor;

@Service
@RequiredArgsConstructor
public class LifecycleCleanupService {

    private final ActionRepository actionRepository;
    private final GroupRepository groupRepository;
    private final RoleRepository roleRepository;
    private final ResourceRepository resourceRepository;
    private final PermissionRepository permissionRepository;
    private final SubjectRepository subjectRepository;

    private final GroupHierarchyRepository groupHierarchyRepository;
    private final RoleHierarchyRepository roleHierarchyRepository;

    private final GroupPermissionRepository groupPermissionRepository;
    private final RoleGroupRepository roleGroupRepository;
    private final RolePermissionRepository rolePermissionRepository;
    private final SubjectRoleRepository subjectRoleRepository;

    private Instant now() {
        return Instant.now();
    }

    private Instant disabledCutoff(Instant now) {
        return now.minus(LifecycleRetention.DISABLED_RETENTION);
    }

    private Instant deletedCutoff(Instant now) {
        return now.minus(LifecycleRetention.DELETED_RETENTION);
    }

    @Transactional
    public void cleanUpActions() {
        Instant now = now();

        actionRepository.markDisabledActionsAsDeleted(disabledCutoff(now), now);
        actionRepository.hardDeleteExpiredActions(deletedCutoff(now));
    }

    @Transactional
    public void cleanUpGroups() {
        Instant now = now();

        groupRepository.markDisabledGroupsAsDeleted(disabledCutoff(now), now);
        groupRepository.hardDeleteExpiredGroups(deletedCutoff(now));
    }

    @Transactional
    public void cleanUpRoles() {
        Instant now = now();

        roleRepository.markDisabledRolesAsDeleted(disabledCutoff(now), now);
        roleRepository.hardDeleteExpiredRoles(deletedCutoff(now));
    }

    @Transactional
    public void cleanUpResources() {
        Instant now = now();

        resourceRepository.markDisabledResourcesAsDeleted(disabledCutoff(now), now);
        resourceRepository.hardDeleteExpiredResources(deletedCutoff(now));
    }

    @Transactional
    public void cleanUpPermissions() {
        Instant now = now();

        permissionRepository.markDisabledPermissionsAsDeleted(disabledCutoff(now), now);
        permissionRepository.hardDeleteExpiredPermissions(deletedCutoff(now));
    }

    @Transactional
    public void cleanUpSubjects() {
        Instant now = now();

        subjectRepository.markDisabledSubjectsAsDeleted(disabledCutoff(now), now);
        subjectRepository.hardDeleteExpiredSubjects(deletedCutoff(now));
    }

    @Transactional
    public void cleanUpGroupHierarchies() {
        Instant now = now();

        groupHierarchyRepository.markDisabledGroupHierarchiesAsDeleted(disabledCutoff(now), now);
        groupHierarchyRepository.hardDeleteExpiredGroupHierarchies(deletedCutoff(now));
    }

    @Transactional
    public void cleanUpRoleHierarchies() {
        Instant now = now();

        roleHierarchyRepository.markDisabledRoleHierarchiesAsDeleted(disabledCutoff(now), now);
        roleHierarchyRepository.hardDeleteExpiredRoleHierarchies(deletedCutoff(now));
    }

    @Transactional
    public void cleanUpGroupPermissions() {
        Instant now = now();

        groupPermissionRepository.markDisabledGroupPermissionsAsDeleted(disabledCutoff(now), now);
        groupPermissionRepository.hardDeleteExpiredGroupPermissions(deletedCutoff(now));
    }

    @Transactional
    public void cleanUpRoleGroups() {
        Instant now = now();

        roleGroupRepository.markDisabledRoleGroupsAsDeleted(disabledCutoff(now), now);
        roleGroupRepository.hardDeleteExpiredRoleGroups(deletedCutoff(now));
    }

    @Transactional
    public void cleanUpRolePermissions() {
        Instant now = now();

        rolePermissionRepository.markDisabledRolePermissionsAsDeleted(disabledCutoff(now), now);
        rolePermissionRepository.hardDeleteExpiredRolePermissions(deletedCutoff(now));
    }

    @Transactional
    public void cleanUpSubjectRoles() {
        Instant now = now();

        subjectRoleRepository.markDisabledSubjectRolesAsDeleted(disabledCutoff(now), now);
        subjectRoleRepository.hardDeleteExpiredSubjectRoles(deletedCutoff(now));
    }
}
