package com.arya.rbac_policy_manager.rbac_engine.resource.service;

import com.arya.rbac_policy_manager.rbac_engine.association.repo.GroupPermissionRepository;
import com.arya.rbac_policy_manager.rbac_engine.association.repo.RolePermissionRepository;
import com.arya.rbac_policy_manager.rbac_engine.common.Enums.Status;
import com.arya.rbac_policy_manager.rbac_engine.common.exception.ActiveEntityNotFoundException;
import com.arya.rbac_policy_manager.rbac_engine.common.exception.DuplicateEntityException;
import com.arya.rbac_policy_manager.rbac_engine.common.exception.EntityNotFoundException;
import com.arya.rbac_policy_manager.rbac_engine.common.exception.InvalidEntityStateException;
import com.arya.rbac_policy_manager.rbac_engine.permission.repo.PermissionRepository;
import com.arya.rbac_policy_manager.rbac_engine.resource.entity.Resource;
import com.arya.rbac_policy_manager.rbac_engine.resource.repo.ResourceRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.Instant;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

@Service
@Transactional
@RequiredArgsConstructor
public class ResourceService {
    private final ResourceRepository resourceRepository;

    private final RolePermissionRepository rolePermissionRepository;
    private final GroupPermissionRepository groupPermissionRepository;
    private final PermissionRepository permissionRepository;

    public Resource getActiveResource(UUID resourceId) {
        return resourceRepository.findByIdAndStatus( resourceId, Status.ACTIVE)
                            .orElseThrow(() -> new ActiveEntityNotFoundException("Resource", resourceId));
    }

    public Resource createResource(
            String name,
            String displayName,
            String description) {
        Optional<Resource> existing = resourceRepository.findByName(name);

        if (existing.isPresent())
        {
            throw new DuplicateEntityException( "Resource", name, existing.get().getStatus());
        }

        Resource resource = new Resource();

        resource.setName(name);
        resource.setDisplayName(displayName);
        resource.setDescription(description);
        resource.setStatus(Status.ACTIVE);

        return resourceRepository.save(resource);
    }

    public Resource updateResource(
            UUID resourceId,
            String displayName,
            String description) {
        Resource resource = getActiveResource(resourceId);

        resource.setDisplayName(displayName);
        resource.setDescription(description);

        return resourceRepository.save(resource);
    }

    @Transactional(readOnly = true)
    public Resource getResource(UUID resourceId) {
        return getActiveResource(resourceId);
    }

    @Transactional(readOnly = true)
    public List<Resource> getAllResources() {
        return resourceRepository.findByStatus( Status.ACTIVE);
    }

    public void disableResource(UUID resourceId) {
        Resource resource = getActiveResource(resourceId);

        Instant now = Instant.now();

        resource.setStatus(Status.DISABLED);
        resource.setDisabledAt(now);
        resource.setDeletedAt(null); //ensure deletedAt is null.
        resourceRepository.save(resource);

        permissionRepository.cascadedMarkPermissionsAsDisabled(now);
        groupPermissionRepository.cascadedMarkGroupPermissionsAsDisabled(now);
        rolePermissionRepository.cascadedMarkRolePermissionsAsDisabled(now);
    }

    public void enableResource(UUID resourceId) {
        // Enabling a resource does not automatically enable related entities. They must be manually enabled if needed.
        Resource resource = resourceRepository.findById(resourceId)
                .orElseThrow(() -> new EntityNotFoundException("Resource not found"));

        if(resource.getStatus() == Status.ACTIVE) {
            throw new InvalidEntityStateException("Resource is already active.");
        }

        if(resource.getStatus() == Status.DELETED) {
            throw new InvalidEntityStateException("Deleted resource cannot be enabled.");
        }

        resource.setStatus(Status.ACTIVE);
        resource.setDisabledAt(null); //ensure disabledAt is null.
        resource.setDeletedAt(null); //ensure deletedAt is null.

        resourceRepository.save(resource);
    }   
    // Manual deletion of resources is not allowed. Disable the resource and let the scheduled cleanup handle the rest.
}