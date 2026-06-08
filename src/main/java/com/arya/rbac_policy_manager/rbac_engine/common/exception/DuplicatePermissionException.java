package com.arya.rbac_policy_manager.rbac_engine.common.exception;

public class DuplicatePermissionException extends RbacException
{
    public DuplicatePermissionException(String message)
    {
        super(message);
    }

    public DuplicatePermissionException(String message, Throwable cause)
    {
        super(message, cause);
    }
}