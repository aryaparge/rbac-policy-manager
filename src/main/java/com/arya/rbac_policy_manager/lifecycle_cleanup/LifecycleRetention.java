package com.arya.rbac_policy_manager.lifecycle_cleanup;

import java.time.Duration;

public final class LifecycleRetention {

    public static final Duration DISABLED_RETENTION =
            Duration.ofDays(60);

    public static final Duration DELETED_RETENTION =
            Duration.ofDays(30);

    private LifecycleRetention() {
    }
}
