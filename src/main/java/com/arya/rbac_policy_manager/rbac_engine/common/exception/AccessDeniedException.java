package com.arya.rbac_policy_manager.rbac_engine.common.exception;

public class AccessDeniedException extends RbacException
{
    public AccessDeniedException(String message)
    {
        super(message);
    }

    public AccessDeniedException(String message, Throwable cause)
    {
        super(message, cause);
    }
}