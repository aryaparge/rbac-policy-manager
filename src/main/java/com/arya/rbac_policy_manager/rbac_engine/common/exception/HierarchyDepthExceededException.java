package com.arya.rbac_policy_manager.rbac_engine.common.exception;

public class HierarchyDepthExceededException extends RbacException
{
    public HierarchyDepthExceededException(String message)
    {
        super(message);
    }

    public HierarchyDepthExceededException(String message, Throwable cause)
    {
        super(message, cause);
    }
}