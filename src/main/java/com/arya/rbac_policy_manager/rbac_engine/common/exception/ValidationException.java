package com.arya.rbac_policy_manager.rbac_engine.common.exception;

public class ValidationException extends RbacException
{
    public ValidationException(String message)
    {
        super(message);
    }

    public ValidationException(String message, Throwable cause)
    {
        super(message, cause);
    }
}