package com.arya.rbac_policy_manager.rbac_engine.common.exception;

public class PermissionNotFoundException extends RbacException
{
    public PermissionNotFoundException(String message)
    {
        super(message);
    }

    public PermissionNotFoundException(String message, Throwable cause)
    {
        super(message, cause);
    }
}