package com.arya.rbac_policy_manager.rbac_engine.common.exception;

public abstract class RbacException extends RuntimeException
{
    public RbacException(String message)
    {
        super(message);
    }

    public RbacException(String message, Throwable cause)
    {
        super(message, cause);
    }
}