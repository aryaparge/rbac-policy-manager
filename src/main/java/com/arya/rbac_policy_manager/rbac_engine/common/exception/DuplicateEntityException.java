package com.arya.rbac_policy_manager.rbac_engine.common.exception;

import com.arya.rbac_policy_manager.rbac_engine.common.Enum.Status;

public class DuplicateEntityException extends RbacException {
    public DuplicateEntityException(String message) {
        super(message);
    }

    public DuplicateEntityException(String message, Throwable cause) {
        super(message, cause);
    }

    public DuplicateEntityException(
            String entityType,
            String identifier,
            Status status) {
        super(buildMessage(entityType, identifier, status));
    }

    private static String buildMessage(
            String entityType,
            String identifier,
            Status status) {
        if (status == Status.DELETED) {
            return entityType +
                    " '" + identifier +
                    "' already exists in DELETED state. Consider reactivating it.";
        }

        if (status == Status.DISABLED) {
            return entityType +
                    " '" + identifier +
                    "' already exists in DISABLED state. Consider enabling it.";
        }

        return entityType +
                " '" + identifier +
                "' already exists.";
    }
}