package com.arya.rbac_policy_manager.rbac_engine.common.exception;

public class CycleDetectedException extends RbacException
{
    public CycleDetectedException(String message)
    {
        super(message);
    }

    public CycleDetectedException(String message, Throwable cause)
    {
        super(message, cause);
    }
}