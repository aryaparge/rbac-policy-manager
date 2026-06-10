package com.arya.rbac_policy_manager.lifecycle_cleanup;

import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

import lombok.RequiredArgsConstructor;

@Component
@RequiredArgsConstructor
public class LifecycleCleanupScheduler {

    private final LifecycleCleanupService lifecycleCleanupService;

    @Scheduled(cron = "0 0 2 * * *")
    public void runCleanup() {

        lifecycleCleanupService.cleanUpGroupHierarchies();
        lifecycleCleanupService.cleanUpRoleHierarchies();

        lifecycleCleanupService.cleanUpGroupPermissions();
        lifecycleCleanupService.cleanUpRolePermissions();

        lifecycleCleanupService.cleanUpRoleGroups();
        lifecycleCleanupService.cleanUpSubjectRoles();

        lifecycleCleanupService.cleanUpPermissions();

        lifecycleCleanupService.cleanUpRoles();
        lifecycleCleanupService.cleanUpGroups();

        lifecycleCleanupService.cleanUpSubjects();

        lifecycleCleanupService.cleanUpActions();
        lifecycleCleanupService.cleanUpResources();
    }
}