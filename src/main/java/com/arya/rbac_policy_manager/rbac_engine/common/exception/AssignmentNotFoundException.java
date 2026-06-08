package com.arya.rbac_policy_manager.rbac_engine.common.exception;

public class AssignmentNotFoundException extends RbacException
{
    public AssignmentNotFoundException(String message)
    {
        super(message);
    }

    public AssignmentNotFoundException(String message, Throwable cause)
    {
        super(message, cause);
    }
}