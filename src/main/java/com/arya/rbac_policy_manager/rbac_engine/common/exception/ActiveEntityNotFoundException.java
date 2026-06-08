package com.arya.rbac_policy_manager.rbac_engine.common.exception;

import java.util.UUID;

public class ActiveEntityNotFoundException extends RbacException {

    public ActiveEntityNotFoundException(String message)
    {
        super(message);
    }

    public ActiveEntityNotFoundException(String message, Throwable cause)
    {
        super(message, cause);
    }

    public ActiveEntityNotFoundException(String entityType, UUID id)
    {
        super("Active" + entityType + " not found with id: " + id);
    }

    public ActiveEntityNotFoundException(String entityType, String identifier)
    {// Active Role not found with identifier: arya.parge
        super("Active" + entityType + " not found with identifier: " + identifier);
    }
    
}
