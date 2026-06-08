package com.arya.rbac_policy_manager.rbac_engine.common.exception;

import java.util.UUID;

public class EntityNotFoundException extends RbacException {

    public EntityNotFoundException(String message)
    {
        super(message);
    }

    public EntityNotFoundException(String message, Throwable cause)
    {
        super(message, cause);
    }

    public EntityNotFoundException(String entityType, UUID id)
    {
        super("" + entityType + " not found with id: " + id);
    }

    public EntityNotFoundException(String entityType, String identifier)
    {//  Role not found with identifier: arya.parge
        super("" + entityType + " not found with identifier: " + identifier);
    }
    
}
