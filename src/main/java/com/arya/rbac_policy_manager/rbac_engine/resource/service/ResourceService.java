package com.arya.rbac_policy_manager.rbac_engine.resource.service;

import com.arya.rbac_policy_manager.rbac_engine.common.Enum.Status;
import com.arya.rbac_policy_manager.rbac_engine.resource.entity.Resource;
import com.arya.rbac_policy_manager.rbac_engine.resource.repo.ResourceRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import java.util.List;

@Service
@Transactional
@RequiredArgsConstructor
public class ResourceService {
    private final ResourceRepository resourceRepository;

    private Resource getActiveResource(String name) {
        return resourceRepository.findByNameAndStatus(
                name,
                Status.ACTIVE).orElseThrow(() -> new RuntimeException("Resource not found"));
    }

    public Resource createResource(
            String name,
            String description) {
        if (resourceRepository.existsByName(name)) {
            throw new RuntimeException("Resource already exists");
        }

        Resource resource = new Resource();

        resource.setName(name);
        resource.setDescription(description);
        resource.setStatus(Status.ACTIVE);

        return resourceRepository.save(resource);
    }

    public Resource updateResource(
            String name,
            String description) {
        Resource resource = getActiveResource(name);

        resource.setName(name);
        resource.setDescription(description);

        return resourceRepository.save(resource);
    }

    @Transactional(readOnly = true)
    public Resource getResource(String name) {
        return getActiveResource(name);
    }

    @Transactional(readOnly = true)
    public List<Resource> getAllResources() {
        return resourceRepository.findByStatus( Status.ACTIVE);
    }

    public void disableResource(String name) {
        Resource resource = getActiveResource(name);

        resource.setStatus(Status.DISABLED);

        resourceRepository.save(resource);
    }

    public void deleteResource(String name) {
        Resource resource = getActiveResource(name);

        resource.setStatus(Status.DELETED);

        resourceRepository.save(resource);
    }
}