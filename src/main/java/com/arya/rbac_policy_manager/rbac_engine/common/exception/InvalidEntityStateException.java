package com.arya.rbac_policy_manager.rbac_engine.common.exception;

public class InvalidEntityStateException extends RbacException
{
    public InvalidEntityStateException(String message)
    {
        super(message);
    }

    public InvalidEntityStateException(String message, Throwable cause)
    {
        super(message, cause);
    }
}