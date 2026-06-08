package com.arya.rbac_policy_manager.rbac_engine.resource.service;

import com.arya.rbac_policy_manager.rbac_engine.common.Enum.Status;
import com.arya.rbac_policy_manager.rbac_engine.common.exception.ActiveEntityNotFoundException;
import com.arya.rbac_policy_manager.rbac_engine.common.exception.DuplicateEntityException;
import com.arya.rbac_policy_manager.rbac_engine.common.exception.EntityNotFoundException;
import com.arya.rbac_policy_manager.rbac_engine.common.exception.InvalidEntityStateException;
import com.arya.rbac_policy_manager.rbac_engine.resource.entity.Resource;
import com.arya.rbac_policy_manager.rbac_engine.resource.repo.ResourceRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

@Service
@Transactional
@RequiredArgsConstructor
public class ResourceService {
    private final ResourceRepository resourceRepository;

    private Resource getActiveResource(UUID resourceId) {
        return resourceRepository.findByIdAndStatus( resourceId, Status.ACTIVE)
                            .orElseThrow(() -> new ActiveEntityNotFoundException("Resource", resourceId));
    }

    public Resource createResource(
            String name,
            String description) {
        Optional<Resource> existing = resourceRepository.findByName(name);

        if (existing.isPresent())
        {
            throw new DuplicateEntityException( "Resource", name, existing.get().getStatus());
        }

        Resource resource = new Resource();

        resource.setName(name);
        resource.setDescription(description);
        resource.setStatus(Status.ACTIVE);

        return resourceRepository.save(resource);
    }

    public Resource updateResource(
            UUID resourceId,
            String name,
            String description) {
        Resource resource = getActiveResource(resourceId);

        resource.setName(name);
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

        resource.setStatus(Status.DISABLED);

        resourceRepository.save(resource);
    }

    public void deleteResource(UUID resourceId) {
        Resource resource = resourceRepository.findById(resourceId).orElseThrow(() -> new EntityNotFoundException("Resource not found"));

        if(resource.getStatus() == Status.ACTIVE) {
            throw new InvalidEntityStateException("Active resource cannot be deleted. Consider disabling instead");
        }

        if(resource.getStatus() == Status.DELETED) {
            throw new InvalidEntityStateException("Resource already deleted.");
        }

        resource.setStatus(Status.DELETED);

        resourceRepository.save(resource);
    }
}